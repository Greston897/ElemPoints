package jar.elem.elempoints.plugin.command;

import jar.elem.elempoints.plugin.ElemPointsPlugin;
import jar.elem.elempoints.plugin.config.CurrencyConfig;
import jar.elem.elempoints.plugin.config.MessagesConfig;
import jar.elem.elempoints.plugin.currency.CurrencyImpl;
import jar.elem.elempoints.plugin.currency.CurrencyManager;
import jar.elem.elempoints.api.result.TransactionResult;
import jar.elem.elempoints.plugin.util.NumberFormat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public final class BalanceCommand implements CommandExecutor, TabCompleter {

    private final ElemPointsPlugin plugin;
    private final CurrencyImpl currency;
    private final CurrencyManager manager;
    private final MessagesConfig msgs;
    private final NumberFormat fmt;
    private final CurrencyConfig cfg;

    public BalanceCommand(ElemPointsPlugin plugin, CurrencyImpl currency,
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
        if (cfg.getPlayerPermission() != null && !sender.hasPermission(cfg.getPlayerPermission())) {
            msgs.send(sender, "error.no-permission", cfg.getMessageOverrides(), basePh());
            return true;
        }

        // No args: own balance
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cPlayers only.");
                return true;
            }
            Player p = (Player) sender;
            double bal = manager.getBalance(p.getUniqueId(), currency.getId());
            Map<String, String> ph = basePh();
            MessagesConfig.addBalancePlaceholders(ph, bal, fmt);
            msgs.send(sender, "balance.self", cfg.getMessageOverrides(), ph);
            return true;
        }

        String sub = args[0].toLowerCase();

        // /cmd help
        if (sub.equals("help")) {
            Map<String, String> ph = basePh();
            msgs.send(sender, "help.header", cfg.getMessageOverrides(), ph);
            msgs.send(sender, "help.balance", cfg.getMessageOverrides(), ph);
            msgs.send(sender, "help.balance-other", cfg.getMessageOverrides(), ph);
            if (cfg.isTransferEnabled()) {
                msgs.send(sender, "help.pay", cfg.getMessageOverrides(), ph);
            }
            return true;
        }

        // /cmd pay <player> <amount>
        if (sub.equals("pay") || sub.equals("transfer") || sub.equals("send")) {
            if (!(sender instanceof Player)) { sender.sendMessage("§cPlayers only."); return true; }
            Player p = (Player) sender;

            if (!cfg.isTransferEnabled()) {
                msgs.send(sender, "transfer.disabled", cfg.getMessageOverrides(), basePh());
                return true;
            }
            if (args.length < 3) {
                msgs.send(sender, "help.pay", cfg.getMessageOverrides(), basePh());
                return true;
            }

            OfflinePlayer target = resolvePlayer(args[1]);
            if (target == null) {
                Map<String, String> ph = basePh();
                ph.put("player", args[1]);
                msgs.send(sender, "error.player-not-found", cfg.getMessageOverrides(), ph);
                return true;
            }
            if (target.getUniqueId().equals(p.getUniqueId())) {
                msgs.send(sender, "error.self-transfer", cfg.getMessageOverrides(), basePh());
                return true;
            }

            double amount = parseAmount(sender, args[2]);
            if (amount < 0) return true;

            if (amount < cfg.getMinTransfer()) {
                Map<String, String> ph = basePh();
                MessagesConfig.addAmountPlaceholders(ph, cfg.getMinTransfer(), fmt);
                msgs.send(sender, "transfer.minimum", cfg.getMessageOverrides(), ph);
                return true;
            }

            TransactionResult result = manager.transfer(p.getUniqueId(), target.getUniqueId(),
                    currency.getId(), amount);

            if (result.isSuccess()) {
                double fee = amount * (cfg.getTransferFeePercent() / 100.0);
                String targetName = target.getName() != null ? target.getName() : args[1];

                Map<String, String> ph = basePh();
                ph.put("player", targetName);
                MessagesConfig.addAmountPlaceholders(ph, amount, fmt);
                MessagesConfig.addFeePlaceholders(ph, fee, fmt);
                msgs.send(sender, "transfer.sent", cfg.getMessageOverrides(), ph);

                if (fee > 0) {
                    msgs.send(sender, "transfer.fee", cfg.getMessageOverrides(), ph);
                }

                if (target.isOnline() && target.getPlayer() != null) {
                    Map<String, String> rph = basePh();
                    rph.put("player", p.getName());
                    MessagesConfig.addAmountPlaceholders(rph, amount, fmt);
                    msgs.send(target.getPlayer(), "transfer.received", cfg.getMessageOverrides(), rph);
                }
            } else {
                Map<String, String> ph = basePh();
                ph.put("player", args[1]);
                MessagesConfig.addAmountPlaceholders(ph, amount, fmt);
                double bal = manager.getBalance(p.getUniqueId(), currency.getId());
                MessagesConfig.addBalancePlaceholders(ph, bal, fmt);
                msgs.send(sender, "error.not-enough", cfg.getMessageOverrides(), ph);
            }
            return true;
        }

        // /cmd <player> — check other's balance
        OfflinePlayer target = resolvePlayer(args[0]);
        if (target == null) {
            Map<String, String> ph = basePh();
            ph.put("player", args[0]);
            msgs.send(sender, "error.player-not-found", cfg.getMessageOverrides(), ph);
            return true;
        }

        double bal = manager.getBalance(target.getUniqueId(), currency.getId());
        Map<String, String> ph = basePh();
        ph.put("player", target.getName() != null ? target.getName() : args[0]);
        MessagesConfig.addBalancePlaceholders(ph, bal, fmt);
        msgs.send(sender, "balance.other", cfg.getMessageOverrides(), ph);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        if (args.length == 1) {
            List<String> opts = new ArrayList<>(Arrays.asList("help"));
            if (cfg.isTransferEnabled()) opts.add("pay");
            Bukkit.getOnlinePlayers().forEach(p -> opts.add(p.getName()));
            return filter(opts, args[0]);
        }
        if (args.length == 2 && isPaySub(args[0])) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .collect(Collectors.toList()), args[1]);
        }
        if (args.length == 3 && isPaySub(args[0])) {
            return Arrays.asList("100", "500", "1000");
        }
        return Collections.emptyList();
    }

    private boolean isPaySub(String s) {
        return s.equalsIgnoreCase("pay") || s.equalsIgnoreCase("transfer") || s.equalsIgnoreCase("send");
    }

    @SuppressWarnings("deprecation")
    private OfflinePlayer resolvePlayer(String name) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(name);
        return (p.hasPlayedBefore() || p.isOnline()) ? p : null;
    }

    private double parseAmount(CommandSender sender, String input) {
        try {
            double v = Double.parseDouble(input);
            if (v <= 0) throw new NumberFormatException();
            return v;
        } catch (NumberFormatException e) {
            Map<String, String> ph = basePh();
            ph.put("input", input);
            msgs.send(sender, "error.invalid-amount", cfg.getMessageOverrides(), ph);
            return -1;
        }
    }

    private List<String> filter(List<String> list, String prefix) {
        String low = prefix.toLowerCase();
        return list.stream().filter(s -> s.toLowerCase().startsWith(low)).collect(Collectors.toList());
    }
}