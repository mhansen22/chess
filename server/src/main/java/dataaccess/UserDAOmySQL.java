package dataaccess;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;

public class UserDAOmySQL implements UserDAO {

    public UserDAOmySQL() throws DataAccessException {
        configureTable();
    }

    @Override
    public void clear() throws DataAccessException {
        String statement = "DELETE FROM users";
        try (var connection = DatabaseManager.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("could not clear users", e);
        }
    }

    @Override
    public void createUser(UserData data) throws DataAccessException {
        if ((data == null) || (data.username() == null) ||(data.password() == null) || (data.email() == null)){
            throw new DataAccessException("bad request");
        }
        //hash password here before entered into the database -->
        String hashedPassword = BCrypt.hashpw(data.password(), BCrypt.gensalt());
        String statement = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)";

        try (var connection = DatabaseManager.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setString(1, data.username());
                //insert hashedPassword remember
                preparedStatement.setString(2, hashedPassword);
                preparedStatement.setString(3, data.email());
                preparedStatement.executeUpdate();
            }
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            throw new DataAccessException("already taken", e);
        } catch (SQLException e) {
            throw new DataAccessException("could not insert user", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        if (username ==null) {
            return null;
        }
        String statement = "SELECT username, password_hash, email FROM users WHERE username = ?";
        try (var connection = DatabaseManager.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setString(1, username);
                try (var resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        var userName = resultSet.getString("username");
                        var hashedPassword = resultSet.getString("password_hash");
                        var email = resultSet.getString("email");
                        return new UserData(userName, hashedPassword, email);
                    }
                }
            }
        } catch (SQLException e){
            throw new DataAccessException("could not get user", e);
        }
        return null;
    }

    private final String createStatement =
            """
            CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(50) PRIMARY KEY,
                password_hash VARCHAR(100) NOT NULL,
                email VARCHAR(255),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            ) ENGINE=InnoDB;
            """;

    private void configureTable() throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(createStatement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to configure table (user)", ex);
        }
    }
}