package dataaccess;
import model.AuthData;
import java.util.Map;
import java.util.HashMap;

public class AuthDAOMem implements AuthDAO {
    private final Map<String, AuthData> authMap = new HashMap<>();
    @Override
    public void clear() { authMap.clear(); }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        if (auth == null || auth.username() == null || auth.authToken() == null) {//check to make sure not null
            throw new DataAccessException("bad request");
        }
        if (authMap.containsKey(auth.authToken())) {
            throw new DataAccessException("auth token already exists");
        }
        authMap.put(auth.authToken(),auth);
    }

    //delete remove from Map
    @Override
    public void deleteAuth(String token) { authMap.remove(token); }
    //get just return the token from Map
    @Override
    public AuthData getAuth(String token) { return authMap.get(token);}
}
