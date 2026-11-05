import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class BankManagementSystem {
    private static final Scanner sc = new Scanner(System.in);
    private static final BankDAO dao = new BankDAO();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n========== BANK MANAGEMENT SYSTEM ==========");
            System.out.println("1. Create Account");
            System.out.println("2. View All Accounts");
            System.out.println("3. Search Account");
            System.out.println("4. Deposit Money");
            System.out.println("5. Withdraw Money");
            System.out.println("6. Transaction History");
            System.out.println("7. Transfer Money");
            System.out.println("8. Bank Statement");
            System.out.println("9. Mini Statement");
            System.out.println("10. Balance Inquiry");
            System.out.println("11. Set / Change PIN");
            System.out.println("12. Exit");
            System.out.print("Enter your choice: ");

           String input = sc.nextLine().trim();

           if (input.isEmpty()) {
           continue;
}

           int choice;

            try {
                choice = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                continue;
            }

            try {
                switch (choice) {
                    case 1:
                        createAccount();
                        break;
                    case 2:
                        viewAccounts();
                        break;
                    case 3:
                        searchAccount();
                        break;
                    case 4:
                        depositMoney();
                        break;
                    case 5:
                        withdrawMoney();
                        break;
                    case 6:
                        transactionHistory();
                        break;
                    case 7:
                        transferMoney();
                        break;
                    case 8:
                        System.out.println("Enter account number:");
                        long statementAccount = Long.parseLong(sc.nextLine().trim());
                        dao.bankStatement(statementAccount);
                        break;
                    case 9:
                        miniStatement();
                        break;
                    case 10:
                        checkBalance();
                        break;
                    case 11:
                        changePin();
                        break;
                    case 12:
                        System.out.println("Thank you for using our bank system!");
                        sc.close();
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            } catch (SQLException e) {
                System.out.println("Database operation failed: " + e.getMessage());
            }
        }
    }

    
private static void createAccount() throws SQLException {
    System.out.print("Enter account holder name: ");
    String name = sc.nextLine().trim();

    if (name.isEmpty()) {
        System.out.println("Name cannot be empty.");
        return;
    }

    BigDecimal balance = readAmount("Enter initial deposit: ");

    if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
        System.out.println("Initial deposit cannot be negative.");
        return;
    }

    if (dao.createAccount(name, balance)) {
        System.out.println("Account created successfully!");
    } else {
        System.out.println("Account creation failed.");
    }
}


    private static void viewAccounts() throws SQLException {
        List<BankAccount> accounts = dao.getAllAccounts();
        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }
        System.out.println("\n--- All Bank Accounts ---");
        for (BankAccount account : accounts) {
            System.out.println("Account Number: " + account.getAccountNumber());
            System.out.println("Holder: " + account.getAccountHolder());
            System.out.println("Balance: " + account.getBalance());
            System.out.println("----------------------");
        }
    }

   
private static void searchAccount() throws SQLException {
    long number = readAccountNumber();

    if (number <= 0) {
        return;
    }

    System.out.println("Enter your 4-digit PIN:");
    String pin = sc.nextLine();

    if (!dao.verifyAccountPin(number, pin)) {
        System.out.println("Incorrect PIN. Account search cancelled.");
        return;
    }

    BankAccount account = dao.findAccount(number);

    if (account == null) {
        System.out.println("Account not found.");
    } else {
        System.out.println("Account Number: " + account.getAccountNumber());
        System.out.println("Holder: " + account.getAccountHolder());
        System.out.println("Balance: Rs. " + account.getBalance());
    }
}

    private static void depositMoney() throws SQLException {
        long number = readAccountNumber();
        if (number <= 0) {
            return;
        }

        BigDecimal amount = readAmount("Enter deposit amount: ");
        if (amount == null) {
            return;
        }

        if (dao.deposit(number, amount)) {
            System.out.println("Deposit successful!");
            dao.recordTransaction(number, "DEPOSIT", amount);
        } else {
            System.out.println("Deposit failed. Check account number and amount.");
        }
    }

    
private static void withdrawMoney() throws SQLException {
    long number = readAccountNumber();

    if (number <= 0) {
        return;
    }

    BigDecimal amount = readAmount("Enter withdrawal amount: ");

    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
        System.out.println("Withdrawal amount must be greater than zero.");
        return;
    }

    System.out.println("Enter your 4-digit PIN:");
    String pin = sc.nextLine();

    if (!dao.verifyAccountPin(number, pin)) {
        System.out.println("Incorrect PIN. Withdrawal cancelled.");
        return;
    }

    if (dao.withdraw(number, amount)) {
        System.out.println("Withdrawal successful!");
        dao.recordTransaction(number, "WITHDRAWAL", amount);
    } else {
        System.out.println("Withdrawal failed. Check account number, amount, or balance.");
    }
}

    private static long readAccountNumber() {
        System.out.print("Enter account number: ");
        try {
            long number = Long.parseLong(sc.nextLine().trim());
            if (number <= 0) {
                System.out.println("Account number must be positive.");
                return -1;
            }
            return number;
        } catch (NumberFormatException e) {
            System.out.println("Invalid account number.");
            return -1;
        }
    }

    public static BigDecimal readAmount(String message) {
        System.out.print(message);
        try {
            BigDecimal amount = new BigDecimal(sc.nextLine().trim());
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                System.out.println("Amount cannot be negative.");
                return null;
            }
            return amount;
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid amount.");
            return null;
        }
    }

    private static void transactionHistory() throws SQLException {
        long accountNumber = readAccountNumber();
        if (accountNumber <= 0) {
            return;
        }
        dao.viewTransactionHistory(accountNumber);
    }
 
private static void transferMoney() throws SQLException {
    System.out.println("Enter sender account number:");
    long senderAccount = readAccountNumber();

    if (senderAccount <= 0) {
        return;
    }

    System.out.println("Enter receiver account number:");
    long receiverAccount = readAccountNumber();

    if (receiverAccount <= 0) {
        return;
    }

    if (senderAccount == receiverAccount) {
        System.out.println("Cannot transfer to the same account.");
        return;
    }

    BigDecimal amount = readAmount("Enter transfer amount: ");

    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
        System.out.println("Invalid amount.");
        return;
    }

    System.out.println("Enter your 4-digit PIN:");
    String pin = sc.nextLine();

    if (!dao.verifyAccountPin(senderAccount, pin)) {
        System.out.println("Incorrect PIN. Transfer cancelled.");
        return;
    }

    dao.transferMoney(senderAccount, receiverAccount, amount);
}


    private static void miniStatement() throws SQLException {
        long accountNumber = readAccountNumber();
        if (accountNumber <= 0) {
            return;
        }
        dao.miniStatement(accountNumber);
    }

    
private static void checkBalance() throws SQLException {
    long accountNumber = readAccountNumber();

    if (accountNumber <= 0) {
        return;
    }

    System.out.println("Enter your 4-digit PIN:");
    String pin = sc.nextLine();

    if (!dao.verifyAccountPin(accountNumber, pin)) {
        System.out.println("Incorrect PIN. Balance inquiry cancelled.");
        return;
    }

    dao.checkBalance(accountNumber);
}

    
    private static void changePin() throws SQLException {
    long accountNumber = readAccountNumber();

    if (accountNumber <= 0) {
        return;
    }

    System.out.println("Enter your current 4-digit PIN:");
    String oldPin = sc.nextLine();

    if (!dao.verifyAccountPin(accountNumber, oldPin)) {
        System.out.println("Incorrect PIN. PIN change cancelled.");
        return;
    }

    System.out.println("Enter new 4-digit PIN:");
    String newPin = sc.nextLine();

    if (!newPin.matches("\\d{4}")) {
        System.out.println("PIN must contain exactly 4 digits.");
        return;
    }

    if (oldPin.equals(newPin)) {
        System.out.println("New PIN must be different from the current PIN.");
        return;
    }

    dao.setAccountPin(accountNumber, newPin);
    }
   

}
