package client;
import chess.ChessGame;
import org.junit.jupiter.api.*;
import server.Server;
import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }
    @BeforeEach
    public void clearDatabase() throws Exception {
        facade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    public void registerPositive() throws Exception {
        var authData = facade.register("mayme", "maymePassword", "maymehan@byu.edu");
        assertNotNull(authData);
        assertEquals("mayme", authData.username());
    }
    @Test
    public void registerNegative() throws Exception {
        var authData = facade.register("marmieSame", "password1", "marmie1@byu.edu");
        assertNotNull(authData);
        //trying again w/ same username
        var exception = assertThrows(Exception.class, () -> facade.register("marmieSame", "password2", "marmie2@byu.edu"));
        assertNotNull(exception.getMessage());
    }

    @Test
    public void loginPositive() throws Exception {
        facade.register("player1", "player1password", "player1@byu.edu");
        var authData = facade.login("player1", "player1password");
        assertNotNull(authData);
        assertEquals("player1", authData.username());
    }
    @Test
    public void loginNegative() throws Exception {
        var exception = assertThrows(Exception.class, () -> facade.login("noUser", "password"));//user does NOT exist!!!
        assertNotNull(exception.getMessage());
    }

    @Test
    public void logoutPositive() throws Exception {
        facade.register("user1", "password", "user1@byu.edu");
        facade.login("user1", "password");
        assertDoesNotThrow(() -> facade.logout());
    }
    @Test
    public void logoutNegative() throws Exception {
        var exception = assertThrows(Exception.class, () -> facade.logout());//logout with no user
        assertNotNull(exception.getMessage());
    }

    @Test
    public void createGamePositive() throws Exception {
        facade.register("joey", "joey", "joey");
        facade.login("joey", "joey");
        int gameId = facade.createGame("joey_game");
        assertTrue(gameId > 0);//make sure it creates a game!!
    }
    @Test
    public void createGameNegative() throws Exception {
        //not logged in and try to create game
        Exception exception = assertThrows(Exception.class, () -> facade.createGame("loggedOUT"));
        assertNotNull(exception.getMessage());
    }

    @Test
    public void listGamesPositive() throws Exception {
        facade.register("test", "test", "test");
        facade.login("test", "test");
        int gameId = facade.createGame("testGame");
        var gameList = facade.listGames();
        assertNotNull(gameList);
        var g = gameList.iterator().next();
        assertEquals(gameId, g.gameId());
        assertEquals("testGame", g.gameName());
    }
    @Test
    public void listGamesNegative() throws Exception {
        var exception = assertThrows(Exception.class, () -> facade.listGames());//bc not signed in
        assertNotNull(exception.getMessage());
    }

    @Test
    public void joinGamePositive() throws Exception {
        facade.register("daisy", "daisy", "daisy");
        facade.login("daisy", "daisy");
        int gameId = facade.createGame("daisyGame");
        assertDoesNotThrow(() -> facade.joinGame(gameId, ChessGame.TeamColor.WHITE));
        var gamesList = facade.listGames();
        var g = gamesList.iterator().next();
        assertEquals(gameId, g.gameId());
        assertEquals("daisy", g.whiteUser());
    }
    @Test
    public void joinGameNegative() throws Exception {
        var exception = assertThrows(Exception.class, () -> facade.joinGame(40, ChessGame.TeamColor.BLACK));//not logged in
        assertNotNull(exception.getMessage());
    }
}