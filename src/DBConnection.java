import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:oracle:thin:@localhost:1521/XE";

    private static final String USERNAME =
            System.getenv("DB_USERNAME");

    private static final String PASSWORD =
            System.getenv("DB_PASSWORD");

    public static Connection getConnection() throws SQLException {

        if (USERNAME == null || PASSWORD == null) {
            throw new SQLException(
                "Database credentials are not set. Set DB_USERNAME and DB_PASSWORD."
            );
        }

        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Oracle JDBC Driver not found.", e);
        }

        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}