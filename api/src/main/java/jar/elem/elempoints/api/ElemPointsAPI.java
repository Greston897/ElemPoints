package jar.elem.elempoints.api;

import jar.elem.elempoints.api.currency.Currency;
import jar.elem.elempoints.api.result.TransactionResult;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Core API for interacting with the ElemPoints economy system.
 * <p>
 * Obtain an instance via {@link ElemPointsProvider#get()}.
 * <p>
 * All methods that take a {@code currencyId} will throw
 * {@link jar.elem.elempoints.api.exception.CurrencyNotFoundException}
 * if the currency is not registered. Use {@link #hasCurrency(String)}
 * to check first, or use {@link #getCurrency(String)} which returns Optional.
 * <p>
 * Example usage:
 * <pre>{@code
 * ElemPointsAPI api = ElemPointsProvider.get();
 *
 * // Check balance
 * double bal = api.getBalance(player.getUniqueId(), "points");
 *
 * // Deposit
 * TransactionResult result = api.deposit(player.getUniqueId(), "points", 100);
 * if (result.isSuccess()) {
 *     player.sendMessage("Deposited!");
 * }
 *
 * // Withdraw with safety
 * if (api.has(player.getUniqueId(), "crystals", 50)) {
 *     api.withdraw(player.getUniqueId(), "crystals", 50);
 * }
 * }</pre>
 *
 * @since 2.0.0
 */
public interface ElemPointsAPI {

    // ========================= Currency Registry =========================

    /**
     * Returns all registered and enabled currencies.
     *
     * @return unmodifiable collection of currencies
     */
    Collection<jar.elem.elempoints.api.currency.Currency> getCurrencies();

    /**
     * Looks up a currency by its unique ID.
     *
     * @param currencyId the currency identifier
     * @return optional containing the currency, or empty if not found
     */
    Optional<Currency> getCurrency(String currencyId);

    /**
     * Returns the primary currency used for Vault integration.
     * There is always exactly one primary currency.
     *
     * @return the primary currency
     */
    Currency getPrimaryCurrency();

    /**
     * Checks whether a currency with the given ID exists and is enabled.
     *
     * @param currencyId the currency identifier
     * @return true if exists and enabled
     */
    boolean hasCurrency(String currencyId);

    // ========================= Balance (by currency ID) =========================

    /**
     * Gets the balance for a player in the specified currency.
     * Creates a default account if one doesn't exist.
     *
     * @param player     player UUID
     * @param currencyId currency identifier
     * @return current balance
     * @throws jar.elem.elempoints.api.exception.CurrencyNotFoundException if currency not found
     */
    double getBalance(UUID player, String currencyId);

    /**
     * Sets the exact balance for a player.
     *
     * @param player     player UUID
     * @param currencyId currency identifier
     * @param amount     new balance (must be >= 0)
     * @return transaction result
     */
    TransactionResult setBalance(UUID player, String currencyId, double amount);

    /**
     * Adds to a player's balance.
     *
     * @param player     player UUID
     * @param currencyId currency identifier
     * @param amount     amount to add (must be > 0)
     * @return transaction result
     */
    TransactionResult deposit(UUID player, String currencyId, double amount);

    /**
     * Removes from a player's balance. Fails if insufficient funds.
     *
     * @param player     player UUID
     * @param currencyId currency identifier
     * @param amount     amount to remove (must be > 0)
     * @return transaction result
     */
    TransactionResult withdraw(UUID player, String currencyId, double amount);

    /**
     * Checks if a player has at least the specified amount.
     *
     * @param player     player UUID
     * @param currencyId currency identifier
     * @param amount     amount to check
     * @return true if balance >= amount
     */
    boolean has(UUID player, String currencyId, double amount);

    // ========================= Balance (primary currency shortcuts) =========================

    /**
     * Gets the balance in the primary currency.
     */
    double getBalance(UUID player);

    /**
     * Deposits to the primary currency.
     */
    TransactionResult deposit(UUID player, double amount);

    /**
     * Withdraws from the primary currency.
     */
    TransactionResult withdraw(UUID player, double amount);

    /**
     * Checks if a player has enough of the primary currency.
     */
    boolean has(UUID player, double amount);

    // ========================= Transfers =========================

    /**
     * Transfers currency between two players.
     * Applies transfer fees as configured.
     *
     * @param from       sender UUID
     * @param to         receiver UUID
     * @param currencyId currency identifier
     * @param amount     amount to transfer
     * @return transaction result
     */
    TransactionResult transfer(UUID from, UUID to, String currencyId, double amount);

    // ========================= Async Operations =========================

    /**
     * Asynchronously gets a player's balance.
     */
    CompletableFuture<Double> getBalanceAsync(UUID player, String currencyId);

    /**
     * Asynchronously deposits to a player's account.
     */
    CompletableFuture<TransactionResult> depositAsync(UUID player, String currencyId, double amount);

    /**
     * Asynchronously withdraws from a player's account.
     */
    CompletableFuture<TransactionResult> withdrawAsync(UUID player, String currencyId, double amount);

    /**
     * Asynchronously sets a player's balance.
     */
    CompletableFuture<TransactionResult> setBalanceAsync(UUID player, String currencyId, double amount);

    // ========================= Data Export =========================

    /**
     * Exports/converts all balances from one currency to another.
     * Existing target balances are ADDED to (not replaced).
     *
     * @param fromCurrencyId source currency
     * @param toCurrencyId   target currency
     * @param rate           conversion rate (1 source = rate target)
     * @return future with the number of accounts converted
     */
    CompletableFuture<Integer> exportData(String fromCurrencyId, String toCurrencyId, double rate);

    // ========================= Utility =========================

    /**
     * Returns true when the plugin is fully loaded and ready for API calls.
     */
    boolean isReady();

    /**
     * Returns the plugin version string.
     */
    String getVersion();

    /**
     * Returns a logger that third-party plugins can use to log messages
     * prefixed with [ElemPoints].
     *
     * @return the ElemPoints logger
     */
    Logger getLogger();

    /**
     * Logs a warning that a currency was not found.
     * Intended for use by third-party plugins to provide clear error messages.
     * <p>
     * Format: [ElemPoints] [YourPlugin] Currency 'xxx' not found in registry!
     *
     * @param callingPlugin name of the plugin that made the call
     * @param currencyId    the currency that was not found
     */
    void logCurrencyNotFound(String callingPlugin, String currencyId);
}