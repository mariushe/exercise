import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.*;

import static org.testng.Assert.assertEquals;

public class TransactionHandlerTest {

    @BeforeMethod
    public void setUp() throws Exception {

        createTenTicketsAsTestData();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        cleanUp();

    }

    @Test
    public void testSuccessfulTransaction() throws Exception {

        Transaction transaction = new Transaction() {

            @Override
            public void execute(Connection connection) {
                System.out.println("Doing the logic to old way");
            }
        };

        TransactionHandler.runInTransaction(transaction);

        assertEquals(getNrOfTicketsIn(TicketState.AVAILABLE), 10);
        assertEquals(getNrOfTicketsIn(TicketState.RESERVED), 0);
        assertEquals(getNrOfTicketsIn(TicketState.BOUGHT), 0);
    }

    @Test
    public void testSuccessfulTransactionWithLambda() throws Exception {

        TransactionHandler.runInTransaction(connection -> System.out.println("Doing the logic with lambda"));

        assertEquals(getNrOfTicketsIn(TicketState.AVAILABLE), 10);
        assertEquals(getNrOfTicketsIn(TicketState.RESERVED), 0);
        assertEquals(getNrOfTicketsIn(TicketState.BOUGHT), 0);
    }

    @Test
    public void testSuccessfulPurchase() throws Exception {

        TransactionHandler.runInTransaction(connection -> {

            int ticketId = findAvailableTicket(connection);

            reserveTicket(ticketId, connection);
            markAsBought(ticketId, connection);
        });

        assertEquals(getNrOfTicketsIn(TicketState.AVAILABLE), 9);
        assertEquals(getNrOfTicketsIn(TicketState.RESERVED), 0);
        assertEquals(getNrOfTicketsIn(TicketState.BOUGHT), 1);
    }

    @Test
    public void testFailedPurchase() throws Exception {

        TransactionHandler.runInTransaction(connection -> {

            int ticketId = findAvailableTicket(connection);

            reserveTicket(ticketId, connection);
            throw new IllegalStateException("Not approved credit card");
        });

        assertEquals(getNrOfTicketsIn(TicketState.AVAILABLE), 10);
        assertEquals(getNrOfTicketsIn(TicketState.RESERVED), 0);
        assertEquals(getNrOfTicketsIn(TicketState.BOUGHT), 0);
    }

    private void reserveTicket(int ticketId, Connection connection) throws SQLException {
        System.out.println(String.format("Reserving ticket with id %d", ticketId));
        changeState(ticketId, TicketState.RESERVED, connection);
    }

    private void markAsBought(int ticketId, Connection connection) throws SQLException {
        System.out.println(String.format("Marking ticket with id %d as bought", ticketId));
        changeState(ticketId, TicketState.BOUGHT, connection);
    }

    private void changeState(int ticketId, TicketState toState, Connection connection) throws SQLException {

        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format("UPDATE ticket SET state = '%s' where id = %d", toState.name(), ticketId));
        statement.close();
    }


    private int findAvailableTicket(Connection connection) throws SQLException {

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT MIN(t.id) as id FROM ticket t WHERE state = 'AVAILABLE'");

        int ticketId = 0;

        if (resultSet.next()) {
            ticketId = resultSet.getInt("id");
        }

        statement.close();
        resultSet.close();

        return ticketId;
    }

    private Connection createDatabaseConnection() throws Exception {

        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/ticket_system", "user", "password");
    }

    private void cleanUp() throws Exception {

        Connection connection = createDatabaseConnection();

        Statement statement = connection.createStatement();
        statement.executeUpdate("DELETE FROM ticket;");
        statement.close();
    }

    private void createTenTicketsAsTestData() throws Exception {

        Connection connection = createDatabaseConnection();

        for (int i = 1; i <= 10; i++) {

            Statement statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO ticket VALUES (" + i + ", 'AVAILABLE');");
            statement.close();
        }

        connection.close();
    }

    private int getNrOfTicketsIn(TicketState state) throws Exception {

        Connection connection = createDatabaseConnection();

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(String.format("SELECT COUNT(*) as result FROM ticket WHERE state = '%s'", state.name()));

        int count = 0;

        if (resultSet.next()) {
            count = resultSet.getInt("result");
        }

        statement.close();
        resultSet.close();
        connection.close();

        return count;
    }

    private enum TicketState {
        AVAILABLE, RESERVED, BOUGHT;
    }
}
