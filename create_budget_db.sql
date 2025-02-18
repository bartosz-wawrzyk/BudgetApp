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