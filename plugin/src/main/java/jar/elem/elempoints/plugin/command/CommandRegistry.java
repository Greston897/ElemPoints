package jar.elem.elempoints.plugin.command;

import jar.elem.elempoints.plugin.ElemPointsPlugin;
import jar.elem.elempoints.plugin.config.CurrencyConfig;
import jar.elem.elempoints.plugin.config.MessagesConfig;
import jar.elem.elempoints.plugin.currency.CurrencyImpl;
import jar.elem.elempoints.plugin.currency.CurrencyManager;
import jar.elem.elempoints.plugin.util.NumberFormat;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Dynamically registers and unregisters commands at runtime.
 * Works on 1.16-1.21+ without depending on plugin.yml command entries.
 */
public final class CommandRegistry {

    private final ElemPointsPlugin plugin;
    private final CurrencyManager currencyManager;
    private final MessagesConfig messages;
    private final NumberFormat numberFormat;
    private final List<String> registered = new ArrayList<>();

    public CommandRegistry(ElemPointsPlugin plugin, CurrencyManager currencyManager,
                           MessagesConfig messages, NumberFormat numberFormat) {
        this.plugin = plugin;
        this.currencyManager = currencyManager;
        this.messages = messages;
        this.numberFormat = numberFormat;
    }

    public void registerAll() {
        for (CurrencyImpl cur : currencyManager.getRegistry().all()) {
            CurrencyConfig cfg = cur.getConfig();

            // Player command
            BalanceCommand balCmd = new BalanceCommand(plugin, cur, currencyManager, messages, numberFormat);
            register(cfg.getPlayerCommand(), cfg.getPlayerAliases(),
                    cfg.getDisplayName() + " balance", balCmd, balCmd);

            // Admin command
            AdminCommand admCmd = new AdminCommand(plugin, cur, currencyManager, messages, numberFormat);
            register(cfg.getAdminCommand(), cfg.getAdminAliases(),
                    cfg.getDisplayName() + " admin", admCmd, admCmd);
        }

        // Global /elempoints command
        ElemPointsCommand mainCmd = new ElemPointsCommand(plugin, currencyManager, messages, numberFormat);
        register("elempoints", Arrays.asList("ep", "epoints"),
                "ElemPoints main command", mainCmd, mainCmd);
    }

    public void unregisterAll() {
        try {
            CommandMap commandMap = getCommandMap();
            Field knownField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, org.bukkit.command.Command> known =
                    (Map<String, org.bukkit.command.Command>) knownField.get(commandMap);

            for (String name : registered) {
                known.remove(name);
                known.remove("elempoints:" + name);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[Commands] Failed to unregister: " + e.getMessage());
        }
        registered.clear();
    }

    private void register(String name, List<String> aliases, String desc,
                          org.bukkit.command.CommandExecutor executor,
                          org.bukkit.command.TabCompleter tab) {
        try {
            PluginCommand cmd = createCommand(name);
            if (cmd == null) {
                plugin.getLogger().warning("[Commands] Could not create: /" + name);
                return;
            }
            cmd.setDescription(desc);
            cmd.setAliases(aliases != null ? aliases : Collections.emptyList());
            cmd.setExecutor(executor);
            cmd.setTabCompleter(tab);

            getCommandMap().register("elempoints", cmd);

            registered.add(name);
            if (aliases != null) registered.addAll(aliases);

            if (plugin.getConfig().getBoolean("general.debug", false)) {
                plugin.getLogger().info("[Commands] Registered: /" + name +
                        (aliases != null && !aliases.isEmpty() ? " (" + String.join(",", aliases) + ")" : ""));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[Commands] Register failed for /" + name + ": " + e.getMessage());
        }
    }

    private PluginCommand createCommand(String name) {
        try {
            Constructor<PluginCommand> ctr = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            ctr.setAccessible(true);
            return ctr.newInstance(name, plugin);
        } catch (Exception e) {
            return null;
        }
    }

    private CommandMap getCommandMap() throws Exception {
        Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        f.setAccessible(true);
        return (CommandMap) f.get(Bukkit.getServer());
    }
}