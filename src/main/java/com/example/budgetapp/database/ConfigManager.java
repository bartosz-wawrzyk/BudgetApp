package com.example.budgetapp.database;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_FILE = "config.properties";

    public static void saveConfig(String host, String port, String dbName, String user, String password) {
        Properties props = new Properties();
        props.setProperty("host", host);
        props.setProperty("port", port);
        props.setProperty("database", dbName);
        props.setProperty("user", user);
        props.setProperty("password", password);

        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            props.store(output, "Database Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            props.load(input);
        } catch (IOException e) {
            System.out.println("Brak pliku konfiguracji, użycie domyślnych ustawień.");
        }
        return props;
    }
}

