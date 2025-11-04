package dataaccess;
import chess.ChessGame;
import com.google.gson.Gson;
import model.Game;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
//took this from petShop example:
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class GamesDAOmySQL implements GameDAO {
    private final Gson gson = new Gson();

    public GamesDAOmySQL() throws DataAccessException {
        configureTable();
    }

    @Override
    public void clear() throws DataAccessException {
        String statement = "DELETE FROM games";
        try (var connection = DatabaseManager.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("could not clear games", e);
        }
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        if (gameName ==null){
            throw new DataAccessException("bad request");
        }
        ChessGame startGame = new ChessGame();
        String json = gson.toJson(startGame);
        String statement = "INSERT INTO games (gameName, whiteUser, blackUser, json) VALUES (?, NULL, NULL, ?)";
        try (var connection = DatabaseManager.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, gameName);
                preparedStatement.setString(2, json);
                preparedStatement.executeUpdate();

                try (var genKeys = preparedStatement.getGeneratedKeys()) {
                    if (genKeys.next()) {
                        return genKeys.getInt(1);
                    } else {
                        throw new DataAccessException("could not get the game id");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("could not create the game", e);
        }
    }

    @Override
    public Collection<Game> listGames() throws DataAccessException {
        var games = new ArrayList<Game>();
        try (var connection = DatabaseManager.getConnection()) {
            String statement = "SELECT gameId, gameName, whiteUser, blackUser, json FROM games";
            try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        int gameId = resultSet.getInt("gameId");
                        String gameName = resultSet.getString("gameName");
                        String whiteUser = resultSet.getString("whiteUser");
                        String blackUser = resultSet.getString("blackUser");
                        String json = resultSet.getString("json");

                        ChessGame chessGame = gson.fromJson(json, ChessGame.class);
                        games.add(new Game(gameId, whiteUser, blackUser, gameName, chessGame));
                    }
                }
            }
            return games;
        } catch (SQLException e) {
            throw new DataAccessException("could not list games", e);
        }
    }

    @Override
    public Game getGame(int gameID) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            String statement = "SELECT gameId, gameName, whiteUser, blackUser, json FROM games WHERE gameId = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setInt(1, gameID);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int gameId = resultSet.getInt("gameId");
                        String gameName = resultSet.getString("gameName");
                        String whiteUser = resultSet.getString("whiteUser");
                        String blackUser = resultSet.getString("blackUser");
                        String json = resultSet.getString("json");

                        ChessGame chessGame = gson.fromJson(json, ChessGame.class);
                        return new Game(gameId, whiteUser, blackUser, gameName, chessGame);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("could not get game", e);
        }
        return null;
    }

    @Override
    public void updateGame(Game game) throws DataAccessException {
        if (game== null) {
            throw new DataAccessException("game not found");
        }
        String json = gson.toJson(game.game());
        String statement = "UPDATE games SET gameName=?, whiteUser=?, blackUser=?, json=? WHERE gameId=?";
        try (var connection = DatabaseManager.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(statement)) {
                //gameName
                preparedStatement.setString(1, game.gameName());
                //whitePlayer, check if null
                if (game.whiteUser() ==null) {
                    preparedStatement.setNull(2, NULL);
                } else {
                    preparedStatement.setString(2, game.whiteUser());
                }
                //blackPlayer, check if null
                if (game.blackUser()== null) {
                    preparedStatement.setNull(3, NULL);
                } else {
                    preparedStatement.setString(3, game.blackUser());
                }
                //json for game chessBoard
                preparedStatement.setString(4, json);
                //gameId
                preparedStatement.setInt(5, game.gameId());

                int rowsChanged = preparedStatement.executeUpdate();//how many rows changed
                if (rowsChanged==0) {
                    throw new DataAccessException("game not found");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("could not update the game", e);
        }
    }

    private final String createStatement =
            """
            CREATE TABLE IF NOT EXISTS games (
                `gameId` int NOT NULL AUTO_INCREMENT,
                `gameName` varchar(256) NOT NULL,
                `whiteUser` varchar(64),
                `blackUser` varchar(64),
                `json` longtext NOT NULL,
                PRIMARY KEY (`gameId`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
            """;

    private void configureTable() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(createStatement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to configure table (games)", ex);
        }
    }
}