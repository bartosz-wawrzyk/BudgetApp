module com.example.budgetapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.budgetapp to javafx.fxml;
    exports com.example.budgetapp;
    exports com.example.budgetapp.controllers;
    opens com.example.budgetapp.controllers to javafx.fxml;
    exports com.example.budgetapp.database;
    opens com.example.budgetapp.database to javafx.fxml;
}