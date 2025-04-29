# Home Budget Management Application

This is a simple application for managing a home budget, created using JavaFX and PostgreSQL technologies.

## Description

The application allows users to track their expenses and income, categorize them, and generate reports. This allows users to easily control their finances and make better decisions regarding budget management.

## Technologies

* **JavaFX**: A library for creating user interfaces in Java applications.
* **PostgreSQL**: A relational database that is used to store application data.

## Features

* Adding and editing expenses and income.
* Categorizing expenses and income.
* Generating reports (e.g., monthly expense summary).
* Ability to view transaction history.

## How to run

1. Make sure you have installed the Java Development Kit (JDK) version 17 or higher.
2. Download and install PostgreSQL.
3. Configure the database connection in the application.
4. Run the application from the IntelliJ IDEA development environment or using the Maven tool.

## Building

The application can be compiled into an executable `.jar` file for easier distribution and launching.

To create a `.jar` file:
1. Make sure you have Maven installed.
2. Open a terminal in the project directory.
3. Run the command:  mvn clean package

## Screens and Modules

### Login Menu

Users must log in to access the main application. The login screen includes:
- Fields for username and password.
- Validation of login credentials via a PostgreSQL database.
- Options to:
  - Register a new user.
  - Open database connection settings.
  - Exit the application safely.

![image](https://github.com/user-attachments/assets/be6265ba-0872-49c1-889d-4aaeb9dac184)


### Main Menu

After successful login, users are taken to the main window, which includes:
- A dynamic table showing monthly income and categorized expenses.
- Ability to:
  - Add new income entries.
  - Configure visible expense categories.
  - View detailed information about a selected month.
  - Access dictionaries of categories and subcategories.
  - Log out and return to the login screen.

 ![image](https://github.com/user-attachments/assets/faf96b8e-0948-4ed2-8b4f-943c7a81ba51)


### Expense Details

When viewing details of a selected month, the application displays:
- A breakdown of expenses by subcategory.
- Exact income and spending amounts.


## Author

Bartosz Wawrzyk
