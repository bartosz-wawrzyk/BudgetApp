package com.example.budgetapp.models;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DetailItem {
    private final StringProperty category;
    private final StringProperty subcategory;
    private DoubleProperty amount;

    public DetailItem(String category, String subcategory, double amount) {
        this.category = new SimpleStringProperty(category);
        this.subcategory = new SimpleStringProperty(subcategory);
        this.amount = new SimpleDoubleProperty(amount);
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public StringProperty subcategoryProperty() {
        return subcategory;
    }

    public DoubleProperty amountProperty() {
        return amount;
    }

    public String getCategory() {
        return category.get();
    }
    public String getSubcategory() {
        return subcategory.get();
    }

    public DoubleProperty getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }
}
