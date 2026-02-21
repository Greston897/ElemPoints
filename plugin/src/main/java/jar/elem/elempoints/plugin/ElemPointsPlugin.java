package jar.elem.elempoints.plugin;

import jar.elem.elempoints.api.ElemPointsProvider;
import jar.elem.elempoints.plugin.command.CommandRegistry;
import jar.elem.elempoints.plugin.config.ConfigManager;
import jar.elem.elempoints.plugin.config.CurrencyConfig;
import jar.elem.elempoints.plugin.currency.CurrencyManager;
import jar.elem.elempoints.plugin.currency.CurrencyRegistry;
import jar.elem.elempoints.plugin.hook.PAPIExpansion;
import jar.elem.elempoints.plugin.hook.VaultHook;
import jar.elem.elempoints.plugin.listener.PlayerListener;
import jar.elem.elempoints.plugin.storage.StorageFactory;
import jar.elem.elempoints.plugin.util.NumberFormat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Locale;

public final class ElemPointsPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private StorageFactory storageFactory;
    private CurrencyRegistry currencyRegistry;
    private CurrencyManager currencyManager;
    private CommandRegistry commandRegistry;
    private VaultHook vaultHook;
    private PAPIExpansion papiExpansion;
    private NumberFormat numberFormat;
    private ElemPointsAPIImpl api;
    private BukkitTask autoSaveTask;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        // ─── Config ───
        configManager = new ConfigManager(this);
        configManager.loadAll();

        // ─── Locale / Number format ───
        numberFormat = createNumberFormat();

        // ─── Storage ───
        storageFactory = new StorageFactory(this);
        storageFactory.initGlobal();

        // ─── Currencies ───
        currencyRegistry = new CurrencyRegistry();
        currencyManager = new CurrencyManager(this, currencyRegistry, storageFactory);

        for (CurrencyConfig cfg : configManager.getCurrencyConfigs().values()) {
            currencyManager.registerCurrency(cfg);
        }
        currencyRegistry.ensurePrimary();

        // ─── Commands ───
        commandRegistry = new CommandRegistry(this, currencyManager, configManager.getMessages(), numberFormat);
        commandRegistry.registerAll();

        // ─── API ───
        api = new ElemPointsAPIImpl(this, currencyManager);
        ElemPointsProvider.register(api);

        // ─── Vault ───
        vaultHook = new VaultHook(this);
        vaultHook.hook(currencyManager);

        // ─── PlaceholderAPI ───
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            papiExpansion = new PAPIExpansion(this, currencyManager, numberFormat);
            papiExpansion.register();
            getLogger().info("[Hook] PlaceholderAPI expansion registered.");
        }

        // ─── Listener ───
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this, currencyManager), this);

        // ─── Load online players ───
        for (Player p : Bukkit.getOnlinePlayers()) {
            currencyManager.loadPlayer(p.getUniqueId());
        }

        // ─── Auto-save ───
        startAutoSave();

        long elapsed = System.currentTimeMillis() - start;
        getLogger().info("═══════════════════════════════════════");
        getLogger().info(" ElemPoints v" + getDescription().getVersion() + " enabled!");
        getLogger().info(" Loaded " + currencyRegistry.size() + " currencies in " + elapsed + "ms");
        getLogger().info(" Server: " + Bukkit.getVersion());
        getLogger().info("═══════════════════════════════════════");
    }

    @Override
    public void onDisable() {
        if (autoSaveTask != null) autoSaveTask.cancel();
        if (currencyManager != null) currencyManager.saveAll();
        if (vaultHook != null) vaultHook.unhook();
        if (papiExpansion != null) papiExpansion.unregister();
        ElemPointsProvider.unregister();
        if (commandRegistry != null) commandRegistry.unregisterAll();
        if (storageFactory != null) storageFactory.shutdownAll();
        getLogger().info("ElemPoints disabled. All data saved.");
    }

    /**
     * Full reload accessible from commands and API.
     */
    public void performReload() {
        // Save
        currencyManager.saveAll();

        // Teardown
        if (autoSaveTask != null) autoSaveTask.cancel();
        commandRegistry.unregisterAll();
        vaultHook.unhook();
        if (papiExpansion != null) papiExpansion.unregister();
        ElemPointsProvider.unregister();
        storageFactory.shutdownAll();

        // Rebuild
        configManager.loadAll();
        numberFormat = createNumberFormat();

        storageFactory = new StorageFactory(this);
        storageFactory.initGlobal();

        currencyRegistry = new CurrencyRegistry();
        currencyManager = new CurrencyManager(this, currencyRegistry, storageFactory);

        for (CurrencyConfig cfg : configManager.getCurrencyConfigs().values()) {
            currencyManager.registerCurrency(cfg);
        }
        currencyRegistry.ensurePrimary();

        commandRegistry = new CommandRegistry(this, currencyManager, configManager.getMessages(), numberFormat);
        commandRegistry.registerAll();

        api = new ElemPointsAPIImpl(this, currencyManager);
        ElemPointsProvider.register(api);

        vaultHook = new VaultHook(this);
        vaultHook.hook(currencyManager);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            papiExpansion = new PAPIExpansion(this, currencyManager, numberFormat);
            papiExpansion.register();
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            currencyManager.loadPlayer(p.getUniqueId());
        }

        startAutoSave();

        getLogger().info("Reloaded! " + currencyRegistry.size() + " currencies active.");
    }

    private NumberFormat createNumberFormat() {
        String localeStr = getConfig().getString("general.locale", "en_US");
        try {
            String[] parts = localeStr.split("_");
            Locale locale = parts.length > 1 ? new Locale(parts[0], parts[1]) : new Locale(parts[0]);
            return new NumberFormat(locale);
        } catch (Exception e) {
            return new NumberFormat(Locale.US);
        }
    }

    private void startAutoSave() {
        int minutes = getConfig().getInt("general.auto-save-interval", 5);
        if (minutes > 0) {
            long ticks = minutes * 60L * 20L;
            autoSaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                currencyManager.saveAll();
                if (getConfig().getBoolean("general.debug", false)) {
                    getLogger().info("[AutoSave] Complete.");
                }
            }, ticks, ticks);
        }
    }
}