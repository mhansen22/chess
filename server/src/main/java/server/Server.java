package server;

import model.GameRequest;
import service.UserService;
import service.ClearService;
import service.GameService;
import model.ErrorResponse;
import model.JoinGame;
import spark.Spark;
import com.google.gson.Gson;
import model.UserData;

public class Server {
    private UserService userService;
    private ClearService clearService;
    private GameService gameService;
    private Gson gson;

    public Server() {
        userService = new UserService();
        clearService = new ClearService();
        gameService = new GameService();
        gson = new Gson();
    }
    //call run in main with port!!
    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("/web");


        Spark.post("/register", (req, res) -> userLogic(req, res, "register"));

        Spark.post("/login", (req, res) -> userLogic(req, res, "login"));

        Spark.post("/user", (req, res) -> userLogic(req, res, "register"));

        Spark.post("/clear", (req, res) -> {
            clearService.clear(); // Clears the database
            res.status(200);
            return "Database cleared";
        });

        Spark.delete("/db", (req, res) -> {
            try {
                clearService.clear();
                res.status(200);
                return "{}";
            } catch (Exception e) {
                res.status(500);
                return new Gson().toJson(new ErrorResponse("Error: " + e.getMessage()));
            }
        });

        Spark.get("/game", (req, res) -> authentLogic(req, res, gameService::listGames));

        Spark.post("/game", (req, res) -> creatingGameLogic(req, res));

        Spark.post("/session", (req, res) -> userLogic(req, res, "login"));

        Spark.put("/game", (req, res) -> {
            String authToken = req.headers("Authorization");
            JoinGame joinRequest = gson.fromJson(req.body(), JoinGame.class);
            return joiningGameLogic(res, authToken, joinRequest);
        });

        Spark.delete("/session", (req, res) -> {
            String authToken = req.headers("Authorization");
            return errorHelperFunc(res, userService.logoutUser(authToken));
        });




        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }


    //HELPER FUNCS FOR BETTER CODE QUALITY SCORE!!!
    //logic for register + login
    private String userLogic(spark.Request req, spark.Response res, String action) {
        UserData user = gson.fromJson(req.body(), UserData.class);
        String response = action.equals("register") ?
                userService.registerUser(user.username(), user.password(), user.email()) :
                userService.loginUser(user.username(), user.password());
        return errorHelperFunc(res, response);
    }
    //logic for authenticaiton
    private String authentLogic(spark.Request req, spark.Response res, java.util.function.Function<String, String> serviceMethod) {
        String authToken = req.headers("Authorization");
        return errorHelperFunc(res, serviceMethod.apply(authToken));
    }
    //logic for creatingGame
    private String creatingGameLogic(spark.Request req, spark.Response res) {
        String authToken = req.headers("Authorization");
        GameRequest gameRequest = gson.fromJson(req.body(), GameRequest.class);
        return errorHelperFunc(res, gameService.createGame(authToken, gameRequest.gameName()));
    }
    //logic for joiningGame
    private String joiningGameLogic(spark.Response res, String authToken, JoinGame joinRequest) {
        return errorHelperFunc(res, gameService.joinGame(authToken, joinRequest.playerColor(), joinRequest.gameID()));
    }
    //to improve code quality score, made helperfunc to reduce code duplciation!
    private String errorHelperFunc(spark.Response res, String response) {
        if (response == null || response.isEmpty()) {
            res.status(500);
            return "{}";
        }
        //the if loops check the specific errors...
        if (response.contains("\"message\":\"Error: unauthorized")) {
            res.status(401);
        } else if (response.contains("\"message\":\"Error: bad request")) {
            res.status(400);
        } else if (response.contains("\"message\":\"Error: already taken")) {
            res.status(403);
        } else if (response.contains("\"message\":\"Error:")) {
            res.status(500);
        } else { res.status(200); }
        return response;
    }
}