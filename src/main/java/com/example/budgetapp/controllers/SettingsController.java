package com.example.budgetapp.controllers;

import com.example.budgetapp.database.ConfigManager;
import com.example.budgetapp.utils.AlertsController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    private Label currentHostLabel;

    @FXML
    private Label currentPortLabel;

    @FXML
    private Label currentDatabaseLabel;

    @FXML
    private Label currentUserLabel;

    @FXML
    private void initialize() {
        currentHostLabel.setText("Host: " + ConfigManager.getHost());
        currentPortLabel.setText("Port: " + ConfigManager.getPort());
        currentDatabaseLabel.setText("Baza danych: " + ConfigManager.getDatabase());
        currentUserLabel.setText("Użytkownik: " + ConfigManager.getUser());
    }

    @FXML
    private void handleSave() {
        if (hostField.getText().isEmpty() || portField.getText().isEmpty() ||
                databaseField.getText().isEmpty() || userField.getText().isEmpty() ||
                passwordField.getText().isEmpty()) {

            AlertsController.showAlert("Błąd", "Wszystkie pola muszą być uzupełnione!", Alert.AlertType.ERROR);
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
}
