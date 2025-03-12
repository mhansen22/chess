package service;

import chess.ChessGame;
import model.ErrorResponse;
import model.GameData;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import java.util.Map;
import java.util.*;
import passoff.model.TestListEntry;

public class GameService {
    //Creates a new game.
    public String createGame(String authToken, String gameName) {
        try {
            if (authToken == null || !AuthDAO.getAuthTokens().containsKey(authToken)) {
                return errorFormat("Error: unauthorized");
            }
            if (gameName.isEmpty() || gameName == null) {
                return errorFormat("Error: bad request");
            }
            int gameID = GameDAO.getGames().size() + 5;
            GameData newGame = new GameData(gameID, null, null, gameName, null);
            GameDAO.addGame(newGame);
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("gameID", gameID);
            return new Gson().toJson(responseMap);
        } catch (Exception e) {
            return errorFormat("Error: " + e.getMessage());
        }
    }
    //Gives a list of all games.
    //Note that whiteUsername and blackUsername may be null!
    public String listGames(String authToken) {
        try {
            if (authToken == null || !AuthDAO.getAuthTokens().containsKey(authToken)) {
                return errorFormat("Error: unauthorized");
            }
            Collection<GameData> gameCollection = GameDAO.getGames().values();
            List<TestListEntry> gameList = new ArrayList<>();
            for (GameData currentGame : gameCollection) {
                if (currentGame == null || currentGame.gameId() <= 0) {
                    continue;
                }
                String gameTitle;
                if (currentGame.gameName() != null) {
                    gameTitle = currentGame.gameName();
                }
                else {
                    gameTitle = "no-name game";
                }
                gameList.add(new TestListEntry(
                        currentGame.gameId(),
                        gameTitle,
                        currentGame.whiteUsername(),
                        currentGame.blackUsername()
                ));
            }
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("games", gameList);
            return new Gson().toJson(responseMap);
        } catch (Exception e) {
            return errorFormat("Error: " + e.getMessage());
        }
    }

    //Join Game
    public String joinGame(String authToken, String playerColor, int gameID) {
        try {
            if (!AuthDAO.getAuthTokens().containsKey(authToken) || authToken == null) {
                return errorFormat("Error: unauthorized");
            }
            GameData game = GameDAO.getGame(gameID);
            if (game == null) {

                return errorFormat("Error: bad request");
            }
            if (playerColor == null || (!playerColor.equals("BLACK") && !playerColor.equals("WHITE"))) {
                return errorFormat("Error: bad request");
            }
            if ((game.whiteUsername() != null && playerColor.equals("WHITE")) ||
                    (playerColor.equals("BLACK") && game.blackUsername() != null)) {

                return errorFormat("Error: already taken");
            }
            String username = AuthDAO.getAuthTokens().get(authToken).username();
            int gameId = game.gameId();
            String gameName = game.gameName();
            ChessGame gameState = game.game();
            String newWhitePlayer = game.whiteUsername();
            String newBlackPlayer = game.blackUsername();
            if ("WHITE".equals(playerColor)) {
                newWhitePlayer = username;
            } else if ("BLACK".equals(playerColor)) {
                newBlackPlayer = username;
            }
            GameData newGame = new GameData(gameId, newWhitePlayer, newBlackPlayer, gameName, gameState);
            GameDAO.addGame(newGame);
            return "{}";
        } catch (Exception e) {
            //change to Gson
            return errorFormat("Error: " + e.getMessage());
        }

    }
    private String errorFormat(String message) {
        return new Gson().toJson(new ErrorResponse(message));
    }
}
