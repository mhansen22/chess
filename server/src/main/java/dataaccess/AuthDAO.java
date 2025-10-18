package dataaccess;

import model.AuthData;

public interface AuthDAO {
    //clear: A method for clearing all data from the database. This is used during testing.
    void clear() throws DataAccessException;

    //createAuth: Create a new authorization.
    void createAuth(AuthData auth) throws DataAccessException;

    //getAuth: Retrieve an authorization given an authToken.
    AuthData getAuth(String token) throws DataAccessException;

    //deleteAuth: Delete an authorization so that it is no longer valid.
    void deleteAuth(String token) throws DataAccessException;
}
