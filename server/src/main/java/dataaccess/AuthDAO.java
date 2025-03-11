package dataaccess;

import java.util.HashMap;
import model.AuthData;
import java.util.Map;

public class AuthDAO {
    private static final Map<String, AuthData> authTokens = new HashMap<>();
    //for use in UserService!!!
    public static Map<String, AuthData> getAuthTokens() {
        return authTokens;
    }
}
