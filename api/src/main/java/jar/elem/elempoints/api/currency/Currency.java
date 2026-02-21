package jar.elem.elempoints.api.currency;

/**
 * Represents a registered currency in ElemPoints.
 * Each currency has a unique identifier, display properties, and behaviour settings.
 *
 * @since 2.0.0
 */
public interface Currency {

    /**
     * Returns the unique string identifier of this currency.
     * This is the key used in all API calls and config files.
     * Example: "points", "crystals", "tokens"
     *
     * @return non-null currency identifier
     */
    String getId();

    /**
     * Returns the display name with colour codes.
     * Example: "&6Donate Points"
     *
     * @return display name
     */
    String getDisplayName();

    /**
     * Returns the singular form.
     * Example: "point"
     *
     * @return singular name
     */
    String getSingular();

    /**
     * Returns the plural form.
     * Example: "points"
     *
     * @return plural name
     */
    String getPlural();

    /**
     * Returns the symbol or emoji prefix.
     * Example: "⭐", "$", "💎"
     *
     * @return symbol string
     */
    String getSymbol();

    /**
     * Returns the default starting balance for new accounts.
     *
     * @return default balance
     */
    double getDefaultBalance();

    /**
     * Returns true if this is the primary currency.
     * The primary currency is used for Vault integration.
     *
     * @return true if primary
     */
    boolean isPrimary();

    /**
     * Returns the maximum allowed balance, or 0 for unlimited.
     *
     * @return max balance
     */
    double getMaxBalance();

    /**
     * Returns true if player-to-player transfers are allowed.
     *
     * @return true if transfers enabled
     */
    boolean isTransferEnabled();

    /**
     * Returns the transfer fee percentage (0-100).
     *
     * @return fee percentage
     */
    double getTransferFee();

    /**
     * Returns the minimum transfer amount.
     *
     * @return minimum transfer
     */
    double getMinTransfer();

    /**
     * Returns true if this currency is currently enabled.
     *
     * @return true if enabled
     */
    boolean isEnabled();
}