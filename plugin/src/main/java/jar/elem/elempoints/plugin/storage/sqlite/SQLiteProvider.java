package jar.elem.elempoints.plugin.storage.sqlite;

import jar.elem.elempoints.plugin.ElemPointsPlugin;
import jar.elem.elempoints.plugin.storage.StorageProvider;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public final class SQLiteProvider implements StorageProvider {

    private final ElemPointsPlugin plugin;
    private final String filePath;
    private Connection connection;
    private final ReentrantLock lock = new ReentrantLock();

    public SQLiteProvider(ElemPointsPlugin plugin, String filePath) {
        this.plugin = plugin;
        this.filePath = filePath;
    }

    @Override
    public void init() {
        try {
            File file = new File(plugin.getDataFolder(), filePath);
            file.getParentFile().mkdirs();
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            connection.createStatement().execute("PRAGMA journal_mode=WAL");
            connection.createStatement().execute("PRAGMA synchronous=NORMAL");
            createTable();
            plugin.getLogger().info("[Storage] SQLite initialized: " + filePath);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[Storage] SQLite init failed: " + filePath, e);
        }
    }

    private void createTable() throws SQLException {
        try (Statement s = connection.createStatement()) {
            s.execute(
                    "CREATE TABLE IF NOT EXISTS ep_balances (" +
                            "  uuid TEXT NOT NULL," +
                            "  currency TEXT NOT NULL," +
                            "  balance REAL NOT NULL DEFAULT 0," +
                            "  updated INTEGER NOT NULL DEFAULT 0," +
                            "  PRIMARY KEY (uuid, currency)" +
                            ")"
            );
        }
    }

    private Connection conn() throws SQLException {
        if (connection == null || connection.isClosed()) {
            File file = new File(plugin.getDataFolder(), filePath);
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        }
        return connection;
    }

    @Override
    public void shutdown() {
        lock.lock();
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "[Storage] SQLite close error", e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isConnected() {
        try { return connection != null && !connection.isClosed(); }
        catch (SQLException e) { return false; }
    }

    @Override
    public CompletableFuture<Double> getBalance(UUID player, String currencyId) {
        return CompletableFuture.supplyAsync(() -> {
            lock.lock();
            try (PreparedStatement ps = conn().prepareStatement(
                    "SELECT balance FROM ep_balances WHERE uuid=? AND currency=?")) {
                ps.setString(1, player.toString());
                ps.setString(2, currencyId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getDouble(1);
                return -1.0; // no account
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "[Storage] getBalance error", e);
                return 0.0;
            } finally {
                lock.unlock();
            }
        });
    }

    @Override
    public CompletableFuture<Void> setBalance(UUID player, String currencyId, double amount) {
        return CompletableFuture.runAsync(() -> {
            lock.lock();
            try (PreparedStatement ps = conn().prepareStatement(
                    "INSERT INTO ep_balances(uuid,currency,balance,updated) VALUES(?,?,?,?) " +
                            "ON CONFLICT(uuid,currency) DO UPDATE SET balance=?,updated=?")) {
                long now = System.currentTimeMillis();
                ps.setString(1, player.toString());
                ps.setString(2, currencyId);
                ps.setDouble(3, amount);
                ps.setLong(4, now);
                ps.setDouble(5, amount);
                ps.setLong(6, now);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "[Storage] setBalance error", e);
            } finally {
                lock.unlock();
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
            lock.lock();
            try (PreparedStatement ps = conn().prepareStatement(
                    "SELECT uuid, balance FROM ep_balances WHERE currency=?")) {
                ps.setString(1, currencyId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(UUID.fromString(rs.getString(1)), rs.getDouble(2));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "[Storage] getAllBalances error", e);
            } finally {
                lock.unlock();
            }
            return map;
        });
    }

    @Override
    public CompletableFuture<Void> bulkSetBalances(String currencyId, Map<UUID, Double> balances) {
        return CompletableFuture.runAsync(() -> {
            lock.lock();
            try {
                Connection c = conn();
                c.setAutoCommit(false);
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO ep_balances(uuid,currency,balance,updated) VALUES(?,?,?,?) " +
                                "ON CONFLICT(uuid,currency) DO UPDATE SET balance=?,updated=?")) {
                    long now = System.currentTimeMillis();
                    for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
                        ps.setString(1, entry.getKey().toString());
                        ps.setString(2, currencyId);
                        ps.setDouble(3, entry.getValue());
                        ps.setLong(4, now);
                        ps.setDouble(5, entry.getValue());
                        ps.setLong(6, now);
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
            } finally {
                lock.unlock();
            }
        });
    }
}