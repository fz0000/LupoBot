package de.nickkel.lupobot.core.mysql;

import de.nickkel.lupobot.core.LupoBot;

import java.sql.*;
import java.util.function.Consumer;

public class MySQL {

    public static String host = "localhost";
    public static String user = "N/A";
    public static String password ="N/A";
    public static String database = "N/A";
    public static String port = "3306";
    public static Connection connection;
    private static RequestQueue requestQ;

    public MySQL(String host, String user, String password, String database, String port) {
        MySQL.host = host;
        MySQL.user = user;
        MySQL.password = password;
        MySQL.database = database;
        MySQL.port = port;

        requestQ = new RequestQueue();
        requestQ.setRunning(true);
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, password);
            LupoBot.getInstance().getLogger().info("Successfully connected to database");
        } catch (SQLException e) {
            LupoBot.getInstance().getLogger().error("Failed to connect to database");
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {

        }
    }

    public void update(PreparedStatement statement) {
        checkConnection();
        LupoBot.getInstance().getExecutorService().execute(() -> this.queryUpdate(statement));
    }

    public void update(String statement) {
        checkConnection();
        LupoBot.getInstance().getExecutorService().execute(() -> this.queryUpdate(statement));
    }

    public void query(PreparedStatement statement, Consumer<ResultSet> consumer) {
        checkConnection();
        LupoBot.getInstance().getExecutorService().execute(() -> {
            ResultSet result = this.query(statement);
            consumer.accept(result);
        });
    }

    public void query(String statement, Consumer<ResultSet> consumer) {
        checkConnection();
        LupoBot.getInstance().getExecutorService().execute(() -> {
            ResultSet result = this.query(statement);
            consumer.accept(result);
        });
    }

    public PreparedStatement prepare(String query) {
        checkConnection();
        try {
            return this.connection.prepareStatement(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void queryUpdate(String query) {
        checkConnection();
        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            queryUpdate(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void queryUpdate(PreparedStatement preparedStatement) {
        checkConnection();
        try {
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                preparedStatement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ResultSet query(String query) {
        checkConnection();
        try {
            return query(this.connection.prepareStatement(query));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResultSet query(PreparedStatement statement) {
        checkConnection();
        try {
            return statement.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void checkConnection() {
        try {
            if (this.connection == null || this.connection.isClosed()) connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return connection != null;
    }
}