package server;

import model.GameRequest;
import service.UserService;
import service.ClearService;
import com.google.gson.Gson;
import model.UserData;
import service.GameService;
import model.ErrorResponse;
import model.JoinGame;
import spark.Spark;

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


        Spark.post("/register", (req, res) -> {
            String body = req.body();
            UserData user = gson.fromJson(body, UserData.class);
            String response = userService.registerUser(user.username(), user.password(), user.email());
            if (errorHelperFunc(response, res)) {
                return response;
            }
            res.status(200);
            return response;
        });
        Spark.post("/login", (req, res) -> {
            String body = req.body();
            UserData user = gson.fromJson(body, UserData.class);
            String response = userService.loginUser(user.username(), user.password());
            if (errorHelperFunc(response, res)) {
                return response;
            }
            res.status(200);
            return response;
        });
        Spark.post("/clear", (req, res) -> {
            clearService.clear(); // Clears the database
            res.status(200);
            return "Database cleared successfully!";
        });
        Spark.post("/user", (req, res) -> {
            String body = req.body();
            UserData user = gson.fromJson(body, UserData.class);
            String response = userService.registerUser(user.username(), user.password(), user.email());
            if (errorHelperFunc(response, res)) {
                return response;
            }
            res.status(200);
            return response;
        });
        Spark.delete("/db", (req, res) -> {
            try {
                clearService.clear();
                res.status(200);
                return "{}";
            } catch (Exception e) {
                res.status(500);
                return new Gson().toJson(new ErrorResponse("Error: " + e.getMessage())); // Internal Server Error
            }
        });
        Spark.get("/game", (req, res) -> {
            String authToken = req.headers("Authorization");
            String response = gameService.listGames(authToken);
            if (errorHelperFunc(response, res)) {
                return response;
            }
            res.status(200);
            return response;
        });
        Spark.post("/game", (req, res) -> {
            String authToken = req.headers("Authorization");
            String body = req.body();
            GameRequest gameRequest = gson.fromJson(body, GameRequest.class);
            String response = gameService.createGame(authToken, gameRequest.gameName());
            if (errorHelperFunc(response, res)) {
                return response;
            }
            res.status(200);
            return response;
        });

        Spark.post("/session", (req, res) -> {
            String body = req.body();
            UserData user = gson.fromJson(body, UserData.class);
            String response = userService.loginUser(user.username(), user.password());
            if (errorHelperFunc(response, res)) {
                return response;
            }
            res.status(200);
            return response;
        });
        Spark.put("/game", (req, res) -> {
            String authToken = req.headers("Authorization");
            String body = req.body();
            JoinGame joinRequest = gson.fromJson(body, JoinGame.class);
            String response = gameService.joinGame(authToken, joinRequest.playerColor(), joinRequest.gameID());
            if (errorHelperFunc(response, res)) {
                return response;
            }
            res.status(200);
            return response;
        });
        Spark.delete("/session", (req, res) -> {
            String authToken = req.headers("Authorization");
            String response = userService.logoutUser(authToken);
            if (errorHelperFunc(response, res)) {
                return response;
            }
            res.status(200);
            return response;
        });



        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    //to improve code quality score, made helperfunc to reduce code duplciation!
    private boolean errorHelperFunc(String response, spark.Response res) {
        if (response == null || response.isEmpty()) {
            res.status(500);

            return true;
        }
        if (response.contains("\"message\":\"Error: unauthorized")) {
            res.status(401);
        } else if (response.contains("\"message\":\"Error: bad request")) {
            res.status(400);
        } else if (response.contains("\"message\":\"Error: already taken")) {
            res.status(403);
        } else if (response.contains("\"message\":\"Error:")) {
            res.status(500);
        } else {
            return false;
        }
        return true;
    }
}
