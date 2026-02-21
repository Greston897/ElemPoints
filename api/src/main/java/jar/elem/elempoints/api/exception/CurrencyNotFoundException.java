package jar.elem.elempoints.api.exception;

/**
 * Thrown when a currency ID is not found in the registry.
 *
 * @since 2.0.0
 */
public class CurrencyNotFoundException extends RuntimeException {

    private final String currencyId;

    public CurrencyNotFoundException(String currencyId) {
        super("Currency not found: '" + currencyId + "'. " +
                "Make sure this currency is defined in ElemPoints and enabled in config.yml");
        this.currencyId = currencyId;
    }

    /**
     * @return the currency ID that was not found
     */
    public String getCurrencyId() {
        return currencyId;
    }
}