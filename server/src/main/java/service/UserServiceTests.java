//package service;
//
//import dataaccess.UserDAO;
//import com.google.gson.Gson;
//import com.google.gson.JsonObject;
//import org.junit.jupiter.api.*;
//import dataaccess.AuthDAO;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class UserServiceTests {
//    private Gson gson = new Gson();
//    private UserService userService;
//
//    @BeforeEach
//    void setUp() {
//        userService = new UserService();
//        AuthDAO.clear();
//        UserDAO.clear();
//    }
//
//
//
//    //test cases for RegisterUser
//    @Test
//    void testingRegisterUserPositive() {
//        String response = userService.registerUser("user1", "password1", "testemail1@email.com");
//        JsonObject json = gson.fromJson(response, JsonObject.class);
//        assertNotNull(json);
//        assertTrue(json.has("authToken"), "should return authToken!");
//    }
//    @Test
//    void testingRegisterUserNegative() {
//        userService.registerUser("user1", "password1", "testemail1@email.com");
//        String response = userService.registerUser("user1", "password2", "testemail2@email.com");
//        assertNotNull(response);
//        assertTrue(response.contains("Error: already taken"), "should return --> Error: already taken");
//    }
//    //test cases for LogoutUser (pos and negative)
//    @Test
//    void testingLogoutUserSuccess() {
//        String registerResponse = userService.registerUser("user1", "password1", "testemail1@email.com");
//        JsonObject json = gson.fromJson(registerResponse, JsonObject.class);
//        String authToken = json.get("authToken").getAsString();
//        assertNotNull(authToken);
//        String response = userService.logoutUser(authToken);
//        assertEquals("{}", response, "should return --> empty JSON");
//    }
//    @Test
//    void testingLogoutUserFailure() {
//        String response = userService.logoutUser("invalidToken");
//        assertNotNull(response);
//
//        assertTrue(response.contains("Error: unauthorized"), "should return --> Error: unauthorized");
//    }
//    //test cases for LoginUser (pos and negative)
//    @Test
//    void testingLoginUserPositive() {
//        userService.registerUser("user1", "password1", "testemail1@email.com");
//        String response = userService.loginUser("user1", "password1");
//        JsonObject json = gson.fromJson(response, JsonObject.class);
//        assertNotNull(json);
//        assertTrue(json.has("authToken"), "should return authToken!");
//    }
//    @Test
//    void testingLoginUserNegative() {
//        userService.registerUser("user1", "password1", "testemail1@email.com");
//        String response = userService.loginUser("user1", "badPassword");
//        assertNotNull(response);
//        assertTrue(response.contains("Error: unauthorized"), "should return --> Error: unauthorized");
//    }
//
//
//
//}