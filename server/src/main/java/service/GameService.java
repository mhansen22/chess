package service;
import dataaccess.*;
import model.*;
import java.util.List;
import java.util.ArrayList;

public class GameService {
    private final AuthDAO auths;
    private final GameDAO games;
    public GameService(GameDAO games, AuthDAO auths) {
        this.games = games;
        this.auths = auths;
    }

    public record CreateGameRequest(String gameName) {}
    public record CreateGameResult(int gameID) {}

    public CreateGameResult createGame(String token, CreateGameRequest req) throws DataAccessException {
        if ((token == null) || (auths.getAuth(token) == null)){
            throw new DataAccessException("unauthorized");
        }
        if ((req == null) ||(req.gameName() == null)) {
            throw new DataAccessException("bad request");
        }
        int gameid = games.createGame(req.gameName());
        return new CreateGameResult(gameid);
    }
    //list the games record::
    public record ListedGame(int gameID, String whiteUsername, String blackUsername, String gameName) {}
    public record ListGamesResult(List<ListedGame> games) {}

    public ListGamesResult listGames(String token) throws DataAccessException {
        if ((token == null) || (auths.getAuth(token) == null)){
            throw new DataAccessException("unauthorized");
        }
        //get every game here
        ArrayList<Game> everyGame = new ArrayList<>(games.listGames());
        List<ListedGame> listGames = new ArrayList<>();
        for (Game game : everyGame) {
            listGames.add(new ListedGame(game.gameId(), game.whiteUser(), game.blackUser(), game.gameName()));
        }
        return new ListGamesResult(listGames);
    }
    //joing a game
    public record JoinGameRequest(String playerColor, int gameID) {}

    public void joinGame(String token, JoinGameRequest req) throws DataAccessException {
        if ((token == null) || (auths.getAuth(token) == null)){
            throw new DataAccessException("unauthorized");
        }
        if ((req == null) || (req.playerColor() ==null) || (req.gameID() <= 0)) {
            throw new DataAccessException("bad request");//bc its missing info it needs to join a game
        }
        Game game = games.getGame(req.gameID());
        if (game == null) {
            throw new DataAccessException("bad request");//no game
        }
        if (req.playerColor().equals("WHITE")) {
            if (game.whiteUser() != null) {
                throw new DataAccessException("already taken");//already has a white user, so error:
            }
            game = new Game(game.gameId(), auths.getAuth(token).username(), game.blackUser(), game.gameName(), game.game());
        }
        else if (req.playerColor().equals("BLACK")) {
            if (game.blackUser() != null) {
                throw new DataAccessException("already taken");
            }
            game = new Game(game.gameId(), game.whiteUser(), auths.getAuth(token).username(), game.gameName(), game.game());
        }
        else {
            throw new DataAccessException("bad request");
        }
        games.updateGame(game);
    }
}
