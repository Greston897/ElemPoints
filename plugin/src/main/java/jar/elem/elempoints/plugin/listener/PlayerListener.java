package jar.elem.elempoints.plugin.listener;

import jar.elem.elempoints.plugin.ElemPointsPlugin;
import jar.elem.elempoints.plugin.currency.CurrencyManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerListener implements Listener {

    private final ElemPointsPlugin plugin;
    private final CurrencyManager manager;

    public PlayerListener(ElemPointsPlugin plugin, CurrencyManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin,
                () -> manager.loadPlayer(e.getPlayer().getUniqueId()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin,
                () -> manager.unloadPlayer(e.getPlayer().getUniqueId()));
    }
}