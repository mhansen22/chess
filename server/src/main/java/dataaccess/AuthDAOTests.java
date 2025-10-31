package dataaccess;
import model.AuthData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthDAOTests {

    private AuthDAO dao;
    @BeforeEach
    void setUp() throws Exception {
        dao = new AuthDAOmySQL();
        dao.clear();
        //to reset each time :0
    }

    @Test
    void createAuthPositive() throws Exception {
        var authToken = new AuthData("fakeauthtoken", "mayme");
        dao.createAuth(authToken);
        var res = dao.getAuth("fakeauthtoken");
        //to make sure its not null AND the username and authToken are the same!!
        assertNotNull(res);
        assertEquals("mayme", res.username());
        assertEquals("fakeauthtoken", res.authToken());
    }
    @Test
    void createAuthNegative() throws Exception {
        var user1 = new AuthData("sameToken", "username1");
        dao.createAuth(user1);
        var user2 = new AuthData("sameToken", "username2");
        //if you try to use same authToken should throw an error
        assertThrows(DataAccessException.class, () -> dao.createAuth(user2));
    }

    @Test
    void getAuthPositive() throws Exception {
        dao.createAuth(new AuthData("correctToken", "mary"));
        var correct = dao.getAuth("correctToken");
        assertNotNull(correct);
        assertEquals("mary", correct.username());
    }
    @Test
    void getAuthNegative() throws Exception {
        var missingAuth = dao.getAuth("thisAuthDoesNotExist");
        assertNull(missingAuth);
    }

    @Test
    void deleteAuthPositive() throws Exception {
        dao.createAuth(new AuthData("fakeAuth123", "hansen"));
        dao.deleteAuth("fakeAuth123");
        //should return null because deleted
        var deleted = dao.getAuth("fakeAuth123");
        assertNull(deleted);
    }
    @Test
    void deleteAuthNegative() throws Exception {
        //deleting something not there is OK
        dao.deleteAuth("nonExistent");
        //should not throw anything but still neg, but makes sure still returns null
        assertNull(dao.getAuth("nonExistent"));
    }

    //only need Positive (1) test for clear
    @Test
    void clearPositive() throws Exception {
        dao.createAuth(new AuthData("tiktok", "katherine"));
        dao.createAuth(new AuthData("toktik", "elena"));
        dao.clear();
        //make sure it cleared BOTH
        assertNull(dao.getAuth("tiktok"));
        assertNull(dao.getAuth("toktik"));
    }
}