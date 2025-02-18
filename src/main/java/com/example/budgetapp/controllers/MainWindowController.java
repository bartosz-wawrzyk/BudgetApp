package com.example.budgetapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainWindowController {

    @FXML
    private Label userIdLabel;

    private String userId;

    public void setUserId(String userId) {
        this.userId = userId;
        userIdLabel.setText("User ID: " + userId);
    }
}
