package jar.elem.elempoints.plugin.storage;

import jar.elem.elempoints.plugin.ElemPointsPlugin;
import jar.elem.elempoints.plugin.config.CurrencyConfig;
import jar.elem.elempoints.plugin.storage.mysql.MySQLProvider;
import jar.elem.elempoints.plugin.storage.sqlite.SQLiteProvider;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Factory that creates storage providers from configuration.
 */
public final class StorageFactory {

    private final ElemPointsPlugin plugin;
    private final Map<String, StorageProvider> providers = new HashMap<>();
    private StorageProvider globalProvider;

    public StorageFactory(ElemPointsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Initialize the global storage provider from config.yml database section.
     */
    public void initGlobal() {
        ConfigurationSection dbSection = plugin.getConfig().getConfigurationSection("database");
        globalProvider = createFromSection(dbSection, "storage/global.db");
        globalProvider.init();
    }

    /**
     * Initialize per-currency storage if it has a custom database config.
     */
    public void initCurrency(CurrencyConfig config) {
        if (config.hasCustomDatabase()) {
            Map<String, Object> dbMap = config.getDatabaseOverride();
            StorageProvider sp = createFromMap(dbMap, "storage/" + config.getId() + ".db");
            sp.init();
            providers.put(config.getId(), sp);
            plugin.getLogger().info("[Storage] Custom storage for '" + config.getId() + "' initialized.");
        }
    }

    /**
     * Get storage provider for a currency (falls back to global).
     */
    public StorageProvider getProvider(String currencyId) {
        return providers.getOrDefault(currencyId, globalProvider);
    }

    /**
     * Shutdown all providers.
     */
    public void shutdownAll() {
        providers.values().forEach(StorageProvider::shutdown);
        if (globalProvider != null) globalProvider.shutdown();
        providers.clear();
    }

    // ─── Internal factory methods ───

    private StorageProvider createFromSection(ConfigurationSection section, String defaultFile) {
        if (section == null) return new SQLiteProvider(plugin, defaultFile);
        String type = section.getString("type", "SQLITE").toUpperCase();
        if ("MYSQL".equals(type)) {
            return createMySQL(section.getConfigurationSection("mysql"));
        }
        String file = defaultFile;
        ConfigurationSection sqlite = section.getConfigurationSection("sqlite");
        if (sqlite != null) file = sqlite.getString("file", defaultFile);
        return new SQLiteProvider(plugin, file);
    }

    private StorageProvider createMySQL(ConfigurationSection mysql) {
        if (mysql == null) return new SQLiteProvider(plugin, "storage/global.db");
        Map<String, String> props = new LinkedHashMap<>();
        ConfigurationSection propsSection = mysql.getConfigurationSection("properties");
        if (propsSection != null) {
            for (String k : propsSection.getKeys(false)) props.put(k, propsSection.getString(k));
        }
        ConfigurationSection pool = mysql.getConfigurationSection("pool");
        return new MySQLProvider(plugin,
                mysql.getString("host", "localhost"),
                mysql.getInt("port", 3306),
                mysql.getString("database", "elempoints"),
                mysql.getString("username", "root"),
                mysql.getString("password", ""),
                mysql.getString("table-prefix", "ep_"),
                pool != null ? pool.getInt("max-size", 10) : 10,
                pool != null ? pool.getInt("min-idle", 2) : 2,
                pool != null ? pool.getLong("max-lifetime", 1800000L) : 1800000L,
                pool != null ? pool.getLong("timeout", 5000L) : 5000L,
                props);
    }

    @SuppressWarnings("unchecked")
    private StorageProvider createFromMap(Map<String, Object> map, String defaultFile) {
        String type = String.valueOf(map.getOrDefault("type", "SQLITE")).toUpperCase();
        if ("MYSQL".equals(type)) {
            Map<String, Object> mysql = (Map<String, Object>) map.getOrDefault("mysql", new HashMap<>());
            Map<String, Object> pool = (Map<String, Object>) mysql.getOrDefault("pool", new HashMap<>());
            Map<String, String> props = new LinkedHashMap<>();
            Object propsObj = mysql.get("properties");
            if (propsObj instanceof Map) {
                ((Map<String, Object>) propsObj).forEach((k, v) -> props.put(k, String.valueOf(v)));
            }
            return new MySQLProvider(plugin,
                    (String) mysql.getOrDefault("host", "localhost"),
                    ((Number) mysql.getOrDefault("port", 3306)).intValue(),
                    (String) mysql.getOrDefault("database", "elempoints"),
                    (String) mysql.getOrDefault("username", "root"),
                    (String) mysql.getOrDefault("password", ""),
                    (String) mysql.getOrDefault("table-prefix", "ep_"),
                    ((Number) pool.getOrDefault("max-size", 10)).intValue(),
                    ((Number) pool.getOrDefault("min-idle", 2)).intValue(),
                    ((Number) pool.getOrDefault("max-lifetime", 1800000L)).longValue(),
                    ((Number) pool.getOrDefault("timeout", 5000L)).longValue(),
                    props);
        }
        Map<String, Object> sqlite = (Map<String, Object>) map.getOrDefault("sqlite", new HashMap<>());
        String file = (String) sqlite.getOrDefault("file", defaultFile);
        return new SQLiteProvider(plugin, file);
    }
}