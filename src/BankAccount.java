import java.math.BigDecimal;

public class BankAccount {
    private long accountNumber;
    private String accountHolder;
    private BigDecimal balance;

    public BankAccount(long accountNumber, String accountHolder, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.balance = balance;
    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
