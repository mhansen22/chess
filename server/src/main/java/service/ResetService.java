package service;
import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.DataAccessException;


public class ResetService {
    private final UserDAO users;
    private final GameDAO games;
    private final AuthDAO auths;

    public ResetService(UserDAO users, AuthDAO auths, GameDAO games) {
        this.users = users;
        this.auths = auths;
        this.games = games;
    }
    public void clear() throws DataAccessException {
        users.clear();
        auths.clear();
        games.clear();
    }
}
