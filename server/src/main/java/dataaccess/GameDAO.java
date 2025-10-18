package dataaccess;

import model.Game;

import java.util.Collection;

public interface GameDAO {
    //clear: A method for clearing all data from the database. This is used during testing.
    void clear() throws DataAccessException;

    //createGame: Create a new game.
    int createGame(String gameName) throws DataAccessException;

    //getGame: Retrieve a specified game with the given game ID.
    Game getGame(int gameID) throws DataAccessException;

    //listGames: Retrieve all games.
    Collection<Game> listGames() throws DataAccessException;

    //updateGame: Updates a chess game. It should replace the chess game string corresponding to a given gameID.
    // This is used when players join a game or when a move is made.
    void updateGame(Game game) throws DataAccessException;
}
