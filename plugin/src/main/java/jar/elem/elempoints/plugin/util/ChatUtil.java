package jar.elem.elempoints.plugin.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Chat message utilities with legacy and hex color support (1.16+).
 */
public final class ChatUtil {

    // Pattern for hex colors: &#RRGGBB or &#RGB
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private ChatUtil() {}

    /**
     * Colorize a message supporting both &-codes and &#RRGGBB hex codes.
     */
    public static String colorize(String message) {
        if (message == null || message.isEmpty()) return "";

        // Hex colors (1.16+)
        if (VersionUtil.isAtLeast(16)) {
            Matcher matcher = HEX_PATTERN.matcher(message);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String hex = matcher.group(1);
                StringBuilder replacement = new StringBuilder("§x");
                for (char c : hex.toCharArray()) {
                    replacement.append('§').append(c);
                }
                matcher.appendReplacement(sb, replacement.toString());
            }
            matcher.appendTail(sb);
            message = sb.toString();
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Replace placeholders in a message.
     */
    public static String replacePlaceholders(String message, Map<String, String> placeholders) {
        if (message == null) return "";
        if (placeholders == null || placeholders.isEmpty()) return message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return message;
    }

    /**
     * Send a colorized message to a sender.
     */
    public static void send(CommandSender sender, String message) {
        if (sender != null && message != null && !message.isEmpty()) {
            sender.sendMessage(colorize(message));
        }
    }

    /**
     * Send a message with placeholder replacement.
     */
    public static void send(CommandSender sender, String message, Map<String, String> placeholders) {
        send(sender, replacePlaceholders(message, placeholders));
    }

    /**
     * Strip all color codes from a message.
     */
    public static String stripColor(String message) {
        return ChatColor.stripColor(colorize(message));
    }
}
