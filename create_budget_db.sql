-- Creation of a ‘budget’ database
CREATE DATABASE budget;

-- Make sure you have the pgcrypto extension (if not, install it)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Creation of ‘users’ table
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       login VARCHAR(255) UNIQUE NOT NULL,
                       password TEXT NOT NULL
);

-- Creation of ‘categories’ table
CREATE TABLE categories (
                            id SERIAL PRIMARY KEY,
                            name VARCHAR(255) NOT NULL
);

-- Creation of ‘subcategories’ table
CREATE TABLE subcategories (
                               id SERIAL PRIMARY KEY,
                               name VARCHAR(255) NOT NULL,
                               id_category INT NOT NULL,
                               FOREIGN KEY (id_category) REFERENCES categories(id)
);

-- Creation of ‘expanses’ main table
CREATE TABLE expenses (
                          id SERIAL PRIMARY KEY,
                          amount DECIMAL(10, 2) NOT NULL,
                          id_category INT NOT NULL,
                          month INT CHECK (month >= 1 AND month <= 12) NOT NULL,
    year INT CHECK (year >= 1900) NOT NULL,
    user_id INT NOT NULL,
    FOREIGN KEY (id_category) REFERENCES categories(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Creation of a table for financial differences
CREATE TABLE financial_differences (
                                       id SERIAL PRIMARY KEY,
                                       total DOUBLE PRECISION NOT NULL,
                                       id_income INTEGER NOT NULL,
                                       id_categories INTEGER NOT NULL,
                                       CONSTRAINT fk_income FOREIGN KEY (id_income) REFERENCES incomes(id),
                                       CONSTRAINT fk_categories FOREIGN KEY (id_categories) REFERENCES categories(id)
);

-- Creation of a table to store column display configurations in the master table
CREATE TABLE table_configuration (
                                     id SERIAL PRIMARY KEY,
                                     user_id INTEGER NOT NULL,
                                     category_id INTEGER NOT NULL,
                                     FOREIGN KEY(user_id) REFERENCES users(id),
                                     FOREIGN KEY(category_id) REFERENCES categories(id)
);

-- Creation of a table to store the difference between income and expenditure
CREATE TABLE income_expense_summary (
                                        id SERIAL PRIMARY KEY,
                                        income_id INTEGER NOT NULL,
                                        category_id INTEGER NOT NULL DEFAULT 0,
                                        amount NUMERIC(10,2) NOT NULL,
                                        FOREIGN KEY (income_id) REFERENCES incomes(id),
                                        UNIQUE (income_id, category_id)
);

