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
