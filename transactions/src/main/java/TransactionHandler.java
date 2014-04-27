import java.sql.Connection;
import java.sql.DriverManager;

public class TransactionHandler {

    public static void runInTransaction(Transaction transaction) throws Exception {

        Connection dbConnection = createDatabaseConnection();
        dbConnection.setAutoCommit(false);

        try {

            System.out.println("Starting transaction");
            transaction.execute(dbConnection);


            System.out.println("Committing transaction");
            dbConnection.commit();

        } catch (Exception e) {

            System.out.println(e.getMessage());
            System.out.println("Rolling back...");
            dbConnection.rollback();
        } finally {
            dbConnection.close();
        }
    }

    private static Connection createDatabaseConnection() throws Exception {

        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/ticket_system", "user", "password");
    }
}
