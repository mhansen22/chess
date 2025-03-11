package dataaccess;

import java.util.Map;
import model.UserData;
import java.util.HashMap;

public class UserDAO {
    private static final Map<String, UserData> users = new HashMap<>();
    //for use in UserService!!!
    public static Map<String, UserData> getUsers() { return users; }
}
