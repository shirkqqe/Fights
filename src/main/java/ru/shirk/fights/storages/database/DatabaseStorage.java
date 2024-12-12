package ru.shirk.fights.storages.database;

import lombok.NonNull;
import ru.shirk.fights.Fights;
import ru.shirk.fights.queue.QueueUser;
import ru.shirk.fights.storages.files.ConfigurationManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class DatabaseStorage {
    private final DatabaseManager mySqlManager;

    public DatabaseStorage() {
        ConfigurationManager cm = Fights.getConfigurationManager();
        mySqlManager = new DatabaseManager(cm);
    }

    public void createUser(@NonNull String name) {
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

    public QueueUser getUser(@NonNull String name) {
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

    public void addToQueue(@NonNull String name, @NonNull String server) {
        final Connection connection = mySqlManager.getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `" +
                    mySqlManager.getBaseName() + "`.`queue` (`name`, `server`) VALUES (?,?)");

            preparedStatement.setString(1, name);
            preparedStatement.setString(2, server);

            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            mySqlManager.returnConnection(connection);
        }
    }

    public @NonNull HashMap<QueueUser, String> getQueue() {
        final HashMap<QueueUser, String> queueUsers = new HashMap<>();
        final Connection connection = mySqlManager.getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `" +
                    mySqlManager.getBaseName() + "`.`queue`");

            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                queueUsers.put(getUser(resultSet.getString("name")),
                        resultSet.getString("server"));
            }
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            mySqlManager.returnConnection(connection);
        }
        return queueUsers;
    }

    public boolean isInQueue(@NonNull String name) {
        final Connection connection = mySqlManager.getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `" +
                    mySqlManager.getBaseName() + "`.`queue` WHERE `name` = ? LIMIT 1");

            preparedStatement.setString(1, name);

            return preparedStatement.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            mySqlManager.returnConnection(connection);
        }
    }

    public void removeFromQueue(@NonNull String name) {
        final Connection connection = mySqlManager.getConnection();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM `" +
                    mySqlManager.getBaseName() + "`.`queue` WHERE `name` = ?");

            preparedStatement.setString(1, name);

            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            mySqlManager.returnConnection(connection);
        }
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
