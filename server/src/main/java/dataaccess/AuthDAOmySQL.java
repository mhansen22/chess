package dataaccess;
import model.AuthData;
import java.sql.*;

public class AuthDAOmySQL implements AuthDAO {

    public AuthDAOmySQL() throws DataAccessException {
        configureDatabase();
        //this is from PetShop example!!!!!
    }

    @Override
    public void clear() throws DataAccessException {
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
    }

    private final String[] createStatements = {
        """
        CREATE TABLE IF NOT EXISTS users (
        username VARCHAR(50) PRIMARY KEY,
        password_hash VARCHAR(100) NOT NULL,
        email VARCHAR(255),
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ) ENGINE=InnoDB
        """,
        """
        CREATE TABLE IF NOT EXISTS auth_tokens (
        token CHAR(36) PRIMARY KEY,
        username VARCHAR(50) NOT NULL,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        INDEX (username)
        ) ENGINE=InnoDB
        """
    };
    //similar to petshop example too,,
    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var ps = conn.prepareStatement(statement)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to configure database", ex);
        }
    }
}