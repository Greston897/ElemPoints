package jar.elem.elempoints.plugin.storage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for database backends.
 */
public interface StorageProvider {

    void init();
    void shutdown();
    boolean isConnected();

    CompletableFuture<Double> getBalance(UUID player, String currencyId);
    CompletableFuture<Void> setBalance(UUID player, String currencyId, double amount);
    CompletableFuture<Boolean> hasAccount(UUID player, String currencyId);
    CompletableFuture<Void> createAccount(UUID player, String currencyId, double defaultBalance);
    CompletableFuture<Map<UUID, Double>> getAllBalances(String currencyId);
    CompletableFuture<Void> bulkSetBalances(String currencyId, Map<UUID, Double> balances);
}