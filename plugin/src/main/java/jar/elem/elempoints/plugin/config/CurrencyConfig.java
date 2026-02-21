package jar.elem.elempoints.plugin.config;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parsed currency configuration from a .yml file.
 */
public final class CurrencyConfig {

    private final String id;
    private final String displayName;
    private final String singular;
    private final String plural;
    private final String symbol;

    private final double defaultBalance;
    private final boolean primary;
    private final double maxBalance;
    private final double minBalance;

    private final boolean transferEnabled;
    private final double transferFeePercent;
    private final double minTransfer;

    private final String playerCommand;
    private final List<String> playerAliases;
    private final String playerPermission;
    private final String adminCommand;
    private final List<String> adminAliases;
    private final String adminPermission;

    private final Map<String, Object> databaseOverride;
    private final Map<String, String> messageOverrides;

    public CurrencyConfig(YamlConfiguration yaml) {
        this.id = yaml.getString("id", "unknown");

        this.displayName = yaml.getString("display.name", "&f" + id);
        this.singular = yaml.getString("display.singular", id);
        this.plural = yaml.getString("display.plural", id + "s");
        this.symbol = yaml.getString("display.symbol", "");

        this.defaultBalance = yaml.getDouble("economy.default-balance", 0);
        this.primary = yaml.getBoolean("economy.primary", false);
        this.maxBalance = yaml.getDouble("economy.max-balance", 0);
        this.minBalance = yaml.getDouble("economy.min-balance", 0);

        this.transferEnabled = yaml.getBoolean("transfer.enabled", true);
        this.transferFeePercent = yaml.getDouble("transfer.fee-percent", 0);
        this.minTransfer = yaml.getDouble("transfer.minimum", 1);

        this.playerCommand = yaml.getString("commands.player.name", id);
        this.playerAliases = yaml.getStringList("commands.player.aliases");
        this.playerPermission = yaml.getString("commands.player.permission",
                "elempoints.currency." + id + ".use");
        this.adminCommand = yaml.getString("commands.admin.name", id + "admin");
        this.adminAliases = yaml.getStringList("commands.admin.aliases");
        this.adminPermission = yaml.getString("commands.admin.permission",
                "elempoints.currency." + id + ".admin");

        // Parse database override
        if (yaml.isConfigurationSection("database")) {
            this.databaseOverride = new HashMap<>();
            for (String key : yaml.getConfigurationSection("database").getKeys(true)) {
                Object val = yaml.getConfigurationSection("database").get(key);
                if (!(val instanceof org.bukkit.configuration.ConfigurationSection)) {
                    this.databaseOverride.put(key, val);
                }
            }
        } else {
            this.databaseOverride = null;
        }

        // Parse message overrides
        if (yaml.isConfigurationSection("messages")) {
            this.messageOverrides = new HashMap<>();
            flattenSection("", yaml.getConfigurationSection("messages"), this.messageOverrides);
        } else {
            this.messageOverrides = Collections.emptyMap();
        }
    }

    private void flattenSection(String prefix,
                                org.bukkit.configuration.ConfigurationSection section,
                                Map<String, String> target) {
        for (String key : section.getKeys(false)) {
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            if (section.isConfigurationSection(key)) {
                flattenSection(fullKey, section.getConfigurationSection(key), target);
            } else {
                target.put(fullKey, section.getString(key, ""));
            }
        }
    }

    // Getters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getSingular() { return singular; }
    public String getPlural() { return plural; }
    public String getSymbol() { return symbol; }
    public double getDefaultBalance() { return defaultBalance; }
    public boolean isPrimary() { return primary; }
    public double getMaxBalance() { return maxBalance; }
    public double getMinBalance() { return minBalance; }
    public boolean isTransferEnabled() { return transferEnabled; }
    public double getTransferFeePercent() { return transferFeePercent; }
    public double getMinTransfer() { return minTransfer; }
    public String getPlayerCommand() { return playerCommand; }
    public List<String> getPlayerAliases() { return playerAliases; }
    public String getPlayerPermission() { return playerPermission; }
    public String getAdminCommand() { return adminCommand; }
    public List<String> getAdminAliases() { return adminAliases; }
    public String getAdminPermission() { return adminPermission; }
    public Map<String, Object> getDatabaseOverride() { return databaseOverride; }
    public boolean hasCustomDatabase() { return databaseOverride != null && !databaseOverride.isEmpty(); }
    public Map<String, String> getMessageOverrides() { return messageOverrides; }
}