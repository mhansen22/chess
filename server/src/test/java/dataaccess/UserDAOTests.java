package dataaccess;
import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mindrot.jbcrypt.BCrypt;

public class UserDAOTests {

    private UserDAO dao;
    @BeforeEach
    void setup() throws DataAccessException {
        dao = new UserDAOmySQL();
        dao.clear();
        //to reset each time :0
    }

    @Test
    void createPositive() throws Exception {
        var user1 = new UserData("user1", "password1", "email1");
        dao.createUser(user1);
        //make sure its created in DB
        var pullUserfromDB = dao.getUser("user1");
        assertEquals("user1", pullUserfromDB.username());
        assertEquals("email1", pullUserfromDB.email());
        assertTrue(BCrypt.checkpw("password1", pullUserfromDB.password()));
    }
    @Test
    void createNegative() throws Exception {
        //if missing it needs to throw an error
        assertThrows(DataAccessException.class, () -> dao.createUser(new UserData(null, "password2", "email2")));
        assertThrows(DataAccessException.class, () -> dao.createUser(new UserData("user2", null, "email2")));
        assertThrows(DataAccessException.class, () -> dao.createUser(new UserData("user2", "password2", null)));
        //ALSO, can't put in the same username
        dao.createUser(new UserData("sameUsername", "passWOOOORD", "randomEmail"));
        assertThrows(DataAccessException.class, () -> dao.createUser(new UserData("sameUsername", "ANotherPASSWORd", "diffEmail")));
    }

    @Test
    void getPositive() throws Exception {
        dao.createUser(new UserData("mayme", "marmie", "maymeEmail"));
        var user = dao.getUser("mayme");
        //make sure they are the same in the DB
        assertEquals("mayme", user.username());
        assertEquals("maymeEmail", user.email());
        assertTrue(BCrypt.checkpw("marmie", user.password()));
    }
    @Test
    void getNegative() throws Exception {
        var user = dao.getUser("notaUser");
        assertNull(user);//check there is NO user created, neg
    }

    @Test
    void clearPositive() throws Exception {
        dao.createUser(new UserData("aAAAA", "secret", "aEmail"));
        dao.createUser(new UserData("bAAAA", "secret2", "bEmail"));
        dao.clear();
        //check null, thus not there :0
        assertNull(dao.getUser("aAAAA"));
        assertNull(dao.getUser("bAAAA"));
    }
}