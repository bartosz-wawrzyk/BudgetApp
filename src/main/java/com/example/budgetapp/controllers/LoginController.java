package com.example.budgetapp.controllers;

import com.example.budgetapp.Main;
import com.example.budgetapp.database.DatabaseConnection;
import com.example.budgetapp.utils.IconUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;

    @FXML
    private AnchorPane loginPane;

    @FXML
    private void handleLogin() {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Wprowadź login i hasło.");
            return;
        }

        if (DatabaseConnection.validateLogin(login, password)) {
            String userId = DatabaseConnection.getUserIdByLogin(login);

            if (userId != null) {
                closeCurrentWindow();
                openNewWindow(userId);
            } else {
                messageLabel.setText("Nie znaleziono użytkownika.");
            }
        } else {
            messageLabel.setText("Błędne dane logowania.");
        }
    }

    @FXML
    public void openSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/example/budgetapp/configurations/settings.fxml"));
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Konfiguracja bazy danych");
            IconUtil.setAppIcon(settingsStage);
            settingsStage.setScene(new javafx.scene.Scene(loader.load()));

            settingsStage.initModality(Modality.APPLICATION_MODAL);
            settingsStage.setResizable(false);
            settingsStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void closeCurrentWindow() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }

    private void openNewWindow(String userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/budgetapp/main/mainWindow.fxml"));
            BorderPane root = loader.load();

            // Pass userId to new controller
            MainWindowController controller = loader.getController();
            controller.setUserId(userId);

            Stage newStage = new Stage();
            Scene scene = new Scene(root);
            newStage.setTitle("Aplikacja do zarządzania budżetem domowym");
            IconUtil.setAppIcon(newStage);
            newStage.setScene(scene);
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Potwierdzenie zamknięcia");
        confirmDialog.setHeaderText("Czy na pewno chcesz zamknąć aplikację?");

        ButtonType buttonYes = new ButtonType("Tak", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonNo = new ButtonType("Nie", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(buttonYes, buttonNo);

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == buttonYes) {
            closeApplication();
        }
    }

    private void closeApplication() {
        Stage stage = (Stage) loginPane.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void openUserRegistration() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/example/budgetapp/configurations/addUser.fxml"));
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Dodanie nowego użytkownika");
            IconUtil.setAppIcon(settingsStage);
            settingsStage.setScene(new javafx.scene.Scene(loader.load()));

            settingsStage.initModality(Modality.APPLICATION_MODAL);
            settingsStage.setResizable(false);
            settingsStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}