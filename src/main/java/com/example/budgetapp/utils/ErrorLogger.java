package com.example.budgetapp.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ErrorLogger {

    private static final String LOG_FILE = "bledy_log.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logError(String errorMessage) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logMessage = timestamp + " - " + errorMessage;

        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(logMessage);
        } catch (IOException e) {
            ErrorLogger.logError("Błąd zapisu błędów do pliku: " + e.getMessage());
        }
    }
}
