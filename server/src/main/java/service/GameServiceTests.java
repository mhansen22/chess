package service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTests {
    private Gson gson = new Gson();
    private GameService gameService;
    private String validAuthToken;

    @BeforeEach
    void setUp() {
        gameService = new GameService();
        AuthDAO.clear();
        UserDAO.clear();
        GameDAO.clear();

        UserService userService = new UserService();
        String registerResponse = userService.registerUser("user1", "password1", "testemail1@email.com");
        JsonObject json = gson.fromJson(registerResponse, JsonObject.class);
        validAuthToken = json.get("authToken").getAsString();
        assertNotNull(validAuthToken, "the authtoken should NOT be null after the user registers!");
    }




    //test cases for Join Game
    @Test
    void testingJoiningGameSuccess() {
        String createResponse = gameService.createGame(validAuthToken, "game1");
        JsonObject json = gson.fromJson(createResponse, JsonObject.class);
        int gameID = json.get("gameID").getAsInt();
        String response = gameService.joinGame(validAuthToken, "WHITE", gameID);
        assertEquals("{}", response, "when you join it should return an empty JSON");
    }
    @Test
    void testingJoiningGameFailure() {
        String createResponse = gameService.createGame(validAuthToken, "game1");
        JsonObject json = gson.fromJson(createResponse, JsonObject.class);
        int gameID = json.get("gameID").getAsInt();
        gameService.joinGame(validAuthToken, "WHITE", gameID);
        String secondJoinResponse = gameService.joinGame(validAuthToken, "WHITE", gameID);
        assertTrue(secondJoinResponse.contains("Error: already taken"), "if the spot is takn it should return error");
    }
    //test cases for List Game
    @Test
    void testingListGamesSuccess() {
        gameService.createGame(validAuthToken, "chess1");
        gameService.createGame(validAuthToken, "chess2");
        String response = gameService.listGames(validAuthToken);
        assertTrue(response.contains("chess1"));
        assertTrue(response.contains("chess2"));
    }
    @Test
    void testingListGamesFailure() {
        String response = gameService.listGames("badToken");
        assertTrue(response.contains("Error: unauthorized"));
    }
    //test cases for Create Game
    @Test
    void testingCreateGameSuccess() {
        String response = gameService.createGame(validAuthToken, "game1");
        JsonObject json = gson.fromJson(response, JsonObject.class);
        assertTrue(response.contains("gameID"), "when creating a game, it should return a gameID");
    }
    @Test
    void testingCreateGameFail() {
        String response = gameService.createGame("badToken", "game1");
        assertTrue(response.contains("Error: unauthorized"), "shoudl error with a bad authtoken");
    }


}
