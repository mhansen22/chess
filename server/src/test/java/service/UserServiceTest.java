package service;

import dataaccess.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private UserService userService;

    @BeforeEach
    void setup() {
        userDAO = new UserDAOMem();
        authDAO = new AuthDAOMem();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    void registerPositive() throws Exception {
        //make a  new user
        var req = new UserService.RegisterRequest("testingUser", "password", "email");
        var result = userService.register(req);
        //make sure its the same:
        assertEquals("testingUser", result.username());
        assertNotNull(result.authToken());
        assertNotNull(authDAO.getAuth(result.authToken()));
        assertEquals("testingUser", userDAO.getUser("testingUser").username());
    }
    @Test
    void registerNegative() throws Exception {
        //register:
        userService.register(new UserService.RegisterRequest("testingUser", "password", "email"));
        //register with same username but diff other stuff, MUST fail:
        var fail = assertThrows(DataAccessException.class, () -> userService.register(new UserService.RegisterRequest("testingUser", "diffPassword", "diffEmail")));
        assertEquals("already taken", fail.getMessage());
    }

    @Test
    void loginPositive() throws Exception {
        userService.register(new UserService.RegisterRequest("testingUser", "password", "email"));
        //try logging in:
        var result = userService.login(new UserService.LoginRequest("testingUser", "password"));
        assertEquals("testingUser", result.username());
        assertNotNull(result.authToken());
        assertNotNull(authDAO.getAuth(result.authToken()));
    }
    @Test
    void loginNegative() throws Exception {
        userService.register(new UserService.RegisterRequest("testingUser", "password", "email"));
        //wrong password:
        var fail = assertThrows(DataAccessException.class, () -> userService.login(new UserService.LoginRequest("testingUser", "wrongPass")));
        assertEquals("unauthorized", fail.getMessage());
    }

    @Test
    void logoutPositive() throws Exception {
        userService.register(new UserService.RegisterRequest("testingUser", "password", "email"));
        var login = userService.login(new UserService.LoginRequest("testingUser", "password"));
        //tokenmust exist:
        assertNotNull(authDAO.getAuth(login.authToken()));
        //logout & make sure token is null
        userService.logout(login.authToken());
        assertNull(authDAO.getAuth(login.authToken()));
    }
    @Test
    void logoutNegative() {
        //logout with wrong token
        var fail = assertThrows(DataAccessException.class, () -> userService.logout("badToken"));
        assertEquals("unauthorized", fail.getMessage());
    }
}