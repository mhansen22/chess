package model;

public class GameData {
    private int gameId;
    private String gameName;
//add heres
    public GameData(int gameId, String gameName) {
        this.gameId = gameId;
        this.gameName = gameName;
    }
    public int getGameId() { return gameId; }
    public String getGameName() { return gameName; }
}
