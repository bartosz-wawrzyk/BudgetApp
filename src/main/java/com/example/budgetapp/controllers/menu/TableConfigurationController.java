package com.example.budgetapp.controllers.menu;

import com.example.budgetapp.utils.ErrorLogger;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

import com.example.budgetapp.database.DatabaseConnection;
import javafx.stage.Stage;

import static com.example.budgetapp.utils.AlertsController.showAlert;

public class TableConfigurationController {

    @FXML
    private VBox checkboxContainer;

    private String userId;

    public void setUserId(String userId) {
        this.userId = userId;

        if (userId != null && !userId.isEmpty()) {
            loadCategories();
        } else {
            ErrorLogger.logError("Błąd: userId jest NULL lub pusty!");
        }
    }

    private void loadCategories() {
        try (Connection conn = DatabaseConnection.connect()) {
            String query = "SELECT DISTINCT c.id, c.name FROM categories c " +
                    "JOIN subcategories s ON c.id = s.id_category " +
                    "JOIN expenses e ON s.id = e.id_subcategory " +
                    "WHERE e.user_id = ?";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, Integer.parseInt(userId));
            ResultSet rs = stmt.executeQuery();

            checkboxContainer.getChildren().clear();

            while (rs.next()) {
                int categoryId = rs.getInt("id");
                String categoryName = rs.getString("name");

                CheckBox checkBox = new CheckBox(categoryName);
                checkBox.setUserData(categoryId);
                checkBox.setStyle("-fx-text-fill: white;");
                checkboxContainer.getChildren().add(checkBox);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void saveConfiguration() {
        if (userId == null || userId.isEmpty()) {
            System.err.println("Błąd: userId jest null lub pusty!");
            return;
        }

        try (Connection conn = DatabaseConnection.connect()) {
            String deleteQuery = "DELETE FROM table_configuration WHERE user_id = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
            deleteStmt.setInt(1, Integer.parseInt(userId));
            deleteStmt.executeUpdate();

            boolean atLeastOneSelected = false;

            String insertQuery = "INSERT INTO table_configuration (user_id, category_id) VALUES (?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);

            for (Node node : checkboxContainer.getChildren()) {
                if (node instanceof CheckBox checkBox && checkBox.isSelected()) {
                    int categoryId = (int) checkBox.getUserData();
                    insertStmt.setInt(1, Integer.parseInt(userId));
                    insertStmt.setInt(2, categoryId);
                    insertStmt.addBatch();
                    atLeastOneSelected = true;
                }
            }

            if (atLeastOneSelected) {
                insertStmt.executeBatch();
                showAlert("Sukces", "Konfiguracja została zapisana.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Brak zaznaczeń", "Nie zaznaczyłeś żadnych kategorii. Konfiguracja nie została zapisana.", Alert.AlertType.WARNING);
            }

            Stage stage = (Stage) checkboxContainer.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Błąd", "Wystąpił błąd podczas zapisywania konfiguracji.", Alert.AlertType.ERROR);
        }
    }
}
