package com.example.budgetapp.database;

import com.example.budgetapp.utils.ErrorLogger;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_FILE = "config.properties";

    private static String host = "localhost";
    private static String port = "5432";
    private static String database = "budget";
    private static String user = "postgres";
    private static String password = "postgres";

    static {
        loadToMemory();
    }

    public static void saveConfig(String newHost, String newPort, String newDb, String newUser, String newPassword) {
        Properties props = new Properties();
        props.setProperty("host", newHost);
        props.setProperty("port", newPort);
        props.setProperty("database", newDb);
        props.setProperty("user", newUser);
        props.setProperty("password", newPassword);

        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            props.store(output, "Database Configuration");

            host = newHost;
            port = newPort;
            database = newDb;
            user = newUser;
            password = newPassword;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            props.load(input);
        } catch (IOException e) {
            ErrorLogger.logError("Brak pliku konfiguracji, użycie domyślnych ustawień." + e.getMessage());
        }
        return props;
    }

    private static void loadToMemory() {
        Properties props = loadConfig();
        host = props.getProperty("host", host);
        port = props.getProperty("port", port);
        database = props.getProperty("database", database);
        user = props.getProperty("user", user);
        password = props.getProperty("password", password);
    }

    public static String getHost() {
        return host;
    }

    public static String getPort() {
        return port;
    }

    public static String getDatabase() {
        return database;
    }

    public static String getUser() {
        return user;
    }

    public static String getPassword() {
        return password;
    }
}
