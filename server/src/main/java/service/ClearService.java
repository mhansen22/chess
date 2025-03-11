package service;

import dataaccess.UserDAO;
import dataaccess.GameDAO;
import dataaccess.AuthDAO;

public class ClearService {
    public void clear() {
        //clear everything: tokens, users, games
        UserDAO.getUsers().clear();
        GameDAO.getGames().clear();
        AuthDAO.getAuthTokens().clear();
    }
}
