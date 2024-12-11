package ru.shirk.fights.storages.database;

import lombok.Getter;
import ru.shirk.fights.Fights;
import ru.shirk.fights.storages.files.Configuration;
import ru.shirk.fights.storages.files.ConfigurationManager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class DatabaseManager {
    private final Configuration config;
    private final Stack<Connection> freePool = new Stack<>();
    private final Set<Connection> occupiedPool = new HashSet<>();
    @Getter
    private final String baseName;

    public DatabaseManager(ConfigurationManager configurationManager){
        this.config = configurationManager.getConfig("settings.yml");
        baseName = config.c("storage.baseName");
        setup();
    }

    private Connection makeAvailable(Connection conn) throws SQLException {
        if (isConnectionAvailable(conn)) {
            return conn;
        }

        occupiedPool.remove(conn);
        conn.close();

        conn = createNewConnection();
        occupiedPool.add(conn);
        return conn;
    }

    private boolean isConnectionAvailable(Connection conn) {
        try (Statement st = conn.createStatement()) {
            st.executeQuery("select 1");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private Connection createNewConnectionForPool() throws SQLException {
        Connection conn = createNewConnection();
        occupiedPool.add(conn);
        return conn;
    }

    private Connection createNewConnection() throws SQLException {
        Connection conn;
        String databaseUrl;
        if(config.c("storage.type").equalsIgnoreCase("MySql")) {
            databaseUrl = "jdbc:mysql://" + config.c("storage.host") + ":" + config.c("storage.port");
        } else {
            databaseUrl = "jdbc:h2:" + Fights.getInstance().getDataFolder().getAbsolutePath() + File.separator + baseName;
        }
        conn = DriverManager.getConnection(databaseUrl, config.c("storage.user"), config.c("storage.password"));
        return conn;
    }

    private Connection getConnectionFromPool() {
        Connection conn = null;

        if (!freePool.isEmpty()) {
            conn = freePool.pop();
            occupiedPool.add(conn);
        }

        return conn;
    }

    public synchronized Connection getConnection(){
        try {
            Connection conn;

            if (isFull()) {
                throw new SQLException("Exceeded the maximum number of connections");
            }

            conn = getConnectionFromPool();

            if (conn == null) {
                conn = createNewConnectionForPool();
            }

            conn = makeAvailable(conn);
            return conn;
        } catch (SQLException e){
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public synchronized void returnConnection(Connection conn) {
        try {
            if (conn == null) {
                throw new NullPointerException();
            }
            occupiedPool.remove(conn);
            freePool.push(conn);
        } catch (NullPointerException e){
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private synchronized boolean isFull() {
        return ((freePool.size() == 0) && (freePool.size()+occupiedPool.size() >= config.getFile().getInt("storage." +
                "maxConnections")));
    }

    private void setup() {
        Connection conn = getConnection();
        try {
            Statement statement = conn.createStatement();

            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + baseName + "`;");

            statement.execute("USE " + baseName + ";");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `gladiators` (" +
                    "  `name` VARCHAR(20) NOT NULL PRIMARY KEY," +
                    "  `wins` INT DEFAULT '0'," +
                    "  `losses` INT DEFAULT '0'" +
                    ");");
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            returnConnection(conn);
        }
    }
}
