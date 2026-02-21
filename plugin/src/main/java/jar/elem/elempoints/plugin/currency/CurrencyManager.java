package jar.elem.elempoints.plugin.currency;

import jar.elem.elempoints.plugin.ElemPointsPlugin;
import jar.elem.elempoints.plugin.config.CurrencyConfig;
import jar.elem.elempoints.api.event.BalanceChangeEvent;
import jar.elem.elempoints.api.event.CurrencyRegisterEvent;
import jar.elem.elempoints.api.event.TransferEvent;
import jar.elem.elempoints.api.result.TransactionResult;
import jar.elem.elempoints.plugin.storage.StorageFactory;
import jar.elem.elempoints.plugin.storage.StorageProvider;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core manager for currency operations, caching, and event firing.
 */
public final class CurrencyManager {

    private final ElemPointsPlugin plugin;
    private final CurrencyRegistry registry;
    private final StorageFactory storageFactory;

    // Cache: currencyId → (uuid → balance)
    private final Map<String, Map<UUID, Double>> cache = new ConcurrentHashMap<>();

    public CurrencyManager(ElemPointsPlugin plugin, CurrencyRegistry registry, StorageFactory storageFactory) {
        this.plugin = plugin;
        this.registry = registry;
        this.storageFactory = storageFactory;
    }

    /**
     * Register a currency from config.
     */
    public void registerCurrency(CurrencyConfig config) {
        CurrencyImpl impl = new CurrencyImpl(config, config.getId());
        registry.register(impl);
        cache.put(config.getId(), new ConcurrentHashMap<>());
        storageFactory.initCurrency(config);
        Bukkit.getPluginManager().callEvent(new CurrencyRegisterEvent(impl));
    }

    public CurrencyRegistry getRegistry() { return registry; }

    // ═══════ Balance Operations ═══════

    public double getBalance(UUID player, String currencyId) {
        validateCurrency(currencyId);
        Map<UUID, Double> cc = cache.get(currencyId);
        if (cc != null && cc.containsKey(player)) {
            return cc.get(player);
        }
        // Load from storage
        StorageProvider sp = storageFactory.getProvider(currencyId);
        double bal = sp.getBalance(player, currencyId).join();
        if (bal < 0) {
            // No account — create default
            CurrencyImpl cur = registry.get(currencyId);
            double def = cur != null ? cur.getDefaultBalance() : 0;
            sp.createAccount(player, currencyId, def).join();
            bal = def;
        }
        if (cc != null) cc.put(player, bal);
        return bal;
    }

    public TransactionResult setBalance(UUID player, String currencyId, double amount,
                                        BalanceChangeEvent.Reason reason, String source) {
        validateCurrency(currencyId);
        CurrencyImpl cur = registry.get(currencyId);
        double old = getBalance(player, currencyId);

        // Enforce limits
        if (cur.getMaxBalance() > 0 && amount > cur.getMaxBalance()) {
            return TransactionResult.failure(TransactionResult.Status.MAX_BALANCE_EXCEEDED, old,
                    "Max balance: " + cur.getMaxBalance());
        }
        double minBal = cur.getConfig().getMinBalance();
        if (amount < minBal) amount = minBal;

        // Fire event
        BalanceChangeEvent event = new BalanceChangeEvent(player, cur, old, amount, reason, source);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return TransactionResult.failure(TransactionResult.Status.CANCELLED_BY_EVENT, old, "Event cancelled");
        }
        double finalAmount = event.getNewBalance();

        // Update cache
        Map<UUID, Double> cc = cache.get(currencyId);
        if (cc != null) cc.put(player, finalAmount);

        // Write to storage
        storageFactory.getProvider(currencyId).setBalance(player, currencyId, finalAmount);

        return TransactionResult.success(old, finalAmount, Math.abs(finalAmount - old));
    }

    public TransactionResult deposit(UUID player, String currencyId, double amount,
                                     BalanceChangeEvent.Reason reason, String source) {
        if (amount <= 0) return TransactionResult.failure(TransactionResult.Status.ERROR, "Amount must be > 0");
        double current = getBalance(player, currencyId);
        return setBalance(player, currencyId, current + amount, reason, source);
    }

    public TransactionResult withdraw(UUID player, String currencyId, double amount,
                                      BalanceChangeEvent.Reason reason, String source) {
        if (amount <= 0) return TransactionResult.failure(TransactionResult.Status.ERROR, "Amount must be > 0");
        double current = getBalance(player, currencyId);
        if (current < amount) {
            return TransactionResult.failure(TransactionResult.Status.INSUFFICIENT_FUNDS, current,
                    "Need " + amount + ", have " + current);
        }
        return setBalance(player, currencyId, current - amount, reason, source);
    }

    public boolean has(UUID player, String currencyId, double amount) {
        return getBalance(player, currencyId) >= amount;
    }

    // ═══════ Transfer ═══════

    public TransactionResult transfer(UUID from, UUID to, String currencyId, double amount) {
        validateCurrency(currencyId);
        CurrencyImpl cur = registry.get(currencyId);

        if (!cur.isTransferEnabled()) {
            return TransactionResult.failure(TransactionResult.Status.TRANSFER_DISABLED, "Transfers disabled");
        }
        if (amount < cur.getMinTransfer()) {
            return TransactionResult.failure(TransactionResult.Status.BELOW_MINIMUM,
                    "Minimum: " + cur.getMinTransfer());
        }

        double fee = amount * (cur.getTransferFee() / 100.0);
        double totalCost = amount + fee;
        double senderBal = getBalance(from, currencyId);

        if (senderBal < totalCost) {
            return TransactionResult.failure(TransactionResult.Status.INSUFFICIENT_FUNDS, senderBal,
                    "Need " + totalCost + " (+" + fee + " fee)");
        }

        // Fire transfer event
        TransferEvent event = new TransferEvent(from, to, cur, amount, fee);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return TransactionResult.failure(TransactionResult.Status.CANCELLED_BY_EVENT, senderBal, "Event cancelled");
        }

        double fAmount = event.getAmount();
        double fFee = event.getFee();
        double fTotal = fAmount + fFee;

        if (senderBal < fTotal) {
            return TransactionResult.failure(TransactionResult.Status.INSUFFICIENT_FUNDS, senderBal, "After event");
        }

        setBalance(from, currencyId, senderBal - fTotal,
                BalanceChangeEvent.Reason.PLAYER_TRANSFER, "transfer:send");
        deposit(to, currencyId, fAmount,
                BalanceChangeEvent.Reason.PLAYER_TRANSFER, "transfer:receive");

        return TransactionResult.success(senderBal, senderBal - fTotal, fAmount);
    }

    // ═══════ Export ═══════

    public CompletableFuture<Integer> exportData(String fromId, String toId, double rate) {
        return CompletableFuture.supplyAsync(() -> {
            StorageProvider fromSp = storageFactory.getProvider(fromId);
            StorageProvider toSp = storageFactory.getProvider(toId);

            Map<UUID, Double> fromBalances = fromSp.getAllBalances(fromId).join();
            Map<UUID, Double> converted = new HashMap<>();

            for (Map.Entry<UUID, Double> e : fromBalances.entrySet()) {
                UUID uuid = e.getKey();
                double existing = toSp.getBalance(uuid, toId).join();
                if (existing < 0) existing = 0;
                converted.put(uuid, existing + (e.getValue() * rate));
            }

            toSp.bulkSetBalances(toId, converted).join();

            // Update cache
            Map<UUID, Double> toCache = cache.get(toId);
            if (toCache != null) toCache.putAll(converted);

            return converted.size();
        });
    }

    // ═══════ Async wrappers ═══════

    public CompletableFuture<Double> getBalanceAsync(UUID player, String currencyId) {
        return CompletableFuture.supplyAsync(() -> getBalance(player, currencyId));
    }

    public CompletableFuture<TransactionResult> depositAsync(UUID player, String currencyId, double amount) {
        return CompletableFuture.supplyAsync(() ->
                deposit(player, currencyId, amount, BalanceChangeEvent.Reason.API_CALL, "async"));
    }

    public CompletableFuture<TransactionResult> withdrawAsync(UUID player, String currencyId, double amount) {
        return CompletableFuture.supplyAsync(() ->
                withdraw(player, currencyId, amount, BalanceChangeEvent.Reason.API_CALL, "async"));
    }

    public CompletableFuture<TransactionResult> setBalanceAsync(UUID player, String currencyId, double amount) {
        return CompletableFuture.supplyAsync(() ->
                setBalance(player, currencyId, amount, BalanceChangeEvent.Reason.API_CALL, "async"));
    }

    // ═══════ Player cache ═══════

    public void loadPlayer(UUID player) {
        for (String id : cache.keySet()) {
            getBalance(player, id);
        }
    }

    public void unloadPlayer(UUID player) {
        for (Map<UUID, Double> cc : cache.values()) {
            cc.remove(player);
        }
    }

    public void saveAll() {
        for (Map.Entry<String, Map<UUID, Double>> entry : cache.entrySet()) {
            String curId = entry.getKey();
            Map<UUID, Double> balances = entry.getValue();
            if (!balances.isEmpty()) {
                storageFactory.getProvider(curId).bulkSetBalances(curId, new HashMap<>(balances));
            }
        }
    }

    // ═══════ Validation ═══════

    private void validateCurrency(String currencyId) {
        if (!registry.has(currencyId)) {
            throw new jar.elem.elempoints.api.exception.CurrencyNotFoundException(currencyId);
        }
    }
}