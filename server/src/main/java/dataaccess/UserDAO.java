package dataaccess;

import model.UserData;
import java.util.HashMap;
import java.util.Map;

public class UserDAO {
    private static final Map<String, UserData> users = new HashMap<>();

    public static void clear() {
        users.clear();
    }
    public static Map<String, UserData> getUsers() {
        return users;
    }
}
