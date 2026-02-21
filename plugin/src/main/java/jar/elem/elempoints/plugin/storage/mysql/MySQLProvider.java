package jar.elem.elempoints.plugin.storage.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jar.elem.elempoints.plugin.ElemPointsPlugin;
import jar.elem.elempoints.plugin.storage.StorageProvider;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public final class MySQLProvider implements StorageProvider {

    private final ElemPointsPlugin plugin;
    private final String host, database, username, password, prefix;
    private final int port, maxPool, minIdle;
    private final long maxLifetime, timeout;
    private final Map<String, String> props;

    private HikariDataSource pool;

    public MySQLProvider(ElemPointsPlugin plugin, String host, int port,
                         String database, String username, String password, String prefix,
                         int maxPool, int minIdle, long maxLifetime, long timeout,
                         Map<String, String> props) {
        this.plugin = plugin;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.prefix = prefix;
        this.maxPool = maxPool;
        this.minIdle = minIdle;
        this.maxLifetime = maxLifetime;
        this.timeout = timeout;
        this.props = props != null ? props : Collections.emptyMap();
    }

    private String table() { return prefix + "balances"; }

    @Override
    public void init() {
        try {
            HikariConfig cfg = new HikariConfig();
            StringBuilder url = new StringBuilder("jdbc:mysql://")
                    .append(host).append(":").append(port).append("/").append(database);
            if (!props.isEmpty()) {
                url.append("?");
                List<String> pairs = new ArrayList<>();
                props.forEach((k, v) -> pairs.add(k + "=" + v));
                url.append(String.join("&", pairs));
            }
            cfg.setJdbcUrl(url.toString());
            cfg.setUsername(username);
            cfg.setPassword(password);
            cfg.setMaximumPoolSize(maxPool);
            cfg.setMinimumIdle(minIdle);
            cfg.setMaxLifetime(maxLifetime);
            cfg.setConnectionTimeout(timeout);
            cfg.setPoolName("ElemPoints-MySQL");
            pool = new HikariDataSource(cfg);

            try (Connection c = pool.getConnection(); Statement s = c.createStatement()) {
                s.execute(
                        "CREATE TABLE IF NOT EXISTS " + table() + " (" +
                                "  uuid VARCHAR(36) NOT NULL," +
                                "  currency VARCHAR(64) NOT NULL," +
                                "  balance DOUBLE NOT NULL DEFAULT 0," +
                                "  updated BIGINT NOT NULL DEFAULT 0," +
                                "  PRIMARY KEY (uuid, currency)," +
                                "  INDEX idx_cur (currency)" +
                                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
                );
            }
            plugin.getLogger().info("[Storage] MySQL initialized: " + host + ":" + port + "/" + database);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[Storage] MySQL init failed", e);
        }
    }

    @Override
    public void shutdown() {
        if (pool != null && !pool.isClosed()) pool.close();
    }

    @Override
    public boolean isConnected() {
        return pool != null && !pool.isClosed();
    }

    @Override
    public CompletableFuture<Double> getBalance(UUID player, String currencyId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection c = pool.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "SELECT balance FROM " + table() + " WHERE uuid=? AND currency=?")) {
                ps.setString(1, player.toString());
                ps.setString(2, currencyId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getDouble(1);
                return -1.0;
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "[Storage] getBalance error", e);
                return 0.0;
            }
        });
    }

    @Override
    public CompletableFuture<Void> setBalance(UUID player, String currencyId, double amount) {
        return CompletableFuture.runAsync(() -> {
            try (Connection c = pool.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "INSERT INTO " + table() + "(uuid,currency,balance,updated) VALUES(?,?,?,?) " +
                                 "ON DUPLICATE KEY UPDATE balance=VALUES(balance), updated=VALUES(updated)")) {
                ps.setString(1, player.toString());
                ps.setString(2, currencyId);
                ps.setDouble(3, amount);
                ps.setLong(4, System.currentTimeMillis());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "[Storage] setBalance error", e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> hasAccount(UUID player, String currencyId) {
        return getBalance(player, currencyId).thenApply(b -> b >= 0);
    }

    @Override
    public CompletableFuture<Void> createAccount(UUID player, String currencyId, double defaultBalance) {
        return setBalance(player, currencyId, defaultBalance);
    }

    @Override
    public CompletableFuture<Map<UUID, Double>> getAllBalances(String currencyId) {
        return CompletableFuture.supplyAsync(() -> {
            Map<UUID, Double> map = new HashMap<>();
            try (Connection c = pool.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "SELECT uuid, balance FROM " + table() + " WHERE currency=?")) {
                ps.setString(1, currencyId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) map.put(UUID.fromString(rs.getString(1)), rs.getDouble(2));
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "[Storage] getAll error", e);
            }
            return map;
        });
    }

    @Override
    public CompletableFuture<Void> bulkSetBalances(String currencyId, Map<UUID, Double> balances) {
        return CompletableFuture.runAsync(() -> {
            try (Connection c = pool.getConnection()) {
                c.setAutoCommit(false);
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO " + table() + "(uuid,currency,balance,updated) VALUES(?,?,?,?) " +
                                "ON DUPLICATE KEY UPDATE balance=VALUES(balance), updated=VALUES(updated)")) {
                    long now = System.currentTimeMillis();
                    for (Map.Entry<UUID, Double> e : balances.entrySet()) {
                        ps.setString(1, e.getKey().toString());
                        ps.setString(2, currencyId);
                        ps.setDouble(3, e.getValue());
                        ps.setLong(4, now);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                    c.commit();
                } catch (SQLException e) {
                    c.rollback();
                    throw e;
                } finally {
                    c.setAutoCommit(true);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "[Storage] bulkSet error", e);
            }
        });
    }
}