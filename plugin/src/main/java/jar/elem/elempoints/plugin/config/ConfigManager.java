package jar.elem.elempoints.plugin.config;

import jar.elem.elempoints.plugin.ElemPointsPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages all configuration files: config.yml, messages.yml, currencies/*.yml
 */
public final class ConfigManager {

    private final ElemPointsPlugin plugin;
    private final MessagesConfig messagesConfig;
    private final Map<String, CurrencyConfig> currencyConfigs = new LinkedHashMap<>();
    private List<String> enabledCurrencyIds = new ArrayList<>();

    public ConfigManager(ElemPointsPlugin plugin) {
        this.plugin = plugin;
        this.messagesConfig = new MessagesConfig();
    }

    /**
     * Load all configs. Called on enable and reload.
     */
    public void loadAll() {
        // 1. Save defaults
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        saveDefault("messages.yml");
        saveDefault("currencies/points.yml");
        saveDefault("currencies/crystals.yml");

        // 2. Load messages.yml
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        YamlConfiguration messagesYaml = YamlConfiguration.loadConfiguration(messagesFile);
        messagesConfig.load(messagesYaml);

        // 3. Read enabled currencies from config.yml
        enabledCurrencyIds = plugin.getConfig().getStringList("currencies.enabled");
        if (enabledCurrencyIds.isEmpty()) {
            plugin.getLogger().warning("No currencies enabled in config.yml!");
        }

        // 4. Load each currency .yml
        currencyConfigs.clear();
        File currenciesDir = new File(plugin.getDataFolder(), "currencies");
        if (!currenciesDir.exists()) {
            currenciesDir.mkdirs();
        }

        for (String id : enabledCurrencyIds) {
            File file = new File(currenciesDir, id + ".yml");
            if (!file.exists()) {
                plugin.getLogger().warning("Currency file not found: currencies/" + id + ".yml — skipping.");
                continue;
            }
            try {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                CurrencyConfig config = new CurrencyConfig(yaml);

                if (!config.getId().equals(id)) {
                    plugin.getLogger().warning("Currency file " + id + ".yml has mismatched id '" +
                            config.getId() + "'. Using filename as id.");
                }

                currencyConfigs.put(id, config);
                plugin.getLogger().info("Loaded currency: " + id +
                        (config.isPrimary() ? " [PRIMARY]" : "") +
                        " /" + config.getPlayerCommand());
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load currency: " + id, e);
            }
        }

        if (currencyConfigs.isEmpty()) {
            plugin.getLogger().severe("No currencies loaded! The plugin won't function properly.");
        }
    }

    private void saveDefault(String resourcePath) {
        File file = new File(plugin.getDataFolder(), resourcePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (InputStream in = plugin.getResource(resourcePath)) {
                if (in == null) {
                    plugin.getLogger().warning("Default resource not found: " + resourcePath);
                    return;
                }
                try (OutputStream out = new FileOutputStream(file)) {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to save default: " + resourcePath, e);
            }
        }
    }

    public MessagesConfig getMessages() { return messagesConfig; }
    public Map<String, CurrencyConfig> getCurrencyConfigs() { return Collections.unmodifiableMap(currencyConfigs); }
    public CurrencyConfig getCurrencyConfig(String id) { return currencyConfigs.get(id); }
    public List<String> getEnabledCurrencyIds() { return Collections.unmodifiableList(enabledCurrencyIds); }
}