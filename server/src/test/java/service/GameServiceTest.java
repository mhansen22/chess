package service;

import dataaccess.*;
import org.junit.jupiter.api.*;
import model.AuthData;
import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    private GameService gameService;
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    @BeforeEach
    void setup() {
        authDAO = new AuthDAOMem();
        gameDAO = new GameDAOMem();
        gameService = new GameService(gameDAO, authDAO);
    }

    @Test
    void createGamePositive() throws Exception {
        //create random authtoken to test
        String token = java.util.UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(token, "testingUser1"));
        var result = gameService.createGame(token, new GameService.CreateGameRequest("game.io"));
        assertTrue(result.gameID() > 0);//game id must be not neg
    }
    @Test
    void createGameNegative() {
        var fail = assertThrows(DataAccessException.class,
                () -> gameService.createGame("failToken", new GameService.CreateGameRequest("failGame.io")));
        assertEquals("unauthorized", fail.getMessage());
    }

    @Test
    void listGamesPositive() throws Exception {
        String token = java.util.UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(token, "testingUser2"));
        gameService.createGame(token, new GameService.CreateGameRequest("game.io1"));
        gameService.createGame(token, new GameService.CreateGameRequest("game.io2"));
        var result = gameService.listGames(token);
        assertEquals(2, result.games().size());//should be 2 games in list
    }
    @Test
    void listGamesNegative() {
        var fail = assertThrows(DataAccessException.class, () -> gameService.listGames("badToken"));
        assertEquals("unauthorized", fail.getMessage());
    }

    @Test
    void joinGamePositive() throws Exception {
        String token = java.util.UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(token, "testingUser3"));
        int id = gameService.createGame(token, new GameService.CreateGameRequest("game.io3")).gameID();
        gameService.joinGame(token, new GameService.JoinGameRequest("WHITE", id));
        assertEquals("testingUser3", gameDAO.getGame(id).whiteUser());
    }
    @Test
    void joinGameNegative() throws Exception {
        String token1 = java.util.UUID.randomUUID().toString();
        String token2 = java.util.UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(token1, "testingUser1"));
        authDAO.createAuth(new AuthData(token2, "testingUser2"));
        int id = gameService.createGame(token1, new GameService.CreateGameRequest("game.io4")).gameID();

        gameService.joinGame(token1, new GameService.JoinGameRequest("BLACK", id));
        var fail = assertThrows(DataAccessException.class, () -> gameService.joinGame(token2, new GameService.JoinGameRequest("BLACK", id)));
        assertEquals("already taken", fail.getMessage());//cannot have two of same color players
    }
}