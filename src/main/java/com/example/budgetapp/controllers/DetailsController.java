package com.example.budgetapp.controllers;

import com.example.budgetapp.database.DatabaseConnection;
import com.example.budgetapp.models.ExpenseRecord;
import com.example.budgetapp.models.DetailItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;

public class DetailsController {
    @FXML
    private Label headerLabel;

    @FXML
    private TableView<DetailItem> detailsTable;

    @FXML
    private TableColumn<DetailItem, String> categoryColumn;

    @FXML
    private TableColumn<DetailItem, String> subcategoryColumn;

    @FXML
    private TableColumn<DetailItem, Double> amountColumn;

    private final ObservableList<DetailItem> data = FXCollections.observableArrayList();

    public void setExpenseData(ExpenseRecord expense, String userId) {
        String month = expense.getMonth();
        String year = expense.getYear();
        headerLabel.setText("Szczegóły dla: " + month + "/" + year);

        categoryColumn.setCellValueFactory(c -> c.getValue().categoryProperty());
        subcategoryColumn.setCellValueFactory(c -> c.getValue().subcategoryProperty());
        amountColumn.setCellValueFactory(c -> c.getValue().amountProperty().asObject());

        detailsTable.setItems(data);

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
            stmt.setInt(2, Integer.parseInt(month));
            stmt.setInt(3, Integer.parseInt(year));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String category = rs.getString("category");
                String subcategory = rs.getString("subcategory");
                double amount = rs.getDouble("amount");

                data.add(new DetailItem(category, subcategory, amount));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) headerLabel.getScene().getWindow();
        stage.close();
    }
}
