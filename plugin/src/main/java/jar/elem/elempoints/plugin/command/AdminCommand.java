package jar.elem.elempoints.plugin.command;

import jar.elem.elempoints.plugin.ElemPointsPlugin;
import jar.elem.elempoints.plugin.config.CurrencyConfig;
import jar.elem.elempoints.plugin.config.MessagesConfig;
import jar.elem.elempoints.plugin.currency.CurrencyImpl;
import jar.elem.elempoints.plugin.currency.CurrencyManager;
import jar.elem.elempoints.api.event.BalanceChangeEvent;
import jar.elem.elempoints.api.result.TransactionResult;
import jar.elem.elempoints.plugin.util.NumberFormat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public final class AdminCommand implements CommandExecutor, TabCompleter {

    private final ElemPointsPlugin plugin;
    private final CurrencyImpl currency;
    private final CurrencyManager manager;
    private final MessagesConfig msgs;
    private final NumberFormat fmt;
    private final CurrencyConfig cfg;

    public AdminCommand(ElemPointsPlugin plugin, CurrencyImpl currency,
                        CurrencyManager manager, MessagesConfig msgs, NumberFormat fmt) {
        this.plugin = plugin;
        this.currency = currency;
        this.manager = manager;
        this.msgs = msgs;
        this.fmt = fmt;
        this.cfg = currency.getConfig();
    }

    private Map<String, String> basePh() {
        return MessagesConfig.currencyPlaceholders(cfg, fmt);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!hasPerm(sender)) {
            msgs.send(sender, "error.no-permission", cfg.getMessageOverrides(), basePh());
            return true;
        }

        if (args.length == 0) { showHelp(sender); return true; }

        switch (args[0].toLowerCase()) {
            case "give": case "add":    doGive(sender, args); break;
            case "take": case "remove": doTake(sender, args); break;
            case "set":                 doSet(sender, args);  break;
            case "reset":               doReset(sender, args); break;
            case "check": case "look":  doCheck(sender, args); break;
            case "export":              doExport(sender, args); break;
            default: showHelp(sender);
        }
        return true;
    }

    private boolean hasPerm(CommandSender s) {
        return s.hasPermission(cfg.getAdminPermission()) || s.hasPermission("elempoints.admin");
    }

    private void doGive(CommandSender sender, String[] args) {
        if (args.length < 3) { msgs.send(sender, "help.admin-give", cfg.getMessageOverrides(), basePh()); return; }
        OfflinePlayer target = resolve(sender, args[1]); if (target == null) return;
        double amount = parseAmt(sender, args[2]); if (amount < 0) return;

        TransactionResult r = manager.deposit(target.getUniqueId(), currency.getId(), amount,
                BalanceChangeEvent.Reason.ADMIN_COMMAND, "admin:" + sender.getName());

        String name = target.getName() != null ? target.getName() : args[1];
        Map<String, String> ph = basePh();
        ph.put("player", name);
        MessagesConfig.addAmountPlaceholders(ph, amount, fmt);

        if (r.isSuccess()) {
            msgs.send(sender, "admin.give.sender", cfg.getMessageOverrides(), ph);
            if (target.isOnline() && target.getPlayer() != null) {
                msgs.send(target.getPlayer(), "admin.give.receiver", cfg.getMessageOverrides(), ph);
            }
        }
    }

    private void doTake(CommandSender sender, String[] args) {
        if (args.length < 3) { msgs.send(sender, "help.admin-take", cfg.getMessageOverrides(), basePh()); return; }
        OfflinePlayer target = resolve(sender, args[1]); if (target == null) return;
        double amount = parseAmt(sender, args[2]); if (amount < 0) return;

        TransactionResult r = manager.withdraw(target.getUniqueId(), currency.getId(), amount,
                BalanceChangeEvent.Reason.ADMIN_COMMAND, "admin:" + sender.getName());

        String name = target.getName() != null ? target.getName() : args[1];
        Map<String, String> ph = basePh();
        ph.put("player", name);
        MessagesConfig.addAmountPlaceholders(ph, amount, fmt);

        if (r.isSuccess()) {
            msgs.send(sender, "admin.take.sender", cfg.getMessageOverrides(), ph);
            if (target.isOnline() && target.getPlayer() != null) {
                msgs.send(target.getPlayer(), "admin.take.receiver", cfg.getMessageOverrides(), ph);
            }
        } else {
            double bal = manager.getBalance(target.getUniqueId(), currency.getId());
            MessagesConfig.addBalancePlaceholders(ph, bal, fmt);
            msgs.send(sender, "error.not-enough", cfg.getMessageOverrides(), ph);
        }
    }

    private void doSet(CommandSender sender, String[] args) {
        if (args.length < 3) { msgs.send(sender, "help.admin-set", cfg.getMessageOverrides(), basePh()); return; }
        OfflinePlayer target = resolve(sender, args[1]); if (target == null) return;
        double amount;
        try { amount = Double.parseDouble(args[2]); if (amount < 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { Map<String, String> ph = basePh(); ph.put("input", args[2]);
            msgs.send(sender, "error.invalid-amount", cfg.getMessageOverrides(), ph); return; }

        manager.setBalance(target.getUniqueId(), currency.getId(), amount,
                BalanceChangeEvent.Reason.ADMIN_COMMAND, "admin:" + sender.getName());

        String name = target.getName() != null ? target.getName() : args[1];
        Map<String, String> ph = basePh();
        ph.put("player", name);
        MessagesConfig.addAmountPlaceholders(ph, amount, fmt);
        msgs.send(sender, "admin.set.sender", cfg.getMessageOverrides(), ph);
        if (target.isOnline() && target.getPlayer() != null) {
            msgs.send(target.getPlayer(), "admin.set.receiver", cfg.getMessageOverrides(), ph);
        }
    }

    private void doReset(CommandSender sender, String[] args) {
        if (args.length < 2) { msgs.send(sender, "help.admin-reset", cfg.getMessageOverrides(), basePh()); return; }
        OfflinePlayer target = resolve(sender, args[1]); if (target == null) return;

        manager.setBalance(target.getUniqueId(), currency.getId(), cfg.getDefaultBalance(),
                BalanceChangeEvent.Reason.ADMIN_COMMAND, "admin:reset");

        String name = target.getName() != null ? target.getName() : args[1];
        Map<String, String> ph = basePh();
        ph.put("player", name);
        msgs.send(sender, "admin.reset.sender", cfg.getMessageOverrides(), ph);
    }

    private void doCheck(CommandSender sender, String[] args) {
        if (args.length < 2) { msgs.send(sender, "help.balance-other", cfg.getMessageOverrides(), basePh()); return; }
        OfflinePlayer target = resolve(sender, args[1]); if (target == null) return;
        double bal = manager.getBalance(target.getUniqueId(), currency.getId());
        String name = target.getName() != null ? target.getName() : args[1];
        Map<String, String> ph = basePh();
        ph.put("player", name);
        MessagesConfig.addBalancePlaceholders(ph, bal, fmt);
        msgs.send(sender, "balance.other", cfg.getMessageOverrides(), ph);
    }

    private void doExport(CommandSender sender, String[] args) {
        if (args.length < 3) { msgs.send(sender, "help.admin-export", cfg.getMessageOverrides(), basePh()); return; }
        String toId = args[1];
        if (!manager.getRegistry().has(toId)) {
            Map<String, String> ph = basePh(); ph.put("input", toId);
            msgs.send(sender, "error.currency-not-found", cfg.getMessageOverrides(), ph);
            return;
        }
        double rate;
        try { rate = Double.parseDouble(args[2]); }
        catch (NumberFormatException e) { Map<String, String> ph = basePh(); ph.put("input", args[2]);
            msgs.send(sender, "error.invalid-amount", cfg.getMessageOverrides(), ph); return; }

        Map<String, String> ph = basePh();
        ph.put("from", currency.getId()); ph.put("to", toId); ph.put("rate", String.valueOf(rate));
        msgs.send(sender, "system.export-start", cfg.getMessageOverrides(), ph);

        manager.exportData(currency.getId(), toId, rate).thenAccept(count -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                Map<String, String> dph = basePh();
                dph.put("count", String.valueOf(count));
                msgs.send(sender, "system.export-done", cfg.getMessageOverrides(), dph);
            });
        }).exceptionally(t -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                Map<String, String> eph = basePh();
                eph.put("error", t.getMessage());
                msgs.send(sender, "system.export-fail", cfg.getMessageOverrides(), eph);
            });
            return null;
        });
    }

    private void showHelp(CommandSender sender) {
        Map<String, String> ph = basePh();
        msgs.send(sender, "help.admin-header", cfg.getMessageOverrides(), ph);
        msgs.send(sender, "help.admin-give", cfg.getMessageOverrides(), ph);
        msgs.send(sender, "help.admin-take", cfg.getMessageOverrides(), ph);
        msgs.send(sender, "help.admin-set", cfg.getMessageOverrides(), ph);
        msgs.send(sender, "help.admin-reset", cfg.getMessageOverrides(), ph);
        msgs.send(sender, "help.admin-export", cfg.getMessageOverrides(), ph);
        msgs.send(sender, "help.admin-reload", cfg.getMessageOverrides(), ph);
    }

    @SuppressWarnings("deprecation")
    private OfflinePlayer resolve(CommandSender sender, String name) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(name);
        if (!p.hasPlayedBefore() && !p.isOnline()) {
            Map<String, String> ph = basePh(); ph.put("player", name);
            msgs.send(sender, "error.player-not-found", cfg.getMessageOverrides(), ph);
            return null;
        }
        return p;
    }

    private double parseAmt(CommandSender sender, String input) {
        try { double v = Double.parseDouble(input); if (v <= 0) throw new NumberFormatException(); return v; }
        catch (NumberFormatException e) { Map<String, String> ph = basePh(); ph.put("input", input);
            msgs.send(sender, "error.invalid-amount", cfg.getMessageOverrides(), ph); return -1; }
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("give","take","set","reset","check","export"), args[0]);
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("export")) {
                return filter(manager.getRegistry().all().stream()
                        .map(CurrencyImpl::getId)
                        .filter(id -> !id.equals(currency.getId()))
                        .collect(Collectors.toList()), args[1]);
            }
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .collect(Collectors.toList()), args[1]);
        }
        if (args.length == 3) return Arrays.asList("100","500","1000","5000");
        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String prefix) {
        String low = prefix.toLowerCase();
        return list.stream().filter(s -> s.toLowerCase().startsWith(low)).collect(Collectors.toList());
    }
}