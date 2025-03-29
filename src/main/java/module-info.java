module com.example.budgetapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires java.prefs;


    opens com.example.budgetapp to javafx.fxml;
    exports com.example.budgetapp;
    exports com.example.budgetapp.controllers;
    opens com.example.budgetapp.controllers to javafx.fxml;
    exports com.example.budgetapp.database;
    opens com.example.budgetapp.database to javafx.fxml;
    opens com.example.budgetapp.models to javafx.base;
    opens com.example.budgetapp.controllers.menu to javafx.fxml;
    exports com.example.budgetapp.controllers.menu;
}