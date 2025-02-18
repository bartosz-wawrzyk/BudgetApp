package com.example.budgetapp.database;

import java.sql.*;
import java.util.Properties;

public class DatabaseConnection {
    private static Properties config = ConfigManager.loadConfig();

    public static Connection connect() {
        String host = config.getProperty("host", "localhost");
        String port = config.getProperty("port", "5432");
        String database = config.getProperty("database", "budget");
        String user = config.getProperty("user", "postgres");
        String password = config.getProperty("password", "postgres");

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean validateLogin(String login, String password) {
        String query = "SELECT * FROM users WHERE login = ? AND password = crypt(?, password)";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, login);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getUserIdByLogin(String login) {
        String userId = null;
        String query = "SELECT id FROM users WHERE login = ?";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                userId = rs.getString("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userId;
    }

    public static boolean isUserExist(String login) {
        String query = "SELECT COUNT(*) FROM users WHERE login = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0; // Zwraca true, jeśli użytkownik istnieje
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean addUser(String login, String password) {
        String query = "INSERT INTO users (login, password) VALUES (?, crypt(?, gen_salt('bf')))";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, login);
            statement.setString(2, password);

            int rowsAffected = statement.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
