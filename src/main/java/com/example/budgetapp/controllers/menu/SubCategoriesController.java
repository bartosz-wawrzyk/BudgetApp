package com.example.budgetapp.controllers.menu;

import com.example.budgetapp.models.SubCategoriesRecord;
import com.example.budgetapp.models.CategoriesRecord;
import com.example.budgetapp.database.DatabaseConnection;
import com.example.budgetapp.utils.AlertsController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;

public class SubCategoriesController {

    @FXML
    private TableView<SubCategoriesRecord> subcategoriesTableView;

    @FXML
    private TableColumn<SubCategoriesRecord, Integer> columnID;

    @FXML
    private TableColumn<SubCategoriesRecord, String> columnName;

    @FXML
    private TableColumn<SubCategoriesRecord, String> columnCategory;

    @FXML
    private Button buttonAdd, buttonEdit, buttonDelete, buttonAccept, buttonCancel;

    @FXML
    private TextField subcategoryNameField;

    @FXML
    private ComboBox<String> comboboxCategory;

    private final ObservableList<SubCategoriesRecord> subcategories = FXCollections.observableArrayList();
    private final ObservableList<CategoriesRecord> categoriesList = FXCollections.observableArrayList();

    private Connection connection;
    private boolean isEditing = false;
    private SubCategoriesRecord selectedSubCategory = null;

    @FXML
    public void initialize() {
        columnID.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        columnName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        columnCategory.setCellValueFactory(cellData -> cellData.getValue().categoryNameProperty());

        subcategoriesTableView.setItems(subcategories);

        comboboxCategory.setDisable(true);
        subcategoryNameField.setDisable(true);

        buttonAccept.setDisable(true);
        buttonCancel.setDisable(true);

        connectToDatabase();
        loadCategories();
        loadSubCategories();
    }

    private void connectToDatabase() {
        connection = DatabaseConnection.connect();
    }

    private void loadCategories() {
        categoriesList.clear();
        if (connection != null) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, name FROM categories")) {
                while (rs.next()) {
                    categoriesList.add(new CategoriesRecord(rs.getInt("id"), rs.getString("name")));
                }
                comboboxCategory.setItems(FXCollections.observableArrayList(categoriesList.stream().map(CategoriesRecord::getName).toArray(String[]::new))); // Populate ComboBox
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadSubCategories() {
        subcategories.clear();
        if (connection != null) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT sb.id, sb.name, (SELECT name FROM categories WHERE id = sb.id_category) AS category_name FROM subcategories sb")) {
                while (rs.next()) {
                    String categoryName = rs.getString("category_name");
                    subcategories.add(new SubCategoriesRecord(rs.getInt("id"), rs.getString("name"), categoryName));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleAdd() {
        disableActionButtons(true);
        subcategoryNameField.setDisable(false);
        subcategoryNameField.clear();
        comboboxCategory.setDisable(false);
        comboboxCategory.requestFocus();
        subcategoriesTableView.refresh();
    }

    @FXML
    private void handleEdit() {
        selectedSubCategory = subcategoriesTableView.getSelectionModel().getSelectedItem();
        if (selectedSubCategory != null) {
            isEditing = true;
            disableActionButtons(true);
            subcategoryNameField.setDisable(false);
            subcategoryNameField.setText(selectedSubCategory.getName());
            comboboxCategory.setDisable(false);
            comboboxCategory.setValue(selectedSubCategory.getCategoryName());
            subcategoryNameField.requestFocus();
        }
    }

    @FXML
    private void handleDelete() {
        selectedSubCategory = subcategoriesTableView.getSelectionModel().getSelectedItem();
        if (selectedSubCategory != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Czy na pewno chcesz usunąć podkategorię?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES && connection != null) {
                    try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM subcategories WHERE id = ?")) {
                        stmt.setInt(1, selectedSubCategory.getId());
                        stmt.executeUpdate();
                        subcategories.remove(selectedSubCategory);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @FXML
    private void handleAccept() {
        String subCategoryName = subcategoryNameField.getText().trim();
        String selectedCategory = comboboxCategory.getValue();

        if (subCategoryName.isEmpty() || selectedCategory == null) {
            AlertsController.showAlert("Błąd", "Nazwa podkategorii i kategoria muszą być wypełnione.", Alert.AlertType.WARNING);
            return;
        }

        if (isEditing && selectedSubCategory != null && connection != null) {
            selectedSubCategory.setName(subCategoryName);
            int categoryId = getCategoryIdByName(selectedCategory);
            try (PreparedStatement stmt = connection.prepareStatement("UPDATE subcategories SET name = ?, id_category = ? WHERE id = ?")) {
                stmt.setString(1, selectedSubCategory.getName());
                stmt.setInt(2, categoryId);
                stmt.setInt(3, selectedSubCategory.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            subcategoriesTableView.refresh();
        } else {
            if (connection != null) {
                int categoryId = getCategoryIdByName(selectedCategory);
                try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO subcategories (name, id_category) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, subCategoryName);
                    stmt.setInt(2, categoryId);

                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows > 0) {
                        try (ResultSet rs = stmt.getGeneratedKeys()) {
                            if (rs.next()) {
                                int generatedId = rs.getInt(1);
                                subcategories.add(new SubCategoriesRecord(generatedId, subCategoryName, selectedCategory));
                                subcategoriesTableView.refresh();
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
            subcategoriesTableView.refresh();
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
        selectedSubCategory = null;
        subcategoryNameField.setDisable(true);
        subcategoryNameField.clear();
        comboboxCategory.setDisable(true);
    }

    private int getCategoryIdByName(String categoryName) {
        for (CategoriesRecord category : categoriesList) {
            if (category.getName().equals(categoryName)) {
                return category.getId();
            }
        }
        return -1;
    }
}