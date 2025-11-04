package dataaccess;
import model.Game;
import chess.ChessGame;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.stream.Collectors;

public class GameDAOTests {

    private GamesDAOmySQL dao;
    @BeforeEach
    void setup() throws DataAccessException {
        dao = new GamesDAOmySQL();
        dao.clear();
    }

    @Test
    void createPositive() throws Exception {
        int gameId = dao.createGame("gameUno");
        assertTrue(gameId > 0, "gameId must be over 0");
        //creates new game with that name and empty fields, null
        var gameUno = dao.getGame(gameId);
        assertEquals("gameUno", gameUno.gameName());
        assertNull(gameUno.whiteUser());
        assertNull(gameUno.blackUser());
    }
    @Test
    void createNegative() {
        assertThrows(DataAccessException.class, () -> dao.createGame(null));
    }

    @Test
    void getPositive() throws Exception {
        int gameId = dao.createGame("chessLevelImpossible");
        var chessLevelImpossible = dao.getGame(gameId);
        assertEquals(gameId, chessLevelImpossible.gameId());
        assertEquals("chessLevelImpossible", chessLevelImpossible.gameName());
    }
    @Test
    void getNegative() throws Exception {
        var game = dao.getGame(6767);
        assertNull(game);
        //there is not game with ID 192, not created yet
    }

    @Test
    void updatePositive() throws Exception {
        int gameId = dao.createGame("ogGame");
        var ogGame = dao.getGame(gameId);
        //add a couple things and change name
        var changes = new Game(gameId, "whiteUser", "blackUser", "newName", ogGame.game());
        dao.updateGame(changes);
        var newGame = dao.getGame(gameId);
        //make sure has NEW name but same users
        assertEquals("newName", newGame.gameName());
        assertEquals("whiteUser", newGame.whiteUser());
        assertEquals("blackUser", newGame.blackUser());
    }
    @Test
    void updateNegative() {
        //with gameID that does not exist
        var notReal = new Game(329235, null, null, "noooo", new ChessGame());
        var e = assertThrows(DataAccessException.class, () -> dao.updateGame(notReal));
        assertEquals("game not found", e.getMessage());
    }

    @Test
    void listPositive() throws Exception {
        int id11 = dao.createGame("elevensGame");
        int id8 = dao.createGame("eightsGame");
        var listOfGames = dao.listGames();
        //make sure it lists the two games i added:
        assertEquals(2, listOfGames.size());
        var gameNames = listOfGames.stream().map(Game::gameName).collect(Collectors.toSet());
        assertTrue(gameNames.contains("elevensGame"));
        assertTrue(gameNames.contains("eightsGame"));
    }
    @Test
    void listNegative() throws Exception {
        var listOfGames = dao.listGames();
        //the list should be empty
        assertTrue(listOfGames.isEmpty());
    }

    @Test
    void clearPositive() throws Exception {
        dao.createGame("stevesGame");
        dao.createGame("dustinsGame");
        dao.clear();
        //make sure they are empty
        assertTrue(dao.listGames().isEmpty());
    }
}