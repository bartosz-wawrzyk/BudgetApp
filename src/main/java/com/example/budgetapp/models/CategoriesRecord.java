package com.example.budgetapp.models;

import javafx.beans.property.*;

public class CategoriesRecord {
    private final IntegerProperty id;
    private final StringProperty name;

    public CategoriesRecord(Integer id, String name) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
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
}
