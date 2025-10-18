package dataaccess;
import model.Game;
import chess.ChessGame;
import java.util.*;
import java.util.HashMap;

public class GameDAOMem implements GameDAO {
    private final Map<Integer, Game> games = new HashMap<>();
    private int nextId = 1;

    @Override
    public void clear() {
        games.clear();
        nextId = 1;
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        if (gameName == null || gameName.isBlank()) {
            throw new DataAccessException("bad request");
        }
        int id = nextId++;
        games.put(id, new Game(id, null, null, gameName, new ChessGame()));
        return id;
    }

    @Override
    public Collection<Game> listGames() {
        return games.values();
    }

    @Override
    public Game getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public void updateGame(Game game) throws DataAccessException {
        if (game == null || !games.containsKey(game.gameId())) {
            throw new DataAccessException("game not found");
        }
        games.put(game.gameId(), game);
    }
}