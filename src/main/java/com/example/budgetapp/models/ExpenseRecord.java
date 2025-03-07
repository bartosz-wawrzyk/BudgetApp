package com.example.budgetapp.models;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ExpenseRecord {

    private final StringProperty month;
    private final StringProperty year;
    private final DoubleProperty income;
    private final DoubleProperty rent;
    private final DoubleProperty adminRent;

    public ExpenseRecord(String month, String year, double income, double rent, double adminRent) {
        this.month = new SimpleStringProperty(month);
        this.year = new SimpleStringProperty(year);
        this.income = new SimpleDoubleProperty(income);
        this.rent = new SimpleDoubleProperty(rent);
        this.adminRent = new SimpleDoubleProperty(adminRent);
    }

    public String getMonth() {
        return month.get();
    }

    public String getYear() {
        return year.get();
    }

    public double getIncome() {
        return income.get();
    }

    public double getRent() {
        return rent.get();
    }

    public double getAdminRent() {
        return adminRent.get();
    }
}
