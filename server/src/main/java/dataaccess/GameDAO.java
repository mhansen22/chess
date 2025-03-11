package dataaccess;

import java.util.Map;
import model.GameData;
import java.util.HashMap;

public class GameDAO {
    private static final Map<Integer, GameData> games = new HashMap<>();
    //for use in GameService!!!
    public static Map<Integer, GameData> getGames() { return games; }
    public static void addGame(GameData game) { games.put(game.gameId(), game); }
    public static GameData getGame(int gameID) { return games.get(gameID); }
}
