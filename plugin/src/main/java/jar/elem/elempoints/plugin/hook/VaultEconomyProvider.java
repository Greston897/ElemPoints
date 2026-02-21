package jar.elem.elempoints.plugin.hook;

import jar.elem.elempoints.plugin.ElemPointsPlugin;
import jar.elem.elempoints.plugin.currency.CurrencyImpl;
import jar.elem.elempoints.plugin.currency.CurrencyManager;
import jar.elem.elempoints.api.event.BalanceChangeEvent;
import jar.elem.elempoints.api.result.TransactionResult;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("deprecation")
public final class VaultEconomyProvider implements Economy {

    private final ElemPointsPlugin plugin;
    private final CurrencyManager manager;

    public VaultEconomyProvider(ElemPointsPlugin plugin, CurrencyManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    private String pid() {
        CurrencyImpl c = manager.getRegistry().getPrimary();
        return c != null ? c.getId() : "points";
    }

    @Override public boolean isEnabled() { return plugin.isEnabled(); }
    @Override public String getName() { return "ElemPoints"; }
    @Override public boolean hasBankSupport() { return false; }
    @Override public int fractionalDigits() { return 2; }
    @Override public String format(double amount) { return String.format("%.2f", amount); }

    @Override public String currencyNamePlural() {
        CurrencyImpl c = manager.getRegistry().getPrimary();
        return c != null ? c.getPlural() : "points";
    }
    @Override public String currencyNameSingular() {
        CurrencyImpl c = manager.getRegistry().getPrimary();
        return c != null ? c.getSingular() : "point";
    }

    @Override public boolean hasAccount(String name) { return true; }
    @Override public boolean hasAccount(OfflinePlayer p) { return true; }
    @Override public boolean hasAccount(String name, String world) { return true; }
    @Override public boolean hasAccount(OfflinePlayer p, String world) { return true; }

    @Override public double getBalance(OfflinePlayer p) {
        return manager.getBalance(p.getUniqueId(), pid());
    }
    @Override public double getBalance(String name) {
        return getBalance(plugin.getServer().getOfflinePlayer(name));
    }
    @Override public double getBalance(String name, String world) { return getBalance(name); }
    @Override public double getBalance(OfflinePlayer p, String world) { return getBalance(p); }

    @Override public boolean has(OfflinePlayer p, double amount) {
        return manager.has(p.getUniqueId(), pid(), amount);
    }
    @Override public boolean has(String name, double amount) {
        return has(plugin.getServer().getOfflinePlayer(name), amount);
    }
    @Override public boolean has(String name, String world, double amount) { return has(name, amount); }
    @Override public boolean has(OfflinePlayer p, String world, double amount) { return has(p, amount); }

    @Override public EconomyResponse withdrawPlayer(OfflinePlayer p, double amount) {
        if (amount < 0) return fail("Cannot withdraw negative");
        TransactionResult r = manager.withdraw(p.getUniqueId(), pid(), amount,
                BalanceChangeEvent.Reason.VAULT, "vault");
        if (r.isSuccess()) return new EconomyResponse(amount, r.getNewBalance(),
                EconomyResponse.ResponseType.SUCCESS, null);
        return new EconomyResponse(0, r.getOldBalance(),
                EconomyResponse.ResponseType.FAILURE, r.getMessage());
    }
    @Override public EconomyResponse withdrawPlayer(String name, double amount) {
        return withdrawPlayer(plugin.getServer().getOfflinePlayer(name), amount);
    }
    @Override public EconomyResponse withdrawPlayer(String name, String world, double amount) {
        return withdrawPlayer(name, amount);
    }
    @Override public EconomyResponse withdrawPlayer(OfflinePlayer p, String world, double amount) {
        return withdrawPlayer(p, amount);
    }

    @Override public EconomyResponse depositPlayer(OfflinePlayer p, double amount) {
        if (amount < 0) return fail("Cannot deposit negative");
        TransactionResult r = manager.deposit(p.getUniqueId(), pid(), amount,
                BalanceChangeEvent.Reason.VAULT, "vault");
        if (r.isSuccess()) return new EconomyResponse(amount, r.getNewBalance(),
                EconomyResponse.ResponseType.SUCCESS, null);
        return new EconomyResponse(0, r.getOldBalance(),
                EconomyResponse.ResponseType.FAILURE, r.getMessage());
    }
    @Override public EconomyResponse depositPlayer(String name, double amount) {
        return depositPlayer(plugin.getServer().getOfflinePlayer(name), amount);
    }
    @Override public EconomyResponse depositPlayer(String name, String world, double amount) {
        return depositPlayer(name, amount);
    }
    @Override public EconomyResponse depositPlayer(OfflinePlayer p, String world, double amount) {
        return depositPlayer(p, amount);
    }

    @Override public boolean createPlayerAccount(String name) { return true; }
    @Override public boolean createPlayerAccount(OfflinePlayer p) { return true; }
    @Override public boolean createPlayerAccount(String name, String world) { return true; }
    @Override public boolean createPlayerAccount(OfflinePlayer p, String world) { return true; }

    private EconomyResponse fail(String msg) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, msg);
    }
    private EconomyResponse noBank() {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No banks");
    }

    @Override public EconomyResponse createBank(String name, String player) { return noBank(); }
    @Override public EconomyResponse createBank(String name, OfflinePlayer player) { return noBank(); }
    @Override public EconomyResponse deleteBank(String name) { return noBank(); }
    @Override public EconomyResponse bankBalance(String name) { return noBank(); }
    @Override public EconomyResponse bankHas(String name, double amount) { return noBank(); }
    @Override public EconomyResponse bankWithdraw(String name, double amount) { return noBank(); }
    @Override public EconomyResponse bankDeposit(String name, double amount) { return noBank(); }
    @Override public EconomyResponse isBankOwner(String name, String playerName) { return noBank(); }
    @Override public EconomyResponse isBankOwner(String name, OfflinePlayer player) { return noBank(); }
    @Override public EconomyResponse isBankMember(String name, String playerName) { return noBank(); }
    @Override public EconomyResponse isBankMember(String name, OfflinePlayer player) { return noBank(); }
    @Override public List<String> getBanks() { return Collections.emptyList(); }
}