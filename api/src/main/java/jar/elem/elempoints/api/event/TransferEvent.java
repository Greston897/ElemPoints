package jar.elem.elempoints.api.event;

import jar.elem.elempoints.api.currency.Currency;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Fired when a player-to-player transfer is about to occur.
 *
 * @since 2.0.0
 */
public class TransferEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID sender;
    private final UUID receiver;
    private final Currency currency;
    private double amount;
    private double fee;
    private boolean cancelled;

    public TransferEvent(UUID sender, UUID receiver, Currency currency, double amount, double fee) {
        super(true);
        this.sender = sender;
        this.receiver = receiver;
        this.currency = currency;
        this.amount = amount;
        this.fee = fee;
    }

    public UUID getSender() { return sender; }
    public UUID getReceiver() { return receiver; }
    public Currency getCurrency() { return currency; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }
    public double getTotalCost() { return amount + fee; }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}