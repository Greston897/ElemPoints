package jar.elem.elempoints.plugin.hook;

import jar.elem.elempoints.plugin.ElemPointsPlugin;
import jar.elem.elempoints.plugin.currency.CurrencyImpl;
import jar.elem.elempoints.plugin.currency.CurrencyManager;
import jar.elem.elempoints.plugin.util.NumberFormat;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

/**
 * PlaceholderAPI expansion for ElemPoints.
 *
 * Supported placeholders:
 *   %elempoints_points%                    — primary balance (plain)
 *   %elempoints_points_formatted%          — primary balance (1,337)
 *   %elempoints_points_shorthand%          — primary balance (1.3k)
 *   %elempoints_{id}_points%               — specific currency balance
 *   %elempoints_{id}_points_formatted%     — specific currency formatted
 *   %elempoints_{id}_points_shorthand%     — specific currency shorthand
 *   %elempoints_{id}_symbol%               — currency symbol
 *   %elempoints_{id}_name%                 — currency display name
 */
public final class PAPIExpansion extends PlaceholderExpansion {

    private final ElemPointsPlugin plugin;
    private final CurrencyManager manager;
    private final NumberFormat fmt;

    public PAPIExpansion(ElemPointsPlugin plugin, CurrencyManager manager, NumberFormat fmt) {
        this.plugin = plugin;
        this.manager = manager;
        this.fmt = fmt;
    }

    @Override public String getIdentifier() { return "elempoints"; }
    @Override public String getAuthor() { return String.join(", ", plugin.getDescription().getAuthors()); }
    @Override public String getVersion() { return plugin.getDescription().getVersion(); }
    @Override public boolean persist() { return true; }
    @Override public boolean canRegister() { return true; }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) return "0";

        String lower = params.toLowerCase();

        // Primary currency shortcuts
        if (lower.equals("points")) {
            return primaryBalance(player, "plain");
        }
        if (lower.equals("points_formatted")) {
            return primaryBalance(player, "formatted");
        }
        if (lower.equals("points_shorthand")) {
            return primaryBalance(player, "shorthand");
        }

        // Per-currency: {currencyId}_{type}
        for (CurrencyImpl cur : manager.getRegistry().all()) {
            String prefix = cur.getId().toLowerCase() + "_";
            if (lower.startsWith(prefix)) {
                String suffix = lower.substring(prefix.length());
                return handleCurrency(player, cur, suffix);
            }
        }

        return null;
    }

    private String primaryBalance(OfflinePlayer player, String type) {
        CurrencyImpl primary = manager.getRegistry().getPrimary();
        if (primary == null) return "0";
        return handleCurrency(player, primary, "points" + (type.equals("plain") ? "" : "_" + type));
    }

    private String handleCurrency(OfflinePlayer player, CurrencyImpl cur, String suffix) {
        switch (suffix) {
            case "points":
                return fmt.plain(manager.getBalance(player.getUniqueId(), cur.getId()));
            case "points_formatted":
                return fmt.formatted(manager.getBalance(player.getUniqueId(), cur.getId()));
            case "points_shorthand":
                return fmt.shorthand(manager.getBalance(player.getUniqueId(), cur.getId()));
            case "symbol":
                return cur.getSymbol();
            case "name":
                return cur.getDisplayName();
            case "singular":
                return cur.getSingular();
            case "plural":
                return cur.getPlural();
            default:
                return null;
        }
    }
}