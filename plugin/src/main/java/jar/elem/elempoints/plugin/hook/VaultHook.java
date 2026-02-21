package jar.elem.elempoints.plugin.hook;


import jar.elem.elempoints.plugin.ElemPointsPlugin;
import jar.elem.elempoints.plugin.currency.CurrencyManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

public final class VaultHook {

    private final ElemPointsPlugin plugin;
    private VaultEconomyProvider provider;

    public VaultHook(ElemPointsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean hook(CurrencyManager manager) {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().info("[Hook] Vault not found — skipping.");
            return false;
        }
        provider = new VaultEconomyProvider(plugin, manager);
        Bukkit.getServicesManager().register(Economy.class, provider, plugin, ServicePriority.Highest);
        plugin.getLogger().info("[Hook] Vault economy registered. Primary: " +
                (manager.getRegistry().getPrimary() != null ? manager.getRegistry().getPrimary().getId() : "none"));
        return true;
    }

    public void unhook() {
        if (provider != null) {
            Bukkit.getServicesManager().unregister(Economy.class, provider);
            provider = null;
        }
    }
}