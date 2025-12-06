package server;
import io.javalin.*;
import com.google.gson.Gson;
import dataaccess.*;
import service.*;
import server.websocket.WebSocketHandler;

public class Server {
    private final Javalin javalin;
    private final Gson serializer = new Gson();
    //DAOs:
    private final UserDAO users;
    private final GameDAO games;
    private final AuthDAO auths;
    //services:
    private final ResetService resetService;
    private final GameService gameService;
    private final UserService userService;

    public Server() {
        try {
            DatabaseManager.createDatabase();
            this.users = new UserDAOmySQL();
            this.games = new GamesDAOmySQL();
            this.auths = new AuthDAOmySQL();
        } catch (DataAccessException e) {
            throw new RuntimeException("not able to initialize the DAOs", e);
        }
        this.resetService = new ResetService(users, auths, games);
        this.gameService = new GameService(games, auths);
        this.userService = new UserService(users, auths);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        //first, to clear everything:
        javalin.delete("/db", ctx -> {
            try {
                resetService.clear();
                ctx.status(200).contentType("application/json").result("{}");
            } catch (DataAccessException e) {
                String message = e.getMessage();
                ctx.status(500).contentType("application/json").result("{\"message\":\"Error: " + message + "\"}");
            }//server failure!!!! important
        });
        //register a user:
        javalin.post("/user", ctx -> {
            try {
                UserService.RegisterRequest req = serializer.fromJson(ctx.body(), UserService.RegisterRequest.class);
                var result = userService.register(req);
                ctx.status(200).contentType("application/json").result(serializer.toJson(result));//success
            } catch (DataAccessException e) {
                ErrorResponse err = errorHelper(e.getMessage());
                ctx.status(err.status()).contentType("application/json").result(err.body());//for code quality check, shortned w helper func
            }
        });
        //login:
        javalin.post("/session", ctx -> {
            try {
                UserService.LoginRequest req = serializer.fromJson(ctx.body(), UserService.LoginRequest.class);
                var result = userService.login(req);
                ctx.status(200).contentType("application/json").result(serializer.toJson(result));
            } catch (DataAccessException e) {
                ErrorResponse err = errorHelper(e.getMessage());
                ctx.status(err.status()).contentType("application/json").result(err.body());//for code quality check, shortned w helper func
            }
        });
        //logout:
        javalin.delete("/session", ctx -> {
            try {
                String token = ctx.header("authorization");//need to get authtoken from the header i think
                if (token ==null) { throw new DataAccessException("unauthorized");}
                userService.logout(token);
                ctx.status(200).contentType("application/json").result("{}");
            } catch (DataAccessException e) {
                ErrorResponse err = errorHelper(e.getMessage());
                ctx.status(err.status()).contentType("application/json").result(err.body());//for code quality check, shortned w helper func
            }
        });
        //list games:
        javalin.get("/game", ctx -> {
            try {
                String token = ctx.header("authorization");
                var result = gameService.listGames(token);
                ctx.status(200).contentType("application/json").result(serializer.toJson(result));
            } catch (DataAccessException e) {
                ErrorResponse err = errorHelper(e.getMessage());
                ctx.status(err.status()).contentType("application/json").result(err.body());//for code quality check, shortned w helper func
            }
        });
        //create a game:
        javalin.post("/game", ctx -> {
            try {
                String token = ctx.header("authorization");
                GameService.CreateGameRequest req = serializer.fromJson(ctx.body(), GameService.CreateGameRequest.class);
                ctx.status(200).contentType("application/json").result(serializer.toJson(gameService.createGame(token, req)));
            } catch (DataAccessException e) {
                ErrorResponse err = errorHelper(e.getMessage());
                ctx.status(err.status()).contentType("application/json").result(err.body());//for code quality check, shortned w helper func
            }
        });
        //join game:
        javalin.put("/game", ctx -> {
            try {
                String token = ctx.header("authorization");
                GameService.JoinGameRequest req = serializer.fromJson(ctx.body(), GameService.JoinGameRequest.class);
                gameService.joinGame(token, req);
                ctx.status(200).contentType("application/json").result("{}");
            } catch (DataAccessException e) {
                ErrorResponse err = errorHelper(e.getMessage());
                ctx.status(err.status()).contentType("application/json").result(err.body());//for code quality check, shortned w helper func
            }
        });
        var wsHandler = new WebSocketHandler(auths, games);

        javalin.ws("/ws", ws -> {
            ws.onConnect(wsHandler);
            ws.onMessage(wsHandler);
            ws.onClose(wsHandler);
        });
    }
    //helper func to reduce code lines and duplicate code
    //matches error message!!
    private record ErrorResponse(int status, String body) {}
    private ErrorResponse errorHelper(String message) {
        return switch (message) {
            case "bad request" ->new ErrorResponse(400,"{\"message\":\"Error: bad request\"}");
            case "unauthorized" -> new ErrorResponse(401, "{\"message\":\"Error: unauthorized\"}");
            case "already taken" -> new ErrorResponse (403,"{\"message\":\"Error: already taken\"}");
            case null, default -> new ErrorResponse(500, "{\"message\":\"Error: " + message + "\"}");
        };
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }
    public void stop() {
        javalin.stop();
    }
}