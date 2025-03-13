package dataaccess;

import java.util.Map;
import model.GameData;
import java.util.HashMap;

public class GameDAO {
    private static final Map<Integer, GameData> GAMES = new HashMap<>();
    //for use in GameService!!!
    public static Map<Integer, GameData> getGames() { return GAMES; }
    public static void addGame(GameData game) { GAMES.put(game.gameId(), game); }
    public static GameData getGame(int gameID) { return GAMES.get(gameID); }
    public static void clear() { GAMES.clear(); }
}
