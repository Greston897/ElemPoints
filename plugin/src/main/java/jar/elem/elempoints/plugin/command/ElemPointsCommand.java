package jar.elem.elempoints.plugin.command;

import jar.elem.elempoints.plugin.ElemPointsPlugin;
import jar.elem.elempoints.plugin.config.MessagesConfig;
import jar.elem.elempoints.plugin.currency.CurrencyImpl;
import jar.elem.elempoints.plugin.currency.CurrencyManager;
import jar.elem.elempoints.plugin.util.NumberFormat;
import org.bukkit.command.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * /elempoints — main plugin command.
 */
public final class ElemPointsCommand implements CommandExecutor, TabCompleter {

    private final ElemPointsPlugin plugin;
    private final CurrencyManager manager;
    private final MessagesConfig msgs;
    private final NumberFormat fmt;

    public ElemPointsCommand(ElemPointsPlugin plugin, CurrencyManager manager,
                             MessagesConfig msgs, NumberFormat fmt) {
        this.plugin = plugin;
        this.manager = manager;
        this.msgs = msgs;
        this.fmt = fmt;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§6ElemPoints §7v" + plugin.getDescription().getVersion());
            sender.sendMessage("§eCurrencies: §f" + manager.getRegistry().size());
            for (CurrencyImpl c : manager.getRegistry().all()) {
                sender.sendMessage("  §7- §e" + c.getId() +
                        (c.isPrimary() ? " §a[PRIMARY]" : "") +
                        " §7(/" + c.getConfig().getPlayerCommand() + ")");
            }
            sender.sendMessage("§fUse /elempoints reload to reload.");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("elempoints.reload") && !sender.hasPermission("elempoints.admin")) {
                sender.sendMessage("§cNo permission.");
                return true;
            }
            plugin.performReload();
            Map<String, String> ph = new HashMap<>();
            ph.put("count", String.valueOf(manager.getRegistry().size()));
            msgs.send(sender, "system.reload", null, ph);
            return true;
        }

        sender.sendMessage("§fUsage: /elempoints [reload]");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("reload").stream()
                    .filter(x -> x.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}