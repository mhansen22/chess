package service;

import dataaccess.AuthDAO;
import model.ErrorResponse;
import model.UserData;
import model.AuthData;
import dataaccess.UserDAO;
import java.util.HashMap;
import com.google.gson.Gson;
import java.util.Map;
import java.util.UUID;

public class UserService {
//cleaned up
    public UserService() {
    }
    //Register a new user.
    public String registerUser(String username, String password, String email) {
        try {
            if (password == null || username == null || email == null || username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                return errorFormat("Error: bad request");
            }
            if (UserDAO.getUsers().containsKey(username)) {
                return errorFormat("Error: already taken");
            }
            UserData newUser = new UserData(username, password, email);
            UserDAO.getUsers().put(username, newUser);
            String authToken = UUID.randomUUID().toString();
            AuthData authData = new AuthData(authToken, username);
            AuthDAO.getAuthTokens().put(authToken, authData);
            Map<String, String> response = new HashMap<>();
            response.put("username", username);
            response.put("authToken", authToken);
            return new Gson().toJson(response);
        } catch (Exception e) {
            return errorFormat("Error: " + e.getMessage());
        }
    }

    //Logs out the user represented by the authToken.
    public String logoutUser (String authToken){
        try {
            if (!AuthDAO.getAuthTokens().containsKey(authToken) || authToken == null) {
                return errorFormat("Error: unauthorized");
            }
            AuthDAO.getAuthTokens().remove(authToken);
            return "{}";

        } catch (Exception e) {
            return errorFormat("Error: " + e.getMessage());
        }
    }
    //Logs in an existing user (returns a new authToken).
    public String loginUser(String username, String password) {
        String authToken;
        try {
            if (password == null || username == null || password.isEmpty() || username.isEmpty()) {
                return errorFormat("Error: bad request");

            }
            UserData user = UserDAO.getUsers().get(username);
            if (user == null || !user.password().equals(password)) {
                return errorFormat("Error: unauthorized");
            }
            authToken = UUID.randomUUID().toString();
            AuthData authData = new AuthData(authToken, username);
            AuthDAO.getAuthTokens().put(authToken, authData);
            Map<String, String> response = new HashMap<>();
            response.put("username", username);
            response.put("authToken", authToken);
            return new Gson().toJson(response);
        } catch (Exception e) {
            //use func to eliminate code duplication
            return errorFormat("Error: " + e.getMessage());
        }
    }
    private String errorFormat(String message) {
        return new Gson().toJson(new ErrorResponse(message));
    }
}