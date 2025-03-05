package dataaccess;

import model.AuthData;
import java.util.HashMap;
import java.util.Map;

public class AuthDAO {

    private static final Map<String, AuthData> authTokens = new HashMap<>();

    public static void clear() {
        authTokens.clear();
    }
    public static Map<String, AuthData> getAuthTokens() {
        return authTokens;
    }
}
