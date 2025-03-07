package com.example.budgetapp.controllers;

import com.example.budgetapp.alerts.AlertsController;
import com.example.budgetapp.database.DatabaseConnection;
import com.example.budgetapp.models.ExpenseRecord;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EditExpenseController {

    @FXML
    private TextField monthField;

    @FXML
    private TextField yearField;

    @FXML
    private TextField incomeField;

    @FXML
    private TextField rentField;

    @FXML
    private TextField adminRentField;

    @FXML
    private Button cancelButton;

    private ExpenseRecord expense;
    private String userId;

    public void setExpenseData(ExpenseRecord expense, String userId) {
        this.expense = expense;
        this.userId = userId;
        monthField.setText(expense.getMonth());
        yearField.setText(expense.getYear());
        incomeField.setText(String.valueOf(expense.getIncome()));
        rentField.setText(String.valueOf(expense.getRent()));
        adminRentField.setText(String.valueOf(expense.getAdminRent()));
    }

    @FXML
    private void handleSave() {
        try (Connection conn = DatabaseConnection.connect()) {
            String updateIncome = "UPDATE incomes SET amount = ? WHERE user_id = ? AND year = ? AND month = ?";
            PreparedStatement stmtIncome = conn.prepareStatement(updateIncome);
            stmtIncome.setDouble(1, Double.parseDouble(incomeField.getText()));
            stmtIncome.setInt(2, Integer.parseInt(userId));
            stmtIncome.setInt(3, Integer.parseInt(yearField.getText()));
            stmtIncome.setInt(4, Integer.parseInt(monthField.getText()));
            stmtIncome.executeUpdate();

            String updateRent = "UPDATE expenses SET amount = ? WHERE user_id = ? AND year = ? AND month = ? AND id_subcategory = 1";
            PreparedStatement stmtRent = conn.prepareStatement(updateRent);
            stmtRent.setDouble(1, Double.parseDouble(rentField.getText()));
            stmtRent.setInt(2, Integer.parseInt(userId));
            stmtRent.setInt(3, Integer.parseInt(yearField.getText()));
            stmtRent.setInt(4, Integer.parseInt(monthField.getText()));
            stmtRent.executeUpdate();

            String updateAdminRent = "UPDATE expenses SET amount = ? WHERE user_id = ? AND year = ? AND month = ? AND id_subcategory = 2";
            PreparedStatement stmtAdminRent = conn.prepareStatement(updateAdminRent);
            stmtAdminRent.setDouble(1, Double.parseDouble(adminRentField.getText()));
            stmtAdminRent.setInt(2, Integer.parseInt(userId));
            stmtAdminRent.setInt(3, Integer.parseInt(yearField.getText()));
            stmtAdminRent.setInt(4, Integer.parseInt(monthField.getText()));
            stmtAdminRent.executeUpdate();

            AlertsController.showAlert("Sukces", "Dane zostały zapisane!", Alert.AlertType.INFORMATION);

        } catch (SQLException e) {
            AlertsController.showAlert("Błąd", "Nie udało się zapisać zmian.", Alert.AlertType.ERROR);
        }

        Stage stage = (Stage) monthField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}

