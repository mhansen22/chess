package dataaccess;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import java.util.HashMap;
import java.util.Map;

public class UserDAOMem implements UserDAO {
    private final Map<String, UserData> userMap = new HashMap<>();

    //same clear func
    @Override
    public void clear() { userMap.clear(); }

    @Override
    public void createUser(UserData data) throws DataAccessException {
        if (data == null || data.email() == null || data.username() == null || data.password() == null) {//check if null
            throw new DataAccessException("bad request");
        }
        //if username already in Map
        if (userMap.containsKey(data.username())) {
            throw new DataAccessException("already taken");
        }
        //then add into Map
        //hashed
        String hashedPassword = BCrypt.hashpw(data.password(), BCrypt.gensalt());
        userMap.put(data.username(), new UserData(data.username(), hashedPassword, data.email()));
    }
    //return username from userMap
    @Override
    public UserData getUser(String username) {
        return userMap.get(username);
    }
}