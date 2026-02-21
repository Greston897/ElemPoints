package jar.elem.elempoints.api.event;

import jar.elem.elempoints.api.currency.Currency;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired after a currency is registered in the system.
 *
 * @since 2.0.0
 */
public class CurrencyRegisterEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Currency currency;

    public CurrencyRegisterEvent(Currency currency) {
        this.currency = currency;
    }

    public Currency getCurrency() { return currency; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}