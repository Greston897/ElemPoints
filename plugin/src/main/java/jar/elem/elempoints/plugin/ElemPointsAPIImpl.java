package jar.elem.elempoints.plugin;

import jar.elem.elempoints.api.ElemPointsAPI;
import jar.elem.elempoints.api.exception.CurrencyNotFoundException;
import jar.elem.elempoints.plugin.currency.CurrencyManager;
import jar.elem.elempoints.api.event.BalanceChangeEvent;
import jar.elem.elempoints.api.result.TransactionResult;
import jar.elem.elempoints.api.currency.Currency;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public final class ElemPointsAPIImpl implements ElemPointsAPI {

    private final ElemPointsPlugin plugin;
    private final CurrencyManager manager;

    public ElemPointsAPIImpl(ElemPointsPlugin plugin, CurrencyManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    // ═══════ Currency ═══════

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Currency> getCurrencies() {
        return (Collection<Currency>) (Collection<?>) manager.getRegistry().all();
    }

    @Override
    public Optional<jar.elem.elempoints.api.currency.Currency> getCurrency(String currencyId) {
        return manager.getRegistry().find(currencyId);
    }

    @Override
    public Currency getPrimaryCurrency() {
        return manager.getRegistry().getPrimary();
    }

    @Override
    public boolean hasCurrency(String currencyId) {
        return manager.getRegistry().has(currencyId);
    }

    // ═══════ Balance (specific currency) ═══════

    @Override
    public double getBalance(UUID player, String currencyId) {
        ensureCurrency(currencyId);
        return manager.getBalance(player, currencyId);
    }

    @Override
    public TransactionResult setBalance(UUID player, String currencyId, double amount) {
        ensureCurrency(currencyId);
        return manager.setBalance(player, currencyId, amount, BalanceChangeEvent.Reason.API_CALL, "api");
    }

    @Override
    public TransactionResult deposit(UUID player, String currencyId, double amount) {
        ensureCurrency(currencyId);
        return manager.deposit(player, currencyId, amount, BalanceChangeEvent.Reason.API_CALL, "api");
    }

    @Override
    public TransactionResult withdraw(UUID player, String currencyId, double amount) {
        ensureCurrency(currencyId);
        return manager.withdraw(player, currencyId, amount, BalanceChangeEvent.Reason.API_CALL, "api");
    }

    @Override
    public boolean has(UUID player, String currencyId, double amount) {
        ensureCurrency(currencyId);
        return manager.has(player, currencyId, amount);
    }

    // ═══════ Balance (primary) ═══════

    private String primary() {
        jar.elem.elempoints.api.currency.Currency c = getPrimaryCurrency();
        return c != null ? c.getId() : "points";
    }

    @Override
    public double getBalance(UUID player) {
        return getBalance(player, primary());
    }

    @Override
    public TransactionResult deposit(UUID player, double amount) {
        return deposit(player, primary(), amount);
    }

    @Override
    public TransactionResult withdraw(UUID player, double amount) {
        return withdraw(player, primary(), amount);
    }

    @Override
    public boolean has(UUID player, double amount) {
        return has(player, primary(), amount);
    }

    // ═══════ Transfer ═══════

    @Override
    public TransactionResult transfer(UUID from, UUID to, String currencyId, double amount) {
        ensureCurrency(currencyId);
        return manager.transfer(from, to, currencyId, amount);
    }

    // ═══════ Async ═══════

    @Override
    public CompletableFuture<Double> getBalanceAsync(UUID player, String currencyId) {
        ensureCurrency(currencyId);
        return manager.getBalanceAsync(player, currencyId);
    }

    @Override
    public CompletableFuture<TransactionResult> depositAsync(UUID player, String currencyId, double amount) {
        ensureCurrency(currencyId);
        return manager.depositAsync(player, currencyId, amount);
    }

    @Override
    public CompletableFuture<TransactionResult> withdrawAsync(UUID player, String currencyId, double amount) {
        ensureCurrency(currencyId);
        return manager.withdrawAsync(player, currencyId, amount);
    }

    @Override
    public CompletableFuture<TransactionResult> setBalanceAsync(UUID player, String currencyId, double amount) {
        ensureCurrency(currencyId);
        return manager.setBalanceAsync(player, currencyId, amount);
    }

    // ═══════ Export ═══════

    @Override
    public CompletableFuture<Integer> exportData(String fromCurrencyId, String toCurrencyId, double rate) {
        ensureCurrency(fromCurrencyId);
        ensureCurrency(toCurrencyId);
        return manager.exportData(fromCurrencyId, toCurrencyId, rate);
    }

    // ═══════ Utility ═══════

    @Override
    public boolean isReady() {
        return plugin.isEnabled() && manager.getRegistry().getPrimary() != null;
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }

    @Override
    public void logCurrencyNotFound(String callingPlugin, String currencyId) {
        plugin.getLogger().warning("[" + callingPlugin + "] Currency '" + currencyId +
                "' not found in ElemPoints registry! Available currencies: " +
                manager.getRegistry().all().toString());
    }

    // ═══════ Internal ═══════

    private void ensureCurrency(String id) {
        if (!manager.getRegistry().has(id)) {
            throw new CurrencyNotFoundException(id);
        }
    }
}