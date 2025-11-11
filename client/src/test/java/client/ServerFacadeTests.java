package client;
import org.junit.jupiter.api.*;
import server.Server;
import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    public void registerPositive() throws Exception {
        String username = "mayme";
        String password = "maymePassword";
        String email = "maymehan@byu.edu";
        var auth = facade.register(username, password, email);
        //assert statements here::
        assertNotNull(auth);
        assertEquals(username, auth.username());
        assertNotNull(auth.authToken());
    }

}