package com.example.budgetapp.controllers;

import com.example.budgetapp.utils.AlertsController;
import com.example.budgetapp.utils.ErrorLogger;
import com.example.budgetapp.database.DatabaseConnection;
import com.example.budgetapp.models.ExpenseRecord;
import com.example.budgetapp.utils.IconUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MainWindowController {

    private String userId;

    @FXML
    private TableView<ExpenseRecord> expensesTable;

    @FXML
    private TableColumn<ExpenseRecord, String> monthColumn;

    @FXML
    private TableColumn<ExpenseRecord, String> yearColumn;

    @FXML
    private TableColumn<ExpenseRecord, Double> incomeColumn;

    @FXML
    private TableColumn<ExpenseRecord, Double> rentColumn;

    @FXML
    private TableColumn<ExpenseRecord, Double> adminRentColumn;

    public void setUserId(String userId) {
        this.userId = userId;

        if (userId != null && !userId.isEmpty()) {
            loadExpenses();
        } else {
            ErrorLogger.logError("Błąd: userId jest NULL lub pusty!");
        }
    }

    @FXML
    private void initialize() {
        monthColumn.setCellValueFactory(new PropertyValueFactory<>("month"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        incomeColumn.setCellValueFactory(new PropertyValueFactory<>("income"));
        rentColumn.setCellValueFactory(new PropertyValueFactory<>("rent"));
        adminRentColumn.setCellValueFactory(new PropertyValueFactory<>("adminRent"));
    }

    @FXML
    private void handleIncome() {
        openModalWindow("/com/example/budgetapp/main/add_income.fxml", "Dodaj przychód", userId);
    }

    @FXML
    private void handleExpenses() {
        openModalWindow("/com/example/budgetapp/main/add_expenses.fxml", "Dodaj wydatek", userId);
    }

    private void openModalWindow(String fxmlFile, String title, String userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            if (fxmlFile.equals("/com/example/budgetapp/main/add_income.fxml")) {
                AddIncomeController controllerIncome = loader.getController();
                controllerIncome.setUserId(userId);
            } else if (fxmlFile.equals("/com/example/budgetapp/main/add_expenses.fxml")) {
                AddExpensesController controllerExpenses = loader.getController();
                controllerExpenses.setUserId(userId);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            IconUtil.setAppIcon(stage);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadExpenses();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEdit() {
        List<ExpenseRecord> selectedItems = expensesTable.getSelectionModel().getSelectedItems();

        if (selectedItems.isEmpty()) {
            AlertsController.showAlert("Błąd", "Nie wybrano żadnego wpisu do edycji.", Alert.AlertType.WARNING);
            return;
        }
        if (selectedItems.size() > 1) {
            AlertsController.showAlert("Błąd", "Proszę wybrać tylko jeden wpis do edycji.", Alert.AlertType.WARNING);
            return;
        }

        ExpenseRecord selectedExpense = selectedItems.get(0);
        openEditModal(selectedExpense);
    }

    private void openEditModal(ExpenseRecord expense) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/budgetapp/main/edit_expense.fxml"));
            Parent root = loader.load();

            EditExpenseController controller = loader.getController();
            controller.setExpenseData(expense, userId);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edytuj wpis");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadExpenses();

        } catch (IOException e) {
            ErrorLogger.logError("Błąd ładowania okna edycji: " + e.getMessage());
        }
    }

    // Method of loading expenditure data
    private void loadExpenses() {
        if (userId == null || userId.isEmpty()) {
            System.err.println("Błąd: userId jest null lub pusty!");
            AlertsController.showAlert("Błąd", "Nie udało się pobrać ID użytkownika.", Alert.AlertType.ERROR);
            return;
        }

        // 🔥 Clean the table to avoid adding duplicates
        expensesTable.getItems().clear();

        try (Connection conn = DatabaseConnection.connect()) {
            String query = "SELECT " +
                    "i.month, " +
                    "i.year, " +
                    "COALESCE(i.amount, 0) AS income, " +
                    "COALESCE((SELECT amount FROM expenses WHERE id_subcategory = 1 AND month = i.month AND year = i.year), 0) AS rent, " +
                    "COALESCE((SELECT amount FROM expenses WHERE id_subcategory = 2 AND month = i.month AND year = i.year), 0) AS adminRent " +
                    "FROM " +
                    "incomes i " +
                    "WHERE " +
                    "i.user_id = ? " +
                    "ORDER BY " +
                    "i.year, i.month; ";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, Integer.parseInt(userId));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String month = String.valueOf(rs.getInt("month"));
                String year = String.valueOf(rs.getInt("year"));
                double income = rs.getDouble("income");
                double rent = rs.getDouble("rent");
                double adminRent = rs.getDouble("adminRent");

                expensesTable.getItems().add(new ExpenseRecord(month, year, income, rent, adminRent));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            AlertsController.showAlert("Błąd", "Nie udało się załadować danych.", Alert.AlertType.ERROR);
        }
    }

    public void handleCategoryDictionary() {
        openModalWindowMenu("/com/example/budgetapp/configurations/categories.fxml","Słownik kategorii");
    }

    public void handleSubcategoryDictionary() {
        openModalWindowMenu("/com/example/budgetapp/configurations/subcategories.fxml","Słownik podkategorii");
    }

    public void handleLogout() {

    }

    private void openModalWindowMenu(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            IconUtil.setAppIcon(stage);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadExpenses();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
