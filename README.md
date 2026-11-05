# Bank Management System

A console-based Bank Management System developed using **Java** and **Oracle Database**. This project demonstrates object-oriented programming, JDBC connectivity, SQL operations, and transaction handling.

## Features

- Create and manage bank accounts
- Deposit and withdraw money
- Transfer money between accounts
- Check account balance
- View transaction history
- Generate mini statements and bank statements
- Set and change account PIN
- Store PINs using hashing

## Technologies Used

- Java
- Oracle Database 11g XE
- JDBC
- Oracle JDBC Driver
- Git and GitHub

## Project Structure

```text
BankManagementSystem/
├── src/
│   ├── BankAccount.java
│   ├── BankDAO.java
│   ├── BankManagementSystem.java
│   ├── DBConnection.java
│   └── PinUtil.java
├── lib/
│   └── ojdbc17.jar
├── out/              # Compiled files (not committed)
├── .gitignore
└── README.md
```

## Requirements

- Java JDK
- Oracle Database
- Oracle JDBC driver (`ojdbc17.jar`)
- Git (optional)

## Database Configuration

Configure the database connection in `src/DBConnection.java`.

The application expects the following environment variables:

- `DB_USERNAME`
- `DB_PASSWORD`

Set these variables to your own Oracle database credentials. Do not commit database passwords or secrets to GitHub.

## Compile and Run (Windows)

Open the terminal in the project directory.

Compile:

```cmd
javac -cp "lib\ojdbc17.jar" -d out src\*.java
```

Run:

```cmd
java -cp "out;lib\ojdbc17.jar" BankManagementSystem
```

## Learning Objectives

This project was created to practice:

- Java OOP concepts
- JDBC and Oracle database connectivity
- SQL queries and database operations
- Transaction handling
- Exception handling
- Organizing a Java project into multiple classes

## Author

**Jasobanta Mahar**

GitHub: [@jasobantamahar](https://github.com/jasobantamahar)
