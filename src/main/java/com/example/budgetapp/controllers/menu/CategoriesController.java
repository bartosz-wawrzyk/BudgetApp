package com.example.budgetapp.controllers.menu;

import com.example.budgetapp.models.CategoriesRecord;
import com.example.budgetapp.database.DatabaseConnection;
import com.example.budgetapp.utils.AlertsController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;

public class CategoriesController {

    @FXML
    private TableView<CategoriesRecord> categoriestableView;

    @FXML
    private TableColumn<CategoriesRecord, Integer> columnID;

    @FXML
    private TableColumn<CategoriesRecord, String> columnName;

    @FXML
    private Button buttonAdd, buttonEdit, buttonDelete, buttonAccept, buttonCancel;

    @FXML
    private TextField categoryNameField;

    private final ObservableList<CategoriesRecord> categories = FXCollections.observableArrayList();
    private Connection connection;
    private boolean isEditing = false;
    private CategoriesRecord selectedCategory = null;

    @FXML
    public void initialize() {
        columnID.setCellValueFactory(new PropertyValueFactory<>("id"));
        columnName.setCellValueFactory(new PropertyValueFactory<>("name"));

        categoriestableView.setItems(categories);
        connectToDatabase();
        loadCategories();

        buttonAccept.setDisable(true);
        buttonCancel.setDisable(true);
        categoryNameField.setDisable(true);
    }

    private void connectToDatabase() {
        connection = DatabaseConnection.connect();
    }

    private void loadCategories() {
        categories.clear();
        if (connection != null) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM categories")) {
                while (rs.next()) {
                    categories.add(new CategoriesRecord(rs.getInt("id"), rs.getString("name")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleAdd() {
        disableActionButtons(true);
        categoryNameField.setDisable(false);
        categoryNameField.clear();
        categoryNameField.requestFocus();

        categoriestableView.refresh();
    }

    @FXML
    private void handleEdit() {
        selectedCategory = categoriestableView.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            isEditing = true;
            disableActionButtons(true);
            categoryNameField.setDisable(false);
            categoryNameField.setText(selectedCategory.getName());
            categoryNameField.requestFocus();
        }
    }

    @FXML
    private void handleDelete() {
        selectedCategory = categoriestableView.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Czy na pewno chcesz usunąć kategorię?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES && connection != null) {
                    try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM categories WHERE id = ?")) {
                        stmt.setInt(1, selectedCategory.getId());
                        stmt.executeUpdate();
                        categories.remove(selectedCategory);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @FXML
    private void handleAccept() {
        if (isEditing && selectedCategory != null && connection != null) {
            selectedCategory.setName(categoryNameField.getText());
            try (PreparedStatement stmt = connection.prepareStatement("UPDATE categories SET name = ? WHERE id = ?")) {
                stmt.setString(1, selectedCategory.getName());
                stmt.setInt(2, selectedCategory.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            categoriestableView.refresh();
        } else {
            String categoryName = categoryNameField.getText().trim();

            if (categoryName.isEmpty()) {
                AlertsController.showAlert("Błąd", "Nazwa kategorii nie może być pusta.", Alert.AlertType.WARNING);
                return;
            }

            if (connection != null) {
                try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO categories (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, categoryName);

                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows > 0) {
                        try (ResultSet rs = stmt.getGeneratedKeys()) {
                            if (rs.next()) {
                                int generatedId = rs.getInt(1);
                                CategoriesRecord newCategory = new CategoriesRecord(generatedId, categoryName);
                                categories.add(newCategory);
                                categoriestableView.refresh();
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        resetButtons();
    }

    @FXML
    private void handleCancel() {
        if (isEditing) {
            categoriestableView.refresh();
        }
        resetButtons();
    }

    private void disableActionButtons(boolean disable) {
        buttonAdd.setDisable(disable);
        buttonEdit.setDisable(disable);
        buttonDelete.setDisable(disable);
        buttonAccept.setDisable(!disable);
        buttonCancel.setDisable(!disable);
    }

    private void resetButtons() {
        disableActionButtons(false);
        isEditing = false;
        selectedCategory = null;
        categoryNameField.setDisable(true);
        categoryNameField.clear();
    }
}