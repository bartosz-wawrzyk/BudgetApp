package com.example.budgetapp.models;

import javafx.beans.property.*;

import java.util.HashMap;
import java.util.Map;

public class ExpenseRecord {
    private final StringProperty month;
    private final StringProperty year;
    private final DoubleProperty income;
    private final Map<Integer, DoubleProperty> subcategoryExpenses = new HashMap<>();

    public ExpenseRecord(String month, String year, double income) {
        this.month = new SimpleStringProperty(month);
        this.year = new SimpleStringProperty(year);
        this.income = new SimpleDoubleProperty(income);
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

    public void setSubcategoryExpense(int subcategoryId, double amount) {
        subcategoryExpenses.put(subcategoryId, new SimpleDoubleProperty(amount));
    }

    public DoubleProperty getSubcategoryExpense(int subcategoryId) {
        return subcategoryExpenses.getOrDefault(subcategoryId, new SimpleDoubleProperty(0));
    }

    private final Map<Integer, DoubleProperty> categoryExpenses = new HashMap<>();

    public void setCategoryExpense(int categoryId, double amount) {
        categoryExpenses.put(categoryId, new SimpleDoubleProperty(amount));
    }

    public DoubleProperty getCategoryExpense(int categoryId) {
        return categoryExpenses.getOrDefault(categoryId, new SimpleDoubleProperty(0));
    }

    private final Map<Integer, DoubleProperty> categorySums = new HashMap<>();

    public void setCategorySum(int categoryId, double amount) {
        categorySums.put(categoryId, new SimpleDoubleProperty(amount));
    }

    public DoubleProperty getCategorySum(int categoryId) {
        return categorySums.getOrDefault(categoryId, new SimpleDoubleProperty(0));
    }

    private final DoubleProperty difference = new SimpleDoubleProperty(0);

    public void setDifference(double diff) {
        this.difference.set(diff);
    }

    public double getDifference() {
        return difference.get();
    }

    public DoubleProperty differenceProperty() {
        return difference;
    }

    public double getTotalExpense() {
        return subcategoryExpenses.values().stream().mapToDouble(DoubleProperty::get).sum();
    }
}
