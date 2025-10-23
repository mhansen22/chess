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

    public record RegisterRequest(String username, String password, String email) {}
    public record RegisterResult(String username, String authToken) {}

    public RegisterResult register(RegisterRequest req) throws DataAccessException {
        if (req == null || req.username() == null || req.password() == null || req.email() == null){
            throw new DataAccessException("bad request");
        }
        //make a new user
        UserData newUser = new UserData(req.username(), req.password(), req.email());
        users.createUser(newUser);
        //now mkae a new random token
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, req.username());
        auths.createAuth(auth);
        return new RegisterResult(req.username(), token);
    }

    public record LoginRequest(String username, String password) {}
    public record LoginResult(String username, String authToken) {}

    public LoginResult login(LoginRequest req) throws DataAccessException {
        if (req == null || req.username() == null || req.password() ==null) {
            throw new DataAccessException("bad request");
        }
        var user = users.getUser(req.username());
        if (user == null) {
            throw new DataAccessException("unauthorized");
        }
        if (!user.password().equals(req.password())) {
            throw new DataAccessException("unauthorized");
        }
        //make new token
        String token = UUID.randomUUID().toString();
        auths.createAuth(new AuthData(token, req.username()));

        return new LoginResult(req.username(), token);
    }

    public void logout(String authToken) throws DataAccessException {
        //i think just remove it??
        var auth = auths.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }
        auths.deleteAuth(authToken);
    }
}