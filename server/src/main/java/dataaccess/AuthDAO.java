package dataaccess;

import java.util.HashMap;
import model.AuthData;
import java.util.Map;

public class AuthDAO {
    private static final Map<String, AuthData> AUTH_TOKENS = new HashMap<>();
    //for use in UserService!!!
    public static void clear() { AUTH_TOKENS.clear(); }
    public static Map<String, AuthData> getAuthTokens() {
        return AUTH_TOKENS;
    }
}
