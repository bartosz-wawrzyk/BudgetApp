package com.example.budgetapp.controllers;

import com.example.budgetapp.utils.MonthUtils;
import com.example.budgetapp.database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import static com.example.budgetapp.utils.AlertsController.showAlert;

public class AddIncomeController {

    @FXML
    private TextField amountField;

    @FXML
    private ComboBox<String> monthComboBox;

    @FXML
    private TextField yearField;

    @FXML
    private Button cancelButton;

    private String userId;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @FXML
    private void handleAddIncome() {
        String amountText = amountField.getText();
        String selectedMonth = monthComboBox.getValue();
        String yearText = yearField.getText();

        if (amountText.isEmpty() || selectedMonth == null || yearText.isEmpty()) {
            showAlert("Błąd", "Wszystkie pola muszą być wypełnione.", Alert.AlertType.ERROR);
            return;
        }

        int month = MonthUtils.getMonthNumber(selectedMonth);
        int year;
        double amount;

        try {
            year = Integer.parseInt(yearText);
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            showAlert("Błąd", "Podaj poprawne wartości liczbowe dla roku i kwoty.", Alert.AlertType.ERROR);
            return;
        }

        try (Connection conn = DatabaseConnection.connect()) {

            String checkQuery = "SELECT amount FROM incomes WHERE user_id = ? AND month = ? AND year = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, Integer.parseInt(userId));
                checkStmt.setInt(2, month);
                checkStmt.setInt(3, year);

                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    double currentAmount = rs.getDouble("amount");
                    double newAmount = currentAmount + amount;

                    String updateQuery = "UPDATE incomes SET amount = ? WHERE user_id = ? AND month = ? AND year = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setDouble(1, newAmount);
                        updateStmt.setInt(2, Integer.parseInt(userId));
                        updateStmt.setInt(3, month);
                        updateStmt.setInt(4, year);
                        updateStmt.executeUpdate();
                    }
                } else {
                    String insertQuery = "INSERT INTO incomes (user_id, month, year, amount) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, Integer.parseInt(userId));
                        insertStmt.setInt(2, month);
                        insertStmt.setInt(3, year);
                        insertStmt.setDouble(4, amount);
                        insertStmt.executeUpdate();
                    }
                }
            }
            showAlert("Sukces", "Przychód został zapisany.", Alert.AlertType.INFORMATION);

            Stage stage = (Stage) amountField.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Błąd", "Wystąpił problem podczas dodawania przychodu.", Alert.AlertType.ERROR);
        }
    }

    public void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
