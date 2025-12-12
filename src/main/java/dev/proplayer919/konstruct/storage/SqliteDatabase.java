package dev.proplayer919.konstruct.storage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class SqliteDatabase {
    private final Path dbPath;
    private Connection connection;
    private final ExecutorService dbWorker;
    private final Object connLock = new Object();

    public SqliteDatabase(Path dbPath) {
        this.dbPath = dbPath;
        this.dbWorker = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "sqlite-db-worker");
            t.setDaemon(true);
            return t;
        });
    }

    public CompletableFuture<Void> connectAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                connect();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, dbWorker);
    }

    public void connect() throws Exception {
        synchronized (connLock) {
            if (connection != null && !connection.isClosed()) return;
            Files.createDirectories(dbPath.getParent());
            String url = "jdbc:sqlite:" + dbPath.toAbsolutePath();
            connection = DriverManager.getConnection(url);
            try (Statement s = connection.createStatement()) {
                // Enable WAL for better concurrency
                s.execute("PRAGMA journal_mode=WAL;");
                s.execute("PRAGMA synchronous=NORMAL;");
                s.execute("PRAGMA foreign_keys=ON;");
            }
            ensureSchema();
        }
    }

    public void close() {
        // Shutdown worker after closing connection
        Future<?> future = dbWorker.submit(() -> {
            synchronized (connLock) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException ignored) {
                    }
                }
            }
        });
        try {
            future.get(5, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
        dbWorker.shutdown();
        try {
            if (!dbWorker.awaitTermination(2, TimeUnit.SECONDS)) dbWorker.shutdownNow();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private void ensureSchema() throws SQLException {
        // Run schema creation on the connection thread to ensure ordering
        try (Statement s = connection.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY);");
            s.execute("CREATE TABLE IF NOT EXISTS player_permissions (player_uuid TEXT NOT NULL, permission_node TEXT NOT NULL, PRIMARY KEY(player_uuid, permission_node), FOREIGN KEY(player_uuid) REFERENCES players(uuid) ON DELETE CASCADE);");
            s.execute("CREATE INDEX IF NOT EXISTS idx_permission_node ON player_permissions(permission_node);");

            // Bans table: player_uuid (text primary key), banned_by (text, nullable), reason (text), created_at (integer epoch millis), expires_at (integer epoch millis, nullable)
            s.execute("CREATE TABLE IF NOT EXISTS bans (player_uuid TEXT PRIMARY KEY, banned_by TEXT, reason TEXT, created_at INTEGER NOT NULL, expires_at INTEGER);");
            s.execute("CREATE INDEX IF NOT EXISTS idx_bans_expires_at ON bans(expires_at);");
        }
    }

    // Run an update (INSERT/UPDATE/DELETE) asynchronously
    public CompletableFuture<Integer> runAsyncUpdate(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement ps = prepareStatementSync(sql, params)) {
                return ps.executeUpdate();
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }, dbWorker);
    }

    // Run query asynchronously and return list of rows as maps
    public CompletableFuture<List<Map<String, Object>>> runAsyncQuery(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement ps = prepareStatementSync(sql, params);
                 ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= cols; i++) {
                        row.put(md.getColumnLabel(i), rs.getObject(i));
                    }
                    rows.add(row);
                }
                return rows;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }, dbWorker);
    }

    // Prepare a statement and set parameters (must be called on DB thread)
    private PreparedStatement prepareStatementSync(String sql, Object... params) throws SQLException {
        synchronized (connLock) {
            if (connection == null) throw new SQLException("Connection not established");
            PreparedStatement ps = connection.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }
            return ps;
        }
    }

    // Run a synchronous transaction (blocking) - will run on DB thread and block until completion
    public void runInTransaction(Consumer<Connection> consumer) throws Exception {
        Future<?> f = dbWorker.submit(() -> {
            synchronized (connLock) {
                try {
                    boolean oldAuto = connection.getAutoCommit();
                    connection.setAutoCommit(false);
                    try {
                        consumer.accept(connection);
                        connection.commit();
                    } catch (Throwable t) {
                        try {
                            connection.rollback();
                        } catch (SQLException ignored) {
                        }
                        throw new CompletionException(t);
                    } finally {
                        connection.setAutoCommit(oldAuto);
                    }
                } catch (SQLException e) {
                    throw new CompletionException(e);
                }
            }
        });
        try {
            f.get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CompletionException && cause.getCause() != null) throw new Exception(cause.getCause());
            throw new Exception(cause);
        }
    }

    // Synchronous convenience wrappers (blocks on async)
    public boolean playerHasPermissionSync(UUID playerUuid, String permissionNode) {
        if (permissionNode == null) return false;

        // Build candidate permission nodes to check in order of specificity:
        // 1) exact node (e.g., "command.give.item")
        // 2) prefix wildcards (e.g., "command.give.*", "command.*")
        // 3) global wildcard "*"
        // Use a LinkedHashSet to preserve insertion order and avoid duplicates
        java.util.Set<String> candidates = new java.util.LinkedHashSet<>();
        candidates.add(permissionNode);

        String[] parts = permissionNode.split("\\.");
        for (int i = parts.length - 1; i >= 1; i--) {
            String prefix = String.join(".", java.util.Arrays.copyOf(parts, i));
            candidates.add(prefix + ".*");
        }

        candidates.add("*");

        // Build SQL with the appropriate number of placeholders
        StringBuilder sql = new StringBuilder("SELECT 1 FROM player_permissions WHERE player_uuid = ? AND permission_node IN (");
        for (int i = 0; i < candidates.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(") LIMIT 1");

        // Prepare parameters: first the UUID, then the candidate nodes
        java.util.List<Object> params = new java.util.ArrayList<>();
        params.add(playerUuid.toString());
        params.addAll(candidates);

        // Run query and return whether any matching row exists
        try {
            List<Map<String, Object>> rows = runAsyncQuery(sql.toString(), params.toArray()).join();
            return !rows.isEmpty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<UUID> getPlayersWithPermissionSync(String permissionNode) {
        try {
            List<Map<String, Object>> rows = runAsyncQuery("SELECT player_uuid FROM player_permissions WHERE permission_node = ?", permissionNode).join();
            Set<UUID> result = new HashSet<>();
            for (Map<String, Object> row : rows) {
                String uuidStr = (String) row.get("player_uuid");
                result.add(UUID.fromString(uuidStr));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void insertPlayerPermissionSync(UUID playerUuid, String permissionNode) {
        runAsyncUpdate("INSERT OR IGNORE INTO players(uuid) VALUES (?)", playerUuid.toString()).join();
        runAsyncUpdate("INSERT OR IGNORE INTO player_permissions(player_uuid, permission_node) VALUES (?, ?)", playerUuid.toString(), permissionNode).join();
    }

    public void removePlayerPermissionSync(UUID playerUuid, String permissionNode) {
        runAsyncUpdate("DELETE FROM player_permissions WHERE player_uuid = ? AND permission_node = ?", playerUuid.toString(), permissionNode).join();
    }

    // --- Bans support ---
    // Insert or replace a ban record. bannedByUuid may be null; expiresAtMillis may be null for permanent bans.
    public void insertBanSync(UUID playerUuid, String bannedByUuid, Long expiresAtMillis, String reason) {
        // Ensure player row exists for FK consistency with players table if desired
        runAsyncUpdate("INSERT OR IGNORE INTO players(uuid) VALUES (?)", playerUuid.toString()).join();
        runAsyncUpdate("INSERT OR REPLACE INTO bans(player_uuid, banned_by, reason, created_at, expires_at) VALUES (?, ?, ?, ?, ?)",
                playerUuid.toString(), bannedByUuid, reason, System.currentTimeMillis(), expiresAtMillis).join();
    }

    // Remove ban (unban)
    public void removeBanSync(UUID playerUuid) {
        runAsyncUpdate("DELETE FROM bans WHERE player_uuid = ?", playerUuid.toString()).join();
    }

    // Return ban info as a Map with keys: reason (String), expires_at (Long) or null. Returns null if no active ban.
    public Map<String, Object> getBanInfoSync(UUID playerUuid) {
        try {
            List<Map<String, Object>> rows = runAsyncQuery("SELECT reason, expires_at FROM bans WHERE player_uuid = ? LIMIT 1", playerUuid.toString()).join();
            if (rows.isEmpty()) return null;
            Map<String, Object> row = rows.getFirst();
            Object expiresObj = row.get("expires_at");
            if (expiresObj != null) {
                long expiresAt = ((Number) expiresObj).longValue();
                long now = System.currentTimeMillis();
                if (expiresAt > 0 && now > expiresAt) {
                    // ban expired -> remove it and return null
                    removeBanSync(playerUuid);
                    return null;
                }
            }
            return row;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Returns true if the player is currently banned (and performs expiry cleanup)
    public boolean isPlayerBannedSync(UUID playerUuid) {
        return getBanInfoSync(playerUuid) != null;
    }
}
