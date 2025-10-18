package dataaccess;

import model.UserData;

public interface UserDAO {
    //clear: A method for clearing all data from the database. This is used during testing.
    void clear() throws DataAccessException;

    //createUser: Create a new user.
    void createUser(UserData u) throws DataAccessException;

    //getUser: Retrieve a user with the given username.
    UserData getUser(String username) throws DataAccessException;
}