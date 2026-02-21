package jar.elem.elempoints.api.currency;

import java.util.UUID;

/**
 * Represents a player's account for a specific currency.
 *
 * @since 2.0.0
 */
public interface CurrencyAccount {

    /**
     * @return the player's UUID
     */
    UUID getOwner();

    /**
     * @return the associated currency
     */
    Currency getCurrency();

    /**
     * @return the current balance
     */
    double getBalance();
}