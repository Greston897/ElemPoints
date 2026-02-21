package jar.elem.elempoints.plugin.config;

import jar.elem.elempoints.plugin.util.ChatUtil;
import jar.elem.elempoints.plugin.util.NumberFormat;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Global message configuration with per-currency override support.
 */
public final class MessagesConfig {

    private final Map<String, String> globalMessages = new HashMap<>();
    private String prefix = "";

    public MessagesConfig() {}

    /**
     * Load global messages from messages.yml.
     */
    public void load(YamlConfiguration yaml) {
        globalMessages.clear();
        this.prefix = yaml.getString("prefix", "");
        flattenSection("", yaml, globalMessages);
        // Remove 'prefix' key from map to avoid recursion
        globalMessages.remove("prefix");
    }

    private void flattenSection(String pfx, org.bukkit.configuration.ConfigurationSection section,
                                Map<String, String> target) {
        for (String key : section.getKeys(false)) {
            String fullKey = pfx.isEmpty() ? key : pfx + "." + key;
            if (section.isConfigurationSection(key)) {
                flattenSection(fullKey, section.getConfigurationSection(key), target);
            } else {
                target.put(fullKey, section.getString(key, ""));
            }
        }
    }

    /**
     * Get a raw message string. First checks currency overrides, then global.
     */
    public String getRaw(String path, Map<String, String> currencyOverrides) {
        if (currencyOverrides != null && currencyOverrides.containsKey(path)) {
            return currencyOverrides.get(path);
        }
        return globalMessages.getOrDefault(path, "&cMissing: " + path);
    }

    /**
     * Get a fully processed message with placeholders applied.
     */
    public String get(String path, Map<String, String> currencyOverrides, Map<String, String> placeholders) {
        String msg = getRaw(path, currencyOverrides);
        // Replace %prefix%
        msg = msg.replace("%prefix%", prefix);
        msg = ChatUtil.replacePlaceholders(msg, placeholders);
        return msg;
    }

    /**
     * Send a message to a sender.
     */
    public void send(CommandSender sender, String path,
                     Map<String, String> currencyOverrides,
                     Map<String, String> placeholders) {
        String msg = get(path, currencyOverrides, placeholders);
        ChatUtil.send(sender, msg);
    }

    /**
     * Build standard placeholders for a currency context.
     */
    public static Map<String, String> currencyPlaceholders(CurrencyConfig config, NumberFormat fmt) {
        Map<String, String> ph = new HashMap<>();
        ph.put("currency", config.getDisplayName());
        ph.put("symbol", config.getSymbol());
        ph.put("command", config.getPlayerCommand());
        ph.put("admin_command", config.getAdminCommand());
        return ph;
    }

    /**
     * Add balance placeholders.
     */
    public static void addBalancePlaceholders(Map<String, String> ph, double balance, NumberFormat fmt) {
        ph.put("balance", fmt.plain(balance));
        ph.put("balance_fmt", fmt.formatted(balance));
        ph.put("balance_short", fmt.shorthand(balance));
    }

    /**
     * Add amount placeholders.
     */
    public static void addAmountPlaceholders(Map<String, String> ph, double amount, NumberFormat fmt) {
        ph.put("amount", fmt.plain(amount));
        ph.put("amount_fmt", fmt.formatted(amount));
        ph.put("amount_short", fmt.shorthand(amount));
    }

    /**
     * Add fee placeholders.
     */
    public static void addFeePlaceholders(Map<String, String> ph, double fee, NumberFormat fmt) {
        ph.put("fee", fmt.plain(fee));
        ph.put("fee_fmt", fmt.formatted(fee));
    }
}