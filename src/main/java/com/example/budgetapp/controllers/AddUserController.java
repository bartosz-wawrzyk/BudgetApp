package com.example.budgetapp.controllers;

import com.example.budgetapp.database.DatabaseConnection;
import com.example.budgetapp.utils.AlertsController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddUserController {

    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label messageLabel;

    @FXML
    private void handleSave() {
        String login = loginField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (login.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            messageLabel.setText("Wszystkie pola muszą być wypełnione.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Hasła muszą być identyczne.");
            return;
        }

        if (DatabaseConnection.isUserExist(login)) {
            messageLabel.setText("Użytkownik o tym loginie już istnieje.");
            return;
        }

        if (DatabaseConnection.addUser(login, password)) {
            AlertsController.showAlert("Sukces", "Użytkownik został pomyślnie dodany.", Alert.AlertType.INFORMATION);
            closeCurrentWindow();
        } else {
            messageLabel.setText("Błąd podczas dodawania użytkownika.");
        }
    }

    @FXML
    private void handleCancel() {
        closeCurrentWindow();
    }

    private void closeCurrentWindow() {
        Stage stage = (Stage) loginField.getScene().getWindow();
        stage.close();
    }
}
