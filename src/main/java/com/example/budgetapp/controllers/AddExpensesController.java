package com.example.budgetapp.controllers;

import com.example.budgetapp.alerts.AlertsController;
import com.example.budgetapp.alerts.MonthUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.example.budgetapp.database.DatabaseConnection;

import java.sql.*;

public class AddExpensesController {

    private String userId;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private ComboBox<String> subcategoryComboBox;

    @FXML
    private ComboBox<String> monthComboBox;

    @FXML
    private TextField amountTextField;

    @FXML
    private TextField yearTextField;

    @FXML
    private Button cancelButton;

    @FXML
    public void initialize() {
        loadCategories();
    }

    private void loadCategories() {
        try (Connection conn = DatabaseConnection.connect()) {
            String query = "SELECT name FROM categories";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                categoryComboBox.getItems().add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Listener for the Category ComboBox to load subcategories
        categoryComboBox.setOnAction(event -> loadSubcategories());
    }

    private void loadSubcategories() {
        subcategoryComboBox.getItems().clear();
        String selectedCategory = categoryComboBox.getValue();

        if (selectedCategory != null) {
            try (Connection conn = DatabaseConnection.connect()) {
                String query = "SELECT s.name FROM subcategories s JOIN categories c ON s.id_category = c.id WHERE c.name = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, selectedCategory);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    subcategoryComboBox.getItems().add(rs.getString("name"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleAddExpense() {
        String selectedCategory = categoryComboBox.getValue();
        String selectedSubcategory = subcategoryComboBox.getValue();
        String monthCategory = monthComboBox.getValue();
        String yearText = yearTextField.getText();
        String amountText = amountTextField.getText();

        int month = MonthUtils.getMonthNumber(monthCategory);
        int year;

        if (selectedCategory == null || selectedSubcategory == null || monthCategory == null || amountText.isEmpty() || yearText.isEmpty()) {
            AlertsController.showAlert("Błąd", "Wszystkie pola muszą być wypełnione.", Alert.AlertType.ERROR);
            return;
        }

        try {
            year = Integer.parseInt(yearText);
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                AlertsController.showAlert("Błąd", "Kwota musi być większa niż zero.", Alert.AlertType.ERROR);
                return;
            }

            if (expenseExists(month, year, selectedSubcategory, userId)) {
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Potwierdzenie");
                confirmationAlert.setHeaderText("Wydatek już istnieje");
                confirmationAlert.setContentText("Wydatek dla tego miesiąca, roku i podkategorii już istnieje. Czy chcesz dodać nową kwotę?");

                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        updateExpense(month, year, selectedSubcategory, amount, userId);
                    }
                });
            } else {
                addNewExpense(month, year, selectedSubcategory, amount, userId);
            }

            Stage stage = (Stage) amountTextField.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            AlertsController.showAlert("Błąd", "Kwota musi być liczbą.", Alert.AlertType.ERROR);
        }
    }

    private boolean expenseExists(int month, int year, String subCategory, String userId) {
        String query = "SELECT 1 FROM expenses WHERE month = ? AND year = ? AND id_subcategory = (SELECT id FROM subcategories WHERE name = ?) AND user_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, month);
            stmt.setInt(2, year);
            stmt.setString(3, subCategory);
            stmt.setInt(4, Integer.parseInt(userId));

            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateExpense(int month, int year, String subCategory, double amount, String userId) {
        String updateQuery = "UPDATE expenses SET amount = amount + ? WHERE month = ? AND year = ? AND id_subcategory = (SELECT id FROM subcategories WHERE name = ?) AND user_id = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            stmt.setDouble(1, amount);
            stmt.setInt(2, month);
            stmt.setInt(3, year);
            stmt.setString(4, subCategory);
            stmt.setInt(5, Integer.parseInt(userId));

            stmt.executeUpdate();
            AlertsController.showAlert("Sukces", "Kwota wydatku została zaktualizowana.", Alert.AlertType.INFORMATION);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addNewExpense(int month, int year, String subCategory, double amount, String userId) {
        String insertQuery = "INSERT INTO expenses (amount, id_subcategory, month, year, user_id) VALUES (?, (SELECT id FROM subcategories WHERE name = ?), ?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {

            stmt.setDouble(1, amount);
            stmt.setString(2, subCategory);
            stmt.setInt(3, month);
            stmt.setInt(4, year);
            stmt.setInt(5, Integer.parseInt(userId));

            stmt.executeUpdate();
            AlertsController.showAlert("Sukces", "Wydatek został dodany.", Alert.AlertType.INFORMATION);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
