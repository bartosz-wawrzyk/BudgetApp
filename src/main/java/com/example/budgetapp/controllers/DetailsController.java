package com.example.budgetapp.controllers;

import com.example.budgetapp.database.DatabaseConnection;
import com.example.budgetapp.models.ExpenseRecord;
import com.example.budgetapp.models.DetailItem;
import com.example.budgetapp.utils.AlertsController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;

public class DetailsController {

    @FXML private Label headerLabel;
    @FXML private TableView<DetailItem> detailsTable;
    @FXML private TableColumn<DetailItem, String> categoryColumn;
    @FXML private TableColumn<DetailItem, String> subcategoryColumn;
    @FXML private TableColumn<DetailItem, Double> amountColumn;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> subcategoryComboBox;
    @FXML private TextField amountField;
    @FXML private Button addButton;
    @FXML private Button deleteButton;
    @FXML private Button editButton;
    @FXML private Button acceptButton;
    @FXML private Button cancelButton;

    private final ObservableList<DetailItem> data = FXCollections.observableArrayList();
    private String userId;
    private ExpenseRecord expense;
    private DetailItem itemBeingEdited = null;

    @FXML
    public void initialize() {
        loadCategories();
        resetFormState();
    }

    public void setExpenseData(ExpenseRecord expense, String userId) {
        this.expense = expense;
        this.userId = userId;

        headerLabel.setText("Szczegóły wydatków dla: " + expense.getMonth() + "/" + expense.getYear());

        categoryColumn.setCellValueFactory(c -> c.getValue().categoryProperty());
        subcategoryColumn.setCellValueFactory(c -> c.getValue().subcategoryProperty());
        amountColumn.setCellValueFactory(c -> c.getValue().amountProperty().asObject());

        detailsTable.setItems(data);
        loadExpenseDetails();
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

        categoryComboBox.setOnAction(event -> loadSubcategories());
    }

    private void loadSubcategories() {
        subcategoryComboBox.getItems().clear();
        String selectedCategory = categoryComboBox.getValue();

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

    private void loadExpenseDetails() {
        data.clear();
        try (Connection conn = DatabaseConnection.connect()) {
            String query = """
                SELECT c.name as category, s.name as subcategory, e.amount
                FROM expenses e
                JOIN subcategories s ON e.id_subcategory = s.id
                JOIN categories c ON s.id_category = c.id
                WHERE e.user_id = ? AND e.month = ? AND e.year = ?
            """;

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, Integer.parseInt(userId));
            stmt.setInt(2, Integer.parseInt(expense.getMonth()));
            stmt.setInt(3, Integer.parseInt(expense.getYear()));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                data.add(new DetailItem(rs.getString("category"), rs.getString("subcategory"), rs.getDouble("amount")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void resetFormState() {
        categoryComboBox.setDisable(true);
        subcategoryComboBox.setDisable(true);
        amountField.setDisable(true);

        categoryComboBox.setValue(null);
        subcategoryComboBox.setValue(null);
        amountField.clear();

        addButton.setDisable(false);
        deleteButton.setDisable(false);
        editButton.setDisable(false);

        acceptButton.setDisable(true);
        cancelButton.setDisable(true);
    }

    @FXML
    private void handleAdd() {
        categoryComboBox.setDisable(false);
        subcategoryComboBox.setDisable(false);
        amountField.setDisable(false);

        addButton.setDisable(true);
        deleteButton.setDisable(true);
        editButton.setDisable(true);

        acceptButton.setDisable(false);
        cancelButton.setDisable(false);
    }

    @FXML
    private void handleAccept() {
        String amountText = amountField.getText();

        try (Connection conn = DatabaseConnection.connect()) {
            double amount = Double.parseDouble(amountText);

            if (itemBeingEdited == null) {
                String category = categoryComboBox.getValue();
                String subcategory = subcategoryComboBox.getValue();

                if (category == null || subcategory == null || amountText.isEmpty()) return;

                String insert = """
                INSERT INTO expenses (amount, id_subcategory, month, year, user_id)
                VALUES (?, (SELECT s.id FROM subcategories s JOIN categories c ON s.id_category = c.id
                            WHERE s.name = ? AND c.name = ?), ?, ?, ?)
            """;

                PreparedStatement stmt = conn.prepareStatement(insert);
                stmt.setDouble(1, amount);
                stmt.setString(2, subcategory);
                stmt.setString(3, category);
                stmt.setInt(4, Integer.parseInt(expense.getMonth()));
                stmt.setInt(5, Integer.parseInt(expense.getYear()));
                stmt.setInt(6, Integer.parseInt(userId));

                stmt.executeUpdate();

            } else {
                String update = """
                UPDATE expenses SET amount = ?
                WHERE user_id = ? AND month = ? AND year = ?
                AND id_subcategory = (
                    SELECT s.id FROM subcategories s
                    JOIN categories c ON s.id_category = c.id
                    WHERE s.name = ? AND c.name = ?
                )
            """;

                PreparedStatement stmt = conn.prepareStatement(update);
                stmt.setDouble(1, amount);
                stmt.setInt(2, Integer.parseInt(userId));
                stmt.setInt(3, Integer.parseInt(expense.getMonth()));
                stmt.setInt(4, Integer.parseInt(expense.getYear()));
                stmt.setString(5, itemBeingEdited.getSubcategory());
                stmt.setString(6, itemBeingEdited.getCategory());

                stmt.executeUpdate();

                itemBeingEdited.setAmount(amount);
                detailsTable.refresh();
                itemBeingEdited = null;
            }

            loadExpenseDetails();

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }

        resetFormState();
    }

    @FXML
    private void handleDelete() {
        ObservableList<DetailItem> selectedItems = detailsTable.getSelectionModel().getSelectedItems();

        if (selectedItems.isEmpty()) {
            AlertsController.showAlert("Błąd", "Zaznacz jeden wpis z tabeli.", Alert.AlertType.WARNING);
            return;
        }

        if (selectedItems.size() > 1) {
            AlertsController.showAlert("Błąd", "ożna usunąć tylko jeden wpis na raz.", Alert.AlertType.WARNING);
            return;
        }

        DetailItem selected = selectedItems.get(0);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Potwierdzenie usunięcia");
        confirm.setHeaderText("Czy na pewno chcesz usunąć wpis?");
        confirm.setContentText(selected.getCategory() + " → " + selected.getSubcategory());

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConnection.connect()) {
                    String delete = """
                    DELETE FROM expenses
                    WHERE user_id = ? AND month = ? AND year = ?
                    AND id_subcategory = (
                        SELECT s.id FROM subcategories s
                        JOIN categories c ON s.id_category = c.id
                        WHERE s.name = ? AND c.name = ?
                    )
                """;

                    PreparedStatement stmt = conn.prepareStatement(delete);
                    stmt.setInt(1, Integer.parseInt(userId));
                    stmt.setInt(2, Integer.parseInt(expense.getMonth()));
                    stmt.setInt(3, Integer.parseInt(expense.getYear()));
                    stmt.setString(4, selected.getSubcategory());
                    stmt.setString(5, selected.getCategory());

                    stmt.executeUpdate();
                    data.remove(selected);

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleEdit() {
        ObservableList<DetailItem> selectedItems = detailsTable.getSelectionModel().getSelectedItems();

        if (selectedItems.isEmpty()) {
            AlertsController.showAlert("Błąd", "Zaznacz jeden wpis z tabeli.", Alert.AlertType.WARNING);
            return;
        }

        if (selectedItems.size() > 1) {
            AlertsController.showAlert("Błąd", "Można edytować tylko jeden wpis na raz.", Alert.AlertType.WARNING);
            return;
        }

        itemBeingEdited = selectedItems.get(0);

        amountField.setText(String.valueOf(itemBeingEdited.getAmount().get()));
        amountField.setDisable(false);

        addButton.setDisable(true);
        deleteButton.setDisable(true);
        editButton.setDisable(true);

        acceptButton.setDisable(false);
        cancelButton.setDisable(false);
    }

    @FXML
    private void handleCancel() {
        itemBeingEdited = null;
        resetFormState();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) headerLabel.getScene().getWindow();
        stage.close();
    }
}