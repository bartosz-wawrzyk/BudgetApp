package com.example.budgetapp.controllers;

import com.example.budgetapp.utils.AlertsController;
import com.example.budgetapp.database.DatabaseConnection;
import com.example.budgetapp.models.ExpenseRecord;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;

public class EditExpenseController {

    @FXML
    private TextField monthField;

    @FXML
    private TextField yearField;

    @FXML
    private TextField incomeField;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private ComboBox<String> subcategoryComboBox;

    @FXML
    private TextField subcategoryAmountField;

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

        monthField.setDisable(true);
        yearField.setDisable(true);

        loadUserCategories();
    }

    private void loadUserCategories() {
        categoryComboBox.getItems().clear();

        try (Connection conn = DatabaseConnection.connect()) {
            String query = """
                SELECT DISTINCT c.name
                FROM categories c
                JOIN subcategories s ON c.id = s.id_category
                JOIN expenses e ON e.id_subcategory = s.id
                WHERE e.user_id = ? AND e.month = ? AND e.year = ?
            """;

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, Integer.parseInt(userId));
            stmt.setInt(2, Integer.parseInt(expense.getMonth()));
            stmt.setInt(3, Integer.parseInt(expense.getYear()));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                categoryComboBox.getItems().add(rs.getString("name"));
            }

            categoryComboBox.setOnAction(e -> loadUserSubcategories());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUserSubcategories() {
        subcategoryComboBox.getItems().clear();
        subcategoryAmountField.clear();

        String selectedCategory = categoryComboBox.getValue();
        if (selectedCategory == null) return;

        try (Connection conn = DatabaseConnection.connect()) {
            String query = """
                SELECT s.name
                FROM subcategories s
                JOIN categories c ON s.id_category = c.id
                JOIN expenses e ON e.id_subcategory = s.id
                WHERE c.name = ? AND e.user_id = ? AND e.month = ? AND e.year = ?
                GROUP BY s.name
            """;

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, selectedCategory);
            stmt.setInt(2, Integer.parseInt(userId));
            stmt.setInt(3, Integer.parseInt(expense.getMonth()));
            stmt.setInt(4, Integer.parseInt(expense.getYear()));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                subcategoryComboBox.getItems().add(rs.getString("name"));
            }

            subcategoryComboBox.setOnAction(e -> loadSubcategoryAmount());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSubcategoryAmount() {
        String selectedSub = subcategoryComboBox.getValue();
        if (selectedSub == null) return;

        try (Connection conn = DatabaseConnection.connect()) {
            String query = """
                SELECT amount FROM expenses
                WHERE user_id = ? AND month = ? AND year = ?
                AND id_subcategory = (SELECT id FROM subcategories WHERE name = ?)
            """;

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, Integer.parseInt(userId));
            stmt.setInt(2, Integer.parseInt(expense.getMonth()));
            stmt.setInt(3, Integer.parseInt(expense.getYear()));
            stmt.setString(4, selectedSub);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                subcategoryAmountField.setText(String.valueOf(rs.getDouble("amount")));
            } else {
                subcategoryAmountField.clear();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
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

            String selectedSub = subcategoryComboBox.getValue();
            if (selectedSub != null && !subcategoryAmountField.getText().isEmpty()) {
                String updateExpense = """
                    UPDATE expenses SET amount = ?
                    WHERE user_id = ? AND year = ? AND month = ?
                    AND id_subcategory = (SELECT id FROM subcategories WHERE name = ?)
                """;

                PreparedStatement stmtExpense = conn.prepareStatement(updateExpense);
                stmtExpense.setDouble(1, Double.parseDouble(subcategoryAmountField.getText()));
                stmtExpense.setInt(2, Integer.parseInt(userId));
                stmtExpense.setInt(3, Integer.parseInt(yearField.getText()));
                stmtExpense.setInt(4, Integer.parseInt(monthField.getText()));
                stmtExpense.setString(5, selectedSub);
                stmtExpense.executeUpdate();
            }

            AlertsController.showAlert("Sukces", "Dane zostały zapisane!", Alert.AlertType.INFORMATION);

        } catch (SQLException e) {
            AlertsController.showAlert("Błąd", "Nie udało się zapisać zmian.", Alert.AlertType.ERROR);
        }

        Stage stage = (Stage) monthField.getScene().getWindow();
        stage.close();
    }

    public void prefillCategoryAndSubcategory(String category, String subcategory) {
        categoryComboBox.setValue(category);
        loadUserSubcategories(); // załaduj subkategorie
        subcategoryComboBox.setValue(subcategory);
        loadSubcategoryAmount();
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
