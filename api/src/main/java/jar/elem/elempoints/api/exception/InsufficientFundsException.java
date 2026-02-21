package jar.elem.elempoints.api.exception;

import java.util.UUID;

/**
 * Thrown when a player does not have enough balance for an operation.
 *
 * @since 2.0.0
 */
public class InsufficientFundsException extends RuntimeException {

    private final UUID player;
    private final String currencyId;
    private final double required;
    private final double available;

    public InsufficientFundsException(UUID player, String currencyId, double required, double available) {
        super("Player " + player + " has insufficient " + currencyId +
                ": required=" + required + ", available=" + available);
        this.player = player;
        this.currencyId = currencyId;
        this.required = required;
        this.available = available;
    }

    public UUID getPlayer() { return player; }
    public String getCurrencyId() { return currencyId; }
    public double getRequired() { return required; }
    public double getAvailable() { return available; }
}