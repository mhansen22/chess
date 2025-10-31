package dataaccess;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;

public class UserDAOmySQL implements UserDAO {

    public UserDAOmySQL() throws DataAccessException {
        configureTable();
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
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(createStatement)) {
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to configure table (user)", ex);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String clearingUsers = "DELETE FROM users";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(clearingUsers)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("could not clear users", e);
        }
    }

    @Override
    public void createUser(UserData data) throws DataAccessException {
        if ((data.username() == null) ||(data.password() == null) || (data.email() == null)){
            throw new DataAccessException("bad request");
        }
        //hash password here before entered into the database -->
        String hashedPassword = BCrypt.hashpw(data.password(), BCrypt.gensalt());
        String insertingUser = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(insertingUser)) {
            ps.setString(1, data.username());
            //insert hashedPassword remember
            ps.setString(2, hashedPassword);
            ps.setString(3, data.email());
            ps.executeUpdate();
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            throw new DataAccessException("already  taken", e);
        } catch (SQLException e) {
            throw new DataAccessException("could not insert user", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        if (username ==null) {
            return null;
        }
        String selectingUser = "SELECT username, password_hash, email FROM users WHERE username = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(selectingUser)) {
            ps.setString(1, username);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    var userName = rs.getString("username");
                    var hashedPassword = rs.getString("password_hash");
                    var email = rs.getString("email");
                    return new UserData(userName, hashedPassword, email);
                }
            }
        } catch (SQLException e){
            throw new DataAccessException("could not get user", e);
        }
        return null;
    }
}