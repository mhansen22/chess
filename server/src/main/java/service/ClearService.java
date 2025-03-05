package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import dataaccess.GameDAO;

public class ClearService {
    public void clear() {
        UserDAO.clear();
        GameDAO.clear();
        AuthDAO.clear();
    }
}
