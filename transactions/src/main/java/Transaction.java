import java.sql.Connection;
import java.sql.SQLException;

public interface Transaction {
    public void execute(Connection connection) throws SQLException;
}
