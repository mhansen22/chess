package service;
import dataaccess.*;
import model.*;
import java.util.UUID;

public class UserService {
    private AuthDAO auths;
    private UserDAO users;
    public UserService(UserDAO users, AuthDAO auths) {
        this.auths = auths;
        this.users = users;
    }

    public AuthData register(String username, String password, String email) throws DataAccessException {
        if (username == null || password == null || email == null){
            throw new DataAccessException("bad requst");
        }
        //make new user
        UserData newUser = new UserData(username, password, email);
        users.createUser(newUser);
        //this makes new rand token
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, username);
        auths.createAuth(auth);
        //now return the auth inf
        return auth;
    }

    public AuthData login(String username, String password) throws DataAccessException {
        if (username == null || password ==null) {
            throw new DataAccessException("bad request");
        }
        var user = users.getUser(username);
        if (user == null || (!user.password().equals(password))) {
            throw new DataAccessException("unauthorized");
        }
        //this makes new token
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, username);
        auths.createAuth(auth);
        return auth;
    }

    public void logout(String authToken) throws DataAccessException {
        var auth = auths.getAuth(authToken);
        if (auth== null) {
            throw new DataAccessException("unauthorizd");
        }
        auths.deleteAuth(authToken);
    }
}