package com.example.budgetapp.controllers;

import com.example.budgetapp.controllers.menu.TableConfigurationController;
import com.example.budgetapp.utils.AlertsController;
import com.example.budgetapp.utils.ErrorLogger;
import com.example.budgetapp.database.DatabaseConnection;
import com.example.budgetapp.models.ExpenseRecord;
import com.example.budgetapp.utils.IconUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.*;

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
    private TableColumn<ExpenseRecord, Double> differenceColumn;

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
        differenceColumn.setCellValueFactory(new PropertyValueFactory<>("difference"));
        differenceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f", value));

                    if (value > 0) {
                        setStyle("-fx-text-fill: #80ff80;");
                    } else if (value < 0) {
                        setStyle("-fx-text-fill: #ff9999;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        applyTableConfiguration();
    }

    public void applyTableConfiguration() {
        expensesTable.getColumns().clear();
        expensesTable.getColumns().addAll(monthColumn, yearColumn, incomeColumn);
    }

    private void loadExpenses() {
        if (userId == null || userId.isEmpty()) {
            AlertsController.showAlert("Błąd", "Nie udało się pobrać ID użytkownika.", Alert.AlertType.ERROR);
            return;
        }

        recalculateAllDifferences();

        expensesTable.getItems().clear();
        expensesTable.getColumns().clear();
        expensesTable.getColumns().addAll(monthColumn, yearColumn, incomeColumn, differenceColumn);

        Map<Integer, String> categoryIdToName = new HashMap<>();
        Map<Integer, List<Integer>> categoryToSubcategories = new HashMap<>();

        try (Connection conn = DatabaseConnection.connect()) {
            // Mapping: category → its subcategories
            String mapQuery = "SELECT c.id as category_id, c.name as category_name, s.id as subcategory_id " +
                    "FROM categories c " +
                    "JOIN subcategories s ON c.id = s.id_category";
            ResultSet mapRs = conn.prepareStatement(mapQuery).executeQuery();
            while (mapRs.next()) {
                int categoryId = mapRs.getInt("category_id");
                int subcategoryId = mapRs.getInt("subcategory_id");
                String categoryName = mapRs.getString("category_name");

                categoryIdToName.put(categoryId, categoryName);
                categoryToSubcategories.computeIfAbsent(categoryId, k -> new ArrayList<>()).add(subcategoryId);
            }

            // Dynamic columns for user-configured subcategories
            String columnQuery = "SELECT c.id, c.name FROM categories c " +
                    "JOIN table_configuration tc ON c.id = tc.category_id " +
                    "WHERE tc.user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(columnQuery);
            stmt.setInt(1, Integer.parseInt(userId));
            ResultSet rs = stmt.executeQuery();

            Set<Integer> selectedCategoryIds = new HashSet<>();
            while (rs.next()) {
                int categoryId = rs.getInt("id");
                selectedCategoryIds.add(categoryId);
            }

            Platform.runLater(() -> {
                double remainingWidth = expensesTable.getWidth() - 600;
                double categoryWidth = remainingWidth / Math.max(1, selectedCategoryIds.size());

                List<TableColumn<ExpenseRecord, ?>> dynamicColumns = new ArrayList<>();

                for (int categoryId : selectedCategoryIds) {
                    String categoryName = categoryIdToName.get(categoryId);
                    TableColumn<ExpenseRecord, Double> sumCol = new TableColumn<>(categoryName);
                    sumCol.setCellValueFactory(cellData -> cellData.getValue().getCategorySum(categoryId).asObject());
                    sumCol.setStyle("-fx-alignment: CENTER;");
                    sumCol.setPrefWidth(categoryWidth);

                    dynamicColumns.add(sumCol);
                }

                expensesTable.getColumns().addAll(dynamicColumns);
            });

            // Data loading
            String dataQuery = "SELECT i.month, i.year, COALESCE(i.amount, 0) AS income, " +
                    "s.id AS subcategory_id, s.id_category AS category_id, COALESCE(e.amount, 0) AS expense " +
                    "FROM incomes i " +
                    "LEFT JOIN expenses e ON i.user_id = e.user_id AND i.month = e.month AND i.year = e.year " +
                    "LEFT JOIN subcategories s ON e.id_subcategory = s.id " +
                    "WHERE i.user_id = ? " +
                    "ORDER BY i.year ASC, i.month ASC";

            PreparedStatement dataStmt = conn.prepareStatement(dataQuery);
            dataStmt.setInt(1, Integer.parseInt(userId));
            ResultSet rsData = dataStmt.executeQuery();

            Map<String, ExpenseRecord> recordsMap = new HashMap<>();

            while (rsData.next()) {
                String month = String.valueOf(rsData.getInt("month"));
                String year = String.valueOf(rsData.getInt("year"));
                double income = rsData.getDouble("income");
                int subcategoryId = rsData.getInt("subcategory_id");
                double expense = rsData.getDouble("expense");

                String key = month + "-" + year;
                ExpenseRecord record = recordsMap.getOrDefault(key, new ExpenseRecord(month, year, income));

                if (subcategoryId != 0) {
                    record.setSubcategoryExpense(subcategoryId, expense);
                }
                recordsMap.put(key, record);
            }

            for (ExpenseRecord record : recordsMap.values()) {
                for (int categoryId : selectedCategoryIds) {
                    double sum = 0;
                    List<Integer> subs = categoryToSubcategories.get(categoryId);
                    if (subs != null) {
                        for (int subId : subs) {
                            sum += record.getSubcategoryExpense(subId).get();
                        }
                    }
                    record.setCategorySum(categoryId, sum);
                }

                try (PreparedStatement diffStmt = conn.prepareStatement("""
                        SELECT amount FROM income_expense_summary
                        WHERE income_id = (
                            SELECT id FROM incomes
                            WHERE user_id = ? AND month = ? AND year = ?
                            LIMIT 1
                        ) AND category_id = 0
                    """)) {
                    diffStmt.setInt(1, Integer.parseInt(userId));
                    diffStmt.setInt(2, Integer.parseInt(record.getMonth()));
                    diffStmt.setInt(3, Integer.parseInt(record.getYear()));

                    ResultSet diffRs = diffStmt.executeQuery();
                    double diff = diffRs.next() ? diffRs.getDouble("amount") : 0.0;
                    record.setDifference(diff);
                } catch (SQLException e) {
                    e.printStackTrace();
                    record.setDifference(0.0);
                }
            }

            List<ExpenseRecord> sortedRecords = new ArrayList<>(recordsMap.values());
            sortedRecords.sort(Comparator.comparing((ExpenseRecord r) -> Integer.parseInt(r.getYear()))
                    .thenComparing(r -> Integer.parseInt(r.getMonth())));
            expensesTable.getItems().addAll(sortedRecords);
        } catch (SQLException e) {
            ErrorLogger.logError("Błąd ładowania danych do tabeli: " + " - " + e.getMessage());
        }
    }

    @FXML
    private void handleIncome() {
        openModalWindow("/com/example/budgetapp/main/add_income.fxml", "Dodaj przychód", userId);
    }

    @FXML
    private void handleTableConfiguration() {
        openModalWindow("/com/example/budgetapp/configurations/table_configuration.fxml", "Konfiguracja tabeli", userId);
    }

    @FXML
    private void handleDetails() {
        ExpenseRecord selected = getSingleSelectedRecord();
        if (selected != null) {
            openDetailsModal(selected);
        }
    }

    private ExpenseRecord getSingleSelectedRecord() {
        List<ExpenseRecord> selectedItems = expensesTable.getSelectionModel().getSelectedItems();

        if (selectedItems.isEmpty()) {
            AlertsController.showAlert("Błąd", "Nie wybrano żadnego miesiąca i roku.", Alert.AlertType.WARNING);
            return null;
        }

        if (selectedItems.size() > 1) {
            AlertsController.showAlert("Błąd", "Proszę wybrać tylko jeden miesiąc i rok.", Alert.AlertType.WARNING);
            return null;
        }
        return selectedItems.get(0);
    }

    private void openDetailsModal(ExpenseRecord expense) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/budgetapp/main/details.fxml"));
            Parent root = loader.load();

            DetailsController controller = loader.getController();
            controller.setExpenseData(expense, userId);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Szczegóły miesiąca: " + expense.getMonth() + "/" + expense.getYear());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            loadExpenses();
        } catch (IOException e) {
            ErrorLogger.logError("Błąd ładowania szczegółów: " + expense.getMonth() + "/" + expense.getYear() + " - " + e.getMessage());
        }
    }

    private void openModalWindow(String fxmlFile, String title, String userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            switch (fxmlFile) {
                case "/com/example/budgetapp/main/add_income.fxml" -> {
                    AddIncomeController controllerIncome = loader.getController();
                    controllerIncome.setUserId(userId);
                }
                case "/com/example/budgetapp/configurations/table_configuration.fxml" -> {
                    TableConfigurationController controllerTable = loader.getController();
                    controllerTable.setUserId(userId);
                }
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            IconUtil.setAppIcon(stage);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            loadExpenses();
        } catch (IOException e) {
            e.printStackTrace();
            ErrorLogger.logError("Błąd podczas otwierania okna modalnego: " + fxmlFile + " - "  + e.getMessage());
        }
    }

    public void handleCategoryDictionary() {
        openModalWindowMenu("/com/example/budgetapp/configurations/categories.fxml", "Słownik kategorii");
    }

    public void handleSubcategoryDictionary() {
        openModalWindowMenu("/com/example/budgetapp/configurations/subcategories.fxml", "Słownik podkategorii");
    }

    public void handleLogout() {
        try {
            Alert logoutDialog = new Alert(Alert.AlertType.CONFIRMATION);
            logoutDialog.setTitle("Potwierdzenie wylogowania");
            logoutDialog.setHeaderText("Czy na pewno chcesz się wylogować?");

            ButtonType buttonYes = new ButtonType("Tak", ButtonBar.ButtonData.OK_DONE);
            ButtonType buttonNo = new ButtonType("Nie", ButtonBar.ButtonData.CANCEL_CLOSE);
            logoutDialog.getButtonTypes().setAll(buttonYes, buttonNo);

            Optional<ButtonType> result = logoutDialog.showAndWait();

            if (result.isPresent() && result.get() == buttonYes) {
                Stage stage = (Stage) expensesTable.getScene().getWindow();
                stage.close();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/budgetapp/login.fxml"));
                AnchorPane root = loader.load();

                Stage newStage = new Stage();
                Scene scene = new Scene(root);
                newStage.setTitle("Logowanie");
                IconUtil.setAppIcon(newStage);
                newStage.setScene(scene);
                newStage.setResizable(false);
                newStage.show();
            }
        } catch (Exception e) {
            ErrorLogger.logError("Błąd podczas wylogowania: " + e.getMessage());
        }
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
            stage.setResizable(false);
            stage.showAndWait();
            loadExpenses();
        } catch (IOException e) {
            ErrorLogger.logError("Błąd otwierania okna modalnego menu: "+ fxmlFile + " - " + e.getMessage());
        }
    }

    private void recalculateAllDifferences() {
        try (Connection conn = DatabaseConnection.connect()) {
            int uid = Integer.parseInt(userId);

            String incomeQuery = """
                SELECT id, amount, month, year FROM incomes
                WHERE user_id = ?
                """;

            assert conn != null;
            PreparedStatement incomeStmt = conn.prepareStatement(incomeQuery);
            incomeStmt.setInt(1, uid);
            ResultSet incomeRs = incomeStmt.executeQuery();

            while (incomeRs.next()) {
                int incomeId = incomeRs.getInt("id");
                double incomeAmount = incomeRs.getDouble("amount");
                int month = incomeRs.getInt("month");
                int year = incomeRs.getInt("year");

                // Calculate the total expenditure
                PreparedStatement expStmt = conn.prepareStatement("""
                SELECT SUM(amount) FROM expenses
                WHERE user_id = ? AND month = ? AND year = ?
                """);
                expStmt.setInt(1, uid);
                expStmt.setInt(2, month);
                expStmt.setInt(3, year);
                ResultSet expRs = expStmt.executeQuery();

                double totalExpense = expRs.next() ? expRs.getDouble(1) : 0.0;
                double diff = incomeAmount - totalExpense;

                // Save or update the difference
                insertOrUpdateSummary(conn, incomeId, 0, diff);
            }
        } catch (SQLException e) {
            ErrorLogger.logError("Błąd przeliczania wydatków: " + e.getMessage());
        }
    }

    private void insertOrUpdateSummary(Connection conn, int incomeId, Integer categoryId, double amount) throws SQLException {
        String query = """
        INSERT INTO income_expense_summary (income_id, category_id, amount)
        VALUES (?, ?, ?)
        ON CONFLICT (income_id, category_id)
        DO UPDATE SET amount = EXCLUDED.amount
        """;
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, incomeId);
            stmt.setInt(2, categoryId != null ? categoryId : 0);
            stmt.setDouble(3, amount);
            stmt.executeUpdate();
        }
    }
}