package server;
import io.javalin.*;
import com.google.gson.Gson;
import dataaccess.*;
import service.*;

public class Server {
    private final Javalin javalin;
    private final Gson serializer = new Gson();

    private final UserDAO users = new UserDAOMem();//DAOs:
    private final GameDAO games = new GameDAOMem();
    private final AuthDAO auths = new AuthDAOMem();
    private final ResetService resetService = new ResetService(users, auths, games);//services:
    private final GameService gameService = new GameService(games, auths);
    private final UserService userService = new UserService(users, auths);

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.delete("/db", ctx -> {//first, to clear everything:
            try {
                resetService.clear();
                ctx.status(200).contentType("application/json").result("{}");
            } catch (DataAccessException e) {
                String message = e.getMessage();
                ctx.status(500).contentType("application/json").result("{\"message\":\"Error: " + message + "\"}");
            }//server failure!!!! important
        });
        javalin.post("/user", ctx -> {//register a user:
            try {
                UserService.RegisterRequest req = serializer.fromJson(ctx.body(), UserService.RegisterRequest.class);
                var result = userService.register(req);
                ctx.status(200).contentType("application/json").result(serializer.toJson(result));//success
            } catch (DataAccessException e) {
                String message = e.getMessage();
                if (message.equals("bad request")) {
                    ctx.status(400).contentType("application/json").result("{\"message\":\"Error: bad request\"}");
                } else if (message.equals("already taken")) {
                    ctx.status(403).contentType("application/json").result("{\"message\":\"Error: already taken\"}");
                } else {
                    ctx.status(500).contentType("application/json").result("{\"message\":\"Error: " +message + "\"}");
                }
            }
        });
        javalin.post("/session", ctx -> { //login:
            try {
                UserService.LoginRequest req = serializer.fromJson(ctx.body(), UserService.LoginRequest.class);
                var result = userService.login(req);
                ctx.status(200).contentType("application/json").result(serializer.toJson(result));
            } catch (DataAccessException e) {
                String message = e.getMessage();
                if (message.equals("bad request")) {
                    ctx.status(400).contentType("application/json").result("{\"message\":\"Error: bad request\"}");
                } else if (message.equals("unauthorized")){
                    ctx.status(401).contentType("application/json").result("{\"message\":\"Error: unauthorized\"}");
                } else {
                    ctx.status(500).contentType("application/json").result("{\"message\":\"Error: " +message + "\"}");
                }
            }
        });
        javalin.delete("/session", ctx -> {//logout:
            try {
                String token = ctx.header("authorization");//need to get authtoken from the header i think
                if (token ==null) { throw new DataAccessException("unauthorized");}
                userService.logout(token);
                ctx.status(200).contentType("application/json").result("{}");
            } catch (DataAccessException e) {
                String message = e.getMessage();
                if (message.equals("unauthorized")) {
                    ctx.status(401).contentType("application/json").result("{\"message\":\"Error: unauthorized\"}");
                } else{
                    ctx.status(500).contentType("application/json").result("{\"message\":\"Error: " + message + "\"}");
                }
            }
        });
        javalin.get("/game", ctx -> {//list games:
            try {
                String token = ctx.header("authorization");
                var result = gameService.listGames(token);
                ctx.status(200).contentType("application/json").result(serializer.toJson(result));
            } catch (DataAccessException e) {
                String message = e.getMessage();
                if ("unauthorized".equals(message)) {
                    ctx.status(401).contentType("application/json").result("{\"message\":\"Error: unauthorized\"}" );
                } else {
                    ctx.status(500).contentType("application/json").result("{\"message\":\"Error: " + message + "\"}");
                }
            }
        });
        javalin.post("/game", ctx -> {//create a game:
            try {
                String token = ctx.header("authorization");
                GameService.CreateGameRequest req = serializer.fromJson(ctx.body(), GameService.CreateGameRequest.class);
                ctx.status(200).contentType("application/json").result(serializer.toJson(gameService.createGame(token, req)));
            } catch (DataAccessException e) {
                String message = e.getMessage();
                if ("bad request".equals(message)) {
                    ctx.status(400).contentType("application/json").result("{\"message\":\"Error: bad request\"}");
                } else if ("unauthorized".equals(message)) {
                    ctx.status(401).contentType("application/json").result("{\"message\":\"Error: unauthorized\"}");
                } else {
                    ctx.status(500).contentType("application/json").result("{\"message\":\"Error: " + message + "\"}");
                }
            }
        });
        javalin.put("/game", ctx -> {//join game:
            try {
                String token = ctx.header("authorization");
                GameService.JoinGameRequest req = serializer.fromJson(ctx.body(), GameService.JoinGameRequest.class);
                gameService.joinGame(token, req);
                ctx.status(200).contentType("application/json").result("{}");
            } catch (DataAccessException e) {
                String message = e.getMessage();
                switch (message) {
                    case "bad request" -> ctx.status(400).contentType("application/json").result("{\"message\":\"Error: bad request\"}");
                    case "unauthorized" -> ctx.status(401).contentType("application/json").result("{\"message\":\"Error: unauthorized\"}");
                    case "already taken" -> ctx.status(403).contentType("application/json").result("{\"message\":\"Error: already taken\"}");
                    case null, default -> ctx.status(500).contentType("application/json").result("{\"message\":\"Error: " + message + "\"}");
                }
            }
        });
    }
    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }
    public void stop() {
        javalin.stop();
    }
}