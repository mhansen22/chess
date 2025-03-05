package dataaccess;

import model.GameData;
import java.util.HashMap;
import java.util.Map;

public class GameDAO {
    private static final Map<Integer, GameData> games = new HashMap<>();

    public static void clear() {
        games.clear();
    }
    public static Map<Integer, GameData> getGames() {
        return games;
    }
}

