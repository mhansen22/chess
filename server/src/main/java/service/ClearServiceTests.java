//package service;
//
//import dataaccess.AuthDAO;
//import static org.junit.jupiter.api.Assertions.*;
//import dataaccess.UserDAO;
//import dataaccess.GameDAO;
//import org.junit.jupiter.api.*;
//
//public class ClearServiceTests {
//    private ClearService clearService;
//    private GameService gameService;
//    private UserService userService;
//
//    @BeforeEach
//    void setUp() {
//        userService = new UserService();
//        gameService = new GameService();
//        clearService = new ClearService();
//        //i need to clear before testing:
//        UserDAO.clear();
//        AuthDAO.clear();
//        GameDAO.clear();
//        userService.registerUser("user1", "password1", "testemail@email.com");
//        gameService.createGame("authtoken", "Game1");
//    }
//    //negative test case:
//    @Test
//    void testingClearServiceWhenEmpty() {
//        clearService.clear();
//        clearService.clear();
//        assertTrue(UserDAO.getUsers().isEmpty());
//        assertTrue(GameDAO.getGames().isEmpty());
//        assertTrue(AuthDAO.getAuthTokens().isEmpty());
//    }
//    //postive test cases:
//    @Test
//    void testingClearServiceRemovingAllData() {
//        clearService.clear();
//        //check empty true!
//        assertTrue(AuthDAO.getAuthTokens().isEmpty(), "Authtokens --> cleared");
//        assertTrue(GameDAO.getGames().isEmpty(), "games --> cleared");
//        assertTrue(UserDAO.getUsers().isEmpty(), "Users --> cleared");
//    }
//    @Test
//    void testingClearServiceClearsAgain() {
//        clearService.clear();
//        clearService.clear();
//        assertTrue(AuthDAO.getAuthTokens().isEmpty(), "Authtokens --> stay empty after two clears");
//        assertTrue(UserDAO.getUsers().isEmpty(), "users --> stay empty after two clears");
//        assertTrue(GameDAO.getGames().isEmpty(), "games --> stay empty after two clears");
//    }
//
//
//}
