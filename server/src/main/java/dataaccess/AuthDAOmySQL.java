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
        try (var connection = DatabaseManager.getConnection()) {
            String statement = "DELETE FROM auth_tokens";
            try (var preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("could not clear auth", e);
        }
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        if ((auth == null) || (auth.authToken() == null) || (auth.username() == null)) {
            throw new DataAccessException("cannot be null");
        }
        String statement = "INSERT INTO auth_tokens (token, username) VALUES (?, ?)";
        try (var connection = DatabaseManager.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setString(1, auth.authToken());
                preparedStatement.setString(2, auth.username());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("could not create an auth", e);
        }
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        if (token == null) {
            return null;
        }
        String statement = "SELECT token, username FROM auth_tokens WHERE token = ?";
        try (var connection = DatabaseManager.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setString(1, token);
                try (var resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return new AuthData(resultSet.getString("token"), resultSet.getString("username"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("could not fetch auth token", e);
        }
        return null;
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        if (token ==null) {
            return;
        }
        String statement = "DELETE FROM auth_tokens WHERE token = ?";
        try (var connection = DatabaseManager.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setString(1, token);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("could not delete auth token", e);
        }
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
    //similar to petshop example too,
    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to configure database", ex);
        }
    }
}