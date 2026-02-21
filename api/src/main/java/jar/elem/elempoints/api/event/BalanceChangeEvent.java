package jar.elem.elempoints.api.event;

import jar.elem.elempoints.api.currency.Currency;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Fired when a player's balance is about to change.
 * Can be cancelled to prevent the change.
 *
 * @since 2.0.0
 */
public class BalanceChangeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    public enum Reason {
        ADMIN_COMMAND,
        PLAYER_TRANSFER,
        API_CALL,
        VAULT,
        EXPORT,
        PLUGIN
    }

    private final UUID player;
    private final Currency currency;
    private final double oldBalance;
    private double newBalance;
    private final Reason reason;
    private final String source;
    private boolean cancelled;

    public BalanceChangeEvent(UUID player, Currency currency, double oldBalance,
                              double newBalance, Reason reason, String source) {
        super(true); // async safe
        this.player = player;
        this.currency = currency;
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
        this.reason = reason;
        this.source = source;
    }

    public UUID getPlayer() { return player; }
    public Currency getCurrency() { return currency; }
    public double getOldBalance() { return oldBalance; }
    public double getNewBalance() { return newBalance; }
    public void setNewBalance(double newBalance) { this.newBalance = newBalance; }
    public Reason getReason() { return reason; }

    /**
     * Returns the source plugin or command that triggered the change.
     *
     * @return source identifier
     */
    public String getSource() { return source; }

    public double getDelta() { return newBalance - oldBalance; }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}