package service;
import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ResetServiceTest {
    @Test
    void testClearPositive() throws Exception {
        GameDAOMem games = new GameDAOMem();
        UserDAOMem users = new UserDAOMem();
        AuthDAOMem auths = new AuthDAOMem();
        //for testing
        users.createUser(new UserData("testingUser", "password", "email"));
        auths.createAuth(new model.AuthData("12345", "testingUser"));
        games.createGame("game.io");

        ResetService reset = new ResetService(users, auths, games);
        reset.clear();
        //make sure empty
        assertEquals(null, users.getUser("testingUser"));
        assertTrue(games.listGames().size() ==0);
        assertEquals(null, auths.getAuth("12345"));
    }

    @Test
    void testClearNeg() {
        UserDAO neg = new UserDAO() {
            public void clear() throws DataAccessException {
                throw new DataAccessException("error");
            }
            public void createUser(UserData user) {}
            public UserData getUser(String username) { return null; }
        };

        ResetService reset = new ResetService(neg, new AuthDAOMem(), new GameDAOMem());
        try {
            reset.clear();
            fail("should have thrown error");
        } catch (Exception er) {
            assertTrue(er.getMessage().contains("error"));
        }
    }
}