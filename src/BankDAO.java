import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BankDAO {
    public boolean createAccount(String accountHolder, BigDecimal initialBalance)
    throws SQLException {
        String sql = "INSERT INTO bank_accounts " + "(account_number, account_holder, balance) " + "VALUES (?, ?, ?)";
        String numberSql = "SELECT NVL(MAX(account_number), 1000) + 1 FROM bank_accounts";

        try (Connection con = DBConnection.getConnection();
        PreparedStatement numberStmt = con.prepareStatement(numberSql);
        ResultSet rs = numberStmt.executeQuery()) {
            long accountNumber = 1001;
            if (rs.next()) {
                accountNumber = rs.getLong(1);
            }

            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setLong(1, accountNumber);
                stmt.setString(2, accountHolder);
                stmt.setBigDecimal(3, initialBalance);

                return stmt.executeUpdate() == 1;
            }
        }
    }

    public List<BankAccount> getAllAccounts() throws SQLException {
        List<BankAccount> accounts = new ArrayList<> ();
        String sql = "SELECT account_number, account_holder, balance " + "FROM bank_accounts ORDER BY account_number";
        try (Connection con = DBConnection.getConnection();
        PreparedStatement stmt = con.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                accounts.add(new BankAccount(
                    rs.getLong("account_number"),
                    rs.getString("account_holder"),
                    rs.getBigDecimal("balance")
                ));
            }
        }
        return accounts;
    }
    public BankAccount findAccount(long accountNumber) throws SQLException {
        String sql = "SELECT account_number, account_holder, balance " + "FROM bank_accounts WHERE account_number = ?";

        try (Connection con = DBConnection.getConnection();
        PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setLong(1,accountNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new BankAccount(
                        rs.getLong("account_number"),
                        rs.getString("account_holder"),
                        rs.getBigDecimal("balance")
                    );
                }
            }
        }
        return null;
    }
    public boolean deposit(long accountNumber, BigDecimal amount)
    throws SQLException {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        String sql = "UPDATE bank_accounts " + "SET balance = balance + ? WHERE account_number = ?";
    
        try (Connection con = DBConnection.getConnection();
        PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setBigDecimal(1, amount);
            stmt.setLong(2,accountNumber);
            return stmt.executeUpdate() == 1;
        }
    }
    public boolean withdraw(long accountNumber, BigDecimal amount)
    throws SQLException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        String sql = "UPDATE bank_accounts " + "SET balance = balance - ? " + "WHERE account_number = ? AND balance >= ?";
        try (Connection con = DBConnection.getConnection();
        PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setBigDecimal(1, amount);
            stmt.setLong(2, accountNumber);
            stmt.setBigDecimal(3, amount);
            return stmt.executeUpdate() == 1;
        }
    }
    public boolean recordTransaction(
        long accountNumber,
        String transactionType,
        BigDecimal amount) throws SQLException {

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }
            String sql = "INSERT INTO bank_transactions " + "(transaction_id, account_number, transaction_type, amount)" + "VALUES(?, ?, ?, ?)";
            String idSql = "SELECT NVL(MAX(transaction_id), 0) + 1 FROM bank_transactions";

            try (Connection con = DBConnection.getConnection()) {
                con.setAutoCommit(false);
                try {
                    long transactionId;
                    try (PreparedStatement idStmt = con.prepareStatement(idSql);
                    ResultSet rs = idStmt.executeQuery()) {
                        rs.next();
                        transactionId = rs.getLong(1);
                    }
                    try (PreparedStatement stmt = con.prepareStatement(sql)) {
                        stmt.setLong(1, transactionId);
                        stmt.setLong(2, accountNumber);
                        stmt.setString(3, transactionType);
                        stmt.setBigDecimal(4, amount);
                        stmt.executeUpdate();
                    }
                    con.commit();
                    return true;
                } catch (SQLException e) {
                    con.rollback();
                    throw e;
                }
            }
        }

public void viewTransactionHistory(long accountNumber) throws SQLException {
    String sql = "SELECT transaction_id, transaction_type, amount, " + " transaction_date FROM bank_transactions " + "WHERE account_number = ? " + "ORDER BY transaction_id";
    try (Connection con = DBConnection.getConnection();
    PreparedStatement stmt = con.prepareStatement(sql)) {
        stmt.setLong(1, accountNumber);
        try (ResultSet rs = stmt.executeQuery()) {
            if (!rs.isBeforeFirst()) {
                System.out.println("No transactions history found.");
                return;
            }
            System.out.println("\n----- Transaction History -----");
            while (rs.next()) {
                System.out.println("Transaction ID: " + rs.getLong("transaction_id"));
                System.out.println("type: " + rs.getString("transaction_type"));
                System.out.println("Amount: " + rs.getBigDecimal("amount"));
                System.out.println("Date: " + rs.getTimestamp("transaction_date"));
                System.out.println("-------------------------------");
                }
            }
        }

    }
    public void transferMoney(long senderAccount,
                          long receiverAccount,
                          BigDecimal amount) throws SQLException {

    if (senderAccount == receiverAccount) {
        System.out.println("Cannot transfer to the same account.");
        return;
    }

    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
        System.out.println("Invalid transfer amount.");
        return;
    }

    String debitSQL =
            "UPDATE bank_accounts " +
            "SET balance = balance - ? " +
            "WHERE account_number = ? AND balance >= ?";

    String creditSQL =
            "UPDATE bank_accounts " +
            "SET balance = balance + ? " +
            "WHERE account_number = ?";

    String transactionSQL =
            "INSERT INTO bank_transactions " +
            "(transaction_id, account_number, transaction_type, amount) " +
            "VALUES (?, ?, ?, ?)";

    try (Connection con = DBConnection.getConnection()) {

        con.setAutoCommit(false);

        try {
            try (PreparedStatement debit = con.prepareStatement(debitSQL)) {
                debit.setBigDecimal(1, amount);
                debit.setLong(2, senderAccount);
                debit.setBigDecimal(3, amount);

                if (debit.executeUpdate() != 1) {
                    con.rollback();
                    System.out.println(
                            "Sender account not found or insufficient balance."
                    );
                    return;
                }
            }

            try (PreparedStatement credit = con.prepareStatement(creditSQL)) {
                credit.setBigDecimal(1, amount);
                credit.setLong(2, receiverAccount);

                if (credit.executeUpdate() != 1) {
                    con.rollback();
                    System.out.println("Receiver account not found.");
                    return;
                }
            }

            try (PreparedStatement transaction =
                         con.prepareStatement(transactionSQL)) {

              long firstId;
              long secondId;

            try (PreparedStatement idQuery = con.prepareStatement(
            "SELECT bank_transaction_seq.NEXTVAL FROM dual")) {

            try (ResultSet rs = idQuery.executeQuery()) {
            rs.next();
            firstId = rs.getLong(1);
    }
}

try (PreparedStatement idQuery = con.prepareStatement(
        "SELECT bank_transaction_seq.NEXTVAL FROM dual")) {

    try (ResultSet rs = idQuery.executeQuery()) {
        rs.next();
        secondId = rs.getLong(1);
    }
}
                transaction.setLong(1, firstId);
                transaction.setLong(2, senderAccount);
                transaction.setString(3, "TRANSFER_OUT");
                transaction.setBigDecimal(4, amount);
                transaction.executeUpdate();

                transaction.setLong(1, secondId);
                transaction.setLong(2, receiverAccount);
                transaction.setString(3, "TRANSFER_IN");
                transaction.setBigDecimal(4, amount);
                transaction.executeUpdate();
            }

            con.commit();
            System.out.println("Money transferred successfully.");

        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
        }
    }

    public void miniStatement(long accountNumber) throws SQLException {
        String sql =
        "SELECT * FROM (" +
        "SELECT transaction_id, transaction_type, amount, transaction_date " +
        "FROM bank_transactions " +
        "WHERE account_number = ? " +
        "ORDER BY transaction_id DESC" +
        ") WHERE ROWNUM <= 5";
        try (Connection con = DBConnection.getConnection();
        PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt. setLong(1, accountNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("No transaction history found.");
                    return;
                }
                System.out.println("\n===== MINI STATEMENT =====");
                while (rs.next()) {
                    System.out.println("Transaction ID: " + rs.getLong("transaction_id"));
                    System.out.println("Type: " + rs.getString("transaction_type"));
                    System.out.println("Amount: " + rs.getBigDecimal("amount"));
                    System.out.println("Date: " + rs.getTimestamp("transaction_date"));

                    System.out.println("---------------------------");
                }
            }
        }
    }
    public void checkBalance(long accountNumber) throws SQLException {
        String sql = "SELECT balance FROM bank_accounts " +
        "WHERE account_number = ?";
        try (Connection con = DBConnection.getConnection();
        PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setLong(1, accountNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Current Balance:Rs. " + rs.getBigDecimal("balance"));
                } else {
                    System.out.println("Account not found.");
                }
            }
        }
    }

    
public void setAccountPin(long accountNumber, String pin)
        throws SQLException {

    String hashedPin;

    try {
        hashedPin = PinUtil.hashPin(pin);
    } catch (Exception e) {
        throw new SQLException("Could not hash PIN.", e);
    }

    String sql = "UPDATE bank_accounts " +
                 "SET account_pin = ? " +
                 "WHERE account_number = ?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement stmt = con.prepareStatement(sql)) {

        stmt.setString(1, hashedPin);
        stmt.setLong(2, accountNumber);

        int rows = stmt.executeUpdate();

        if (rows > 0) {
            System.out.println("PIN set successfully.");
        } else {
            System.out.println("Account not found.");
        }
    }
}

public boolean verifyAccountPin(long accountNumber, String pin)
        throws SQLException {

    String sql = "SELECT account_pin FROM bank_accounts " +
                 "WHERE account_number = ?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement stmt = con.prepareStatement(sql)) {

        stmt.setLong(1, accountNumber);

        try (ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                String savedPin = rs.getString("account_pin");

                try {
                    return PinUtil.verifyPin(pin, savedPin);
                } catch (Exception e) {
                    throw new SQLException(
                            "Could not verify PIN.", e);
                }
                }
            }
        }
      return false;
    }

     public void bankStatement(long accountNumber) {
        String sql = "SELECT transaction_id, transaction_type, amount, transaction_date " + "FROM bank_transactions " + "WHERE account_number = ? " + "ORDER BY transaction_date ASC";
        try (Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\n====== BANK STATEMEMT ======");
                System.out.println("Transaction ID / Type / Amount / Date");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.println(rs.getLong("transaction_id") + " / " + rs.getString("transaction_type") + " / " + rs.getBigDecimal("amount") + " / " + rs.getTimestamp("transaction_date"));
                }
                if (!found) {
                    System.out.println("No transactions found for this account.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Bank statement failed: " + e.getMessage());
        }
    }

}

