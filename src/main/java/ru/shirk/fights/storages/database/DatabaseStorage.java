package ru.shirk.fights.storages.database;

import ru.shirk.fights.Fights;
import ru.shirk.fights.queue.QueueUser;
import ru.shirk.fights.storages.files.ConfigurationManager;

import java.sql.*;
import java.util.Arrays;

public class DatabaseStorage {
    private final DatabaseManager mySqlManager;

    public DatabaseStorage() {
        ConfigurationManager cm = Fights.getConfigurationManager();
        mySqlManager = new DatabaseManager(cm);
    }

    public void createUser(final String name) {
        final Connection connection = mySqlManager.getConnection();
        try {
            PreparedStatement preparedStmt = connection.prepareStatement("INSERT IGNORE INTO `" + mySqlManager.getBaseName() +
                    "`.`gladiators` (name) VALUES (?)");
            preparedStmt.setString(1, name);

            preparedStmt.executeUpdate();

            preparedStmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            mySqlManager.returnConnection(connection);
        }
    }

    public QueueUser getUser(final String name) {
        final Connection connection = mySqlManager.getConnection();
        try {
            PreparedStatement preparedStmt = connection.prepareStatement("SELECT * FROM `" + mySqlManager.getBaseName()
                    + "`.`gladiators` WHERE `name` = ?");
            preparedStmt.setString(1, name);

            try (ResultSet rs = preparedStmt.executeQuery()) {
                if (rs.next()) {
                    return new QueueUser(name, rs.getInt("wins"), rs.getInt("losses"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            mySqlManager.returnConnection(connection);
        }
        return null;
    }

    public void addWin(final String name) {
        final Connection connection = mySqlManager.getConnection();
        try {
            PreparedStatement preparedStmt = connection.prepareStatement("UPDATE `" + mySqlManager.getBaseName() +
                    "`.`gladiators` SET `wins` = `wins` + 1 WHERE name = ?");

            preparedStmt.setString(1, name);

            preparedStmt.executeUpdate();

            preparedStmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            mySqlManager.returnConnection(connection);
        }
    }

    public void addLoss(final String name) {
        final Connection connection = mySqlManager.getConnection();
        try {
            PreparedStatement preparedStmt = connection.prepareStatement("UPDATE `" + mySqlManager.getBaseName() +
                    "`.`gladiators` SET `losses` = `losses` + 1 WHERE name = ?");

            preparedStmt.setString(1, name);

            preparedStmt.executeUpdate();

            preparedStmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            mySqlManager.returnConnection(connection);
        }
    }
}
