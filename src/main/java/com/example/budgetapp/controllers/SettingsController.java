package com.example.budgetapp.controllers;

import com.example.budgetapp.database.ConfigManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SettingsController {

    @FXML
    private TextField hostField;
    @FXML
    private TextField portField;
    @FXML
    private TextField databaseField;
    @FXML
    private TextField userField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleSave() {
        if (hostField.getText().isEmpty() || portField.getText().isEmpty() ||
                databaseField.getText().isEmpty() || userField.getText().isEmpty() ||
                passwordField.getText().isEmpty()) {

            showAlert("Wszystkie pola muszą być uzupełnione!", Alert.AlertType.ERROR);
            return;
        }

        ConfigManager.saveConfig(
                hostField.getText(),
                portField.getText(),
                databaseField.getText(),
                userField.getText(),
                passwordField.getText()
        );

        Stage stage = (Stage) hostField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) hostField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
