package com.example.budgetapp.models;

import javafx.beans.property.*;

public class SubCategoriesRecord {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty categoryName;

    public SubCategoriesRecord(int id, String name, String categoryName) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.categoryName = new SimpleStringProperty(categoryName);
    }

    public Integer getId() {
        return id.get();
    }

    public void setId(Integer newId) {
        this.id.set(newId);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String newName) {
        this.name.set(newName);
    }

    public String getCategoryName() {
        return categoryName.get();
    }

    public void setCategoryName(String newCategoryName) {
        this.categoryName.set(newCategoryName);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty categoryNameProperty() {
        return categoryName;
    }
}