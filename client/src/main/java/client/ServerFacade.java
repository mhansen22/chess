package client;

import com.google.gson.Gson;
import model.AuthData;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Collection;
import model.Game;
import chess.ChessGame;

public class ServerFacade {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;
    private final Gson gson = new Gson();
    private String authToken;

    public ServerFacade(String serverUrl) {
        if (serverUrl.endsWith("/")) {
            this.serverUrl = serverUrl.substring(0, (serverUrl.length()-1));
        } else {
            this.serverUrl = serverUrl;
        }
    }

    public AuthData register(String username, String password, String email) throws ClientException {
        var request = buildRequest("POST", "/user", new RegisterRequest(username, password, email));
        var response = sendRequest(request);
        var auth = handleResponse(response, AuthData.class);
        authToken = auth.authToken();
        return auth;
    }
    public AuthData login(String username, String password) throws ClientException {
        var request = buildRequest("POST", "/session", new LoginRequest(username, password));
        var response = sendRequest(request);
        var auth = handleResponse(response, AuthData.class);
        authToken = auth.authToken();
        return auth;
    }
    public void logout() throws ClientException {
        var request = buildRequest("DELETE", "/session", null);
        var response = sendRequest(request);
        handleResponse(response, Void.class);
        authToken = null;
    }

    public int createGame(String gameName) throws ClientException {
        var request = buildRequest("POST", "/game", new CreateGameRequest(gameName));
        var response = sendRequest(request);
        var out = handleResponse(response, CreateGameResponse.class);
        return out.gameID();
    }

    public Collection<Game> listGames() throws ClientException {
        var request = buildRequest("GET", "/game", null);
        var response = sendRequest(request);
        var gamesListRes = handleResponse(response, ListGamesResponse.class);
        var result = new java.util.ArrayList<Game>();
        if (gamesListRes.games() != null){
            for (var game : gamesListRes.games()) {
                result.add(new Game(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), new ChessGame()));
            }
        }
        return result;
    }

    public void joinGame(Integer gameID, ChessGame.TeamColor color) throws ClientException {
        var request = buildRequest("PUT", "/game", new JoinGameRequest(color, gameID));
        var response = sendRequest(request);
        handleResponse(response, Void.class);
    }

    //helpers from petShop example, quick change
    private HttpRequest buildRequest(String method, String path, Object body) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body))
                .header("Accept", "application/json");
        if (body != null) {
            request.header("Content-Type", "application/json");
        }
        if (authToken != null){
            request.header("Authorization", authToken);
        }
        return request.build();
    }

    private BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ClientException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ClientException("network error" + ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ClientException {
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            var body = response.body();
            if (body != null) {
                ErrorResponse error = gson.fromJson(body, ErrorResponse.class);
                if ((error !=null) && (error.message != null)) {
                    throw new ClientException(status, error.message);
                }
            }
            throw new ClientException(status, "HTTP " + status);
        }
        if ((responseClass == null) ||(responseClass == Void.class)){
            return null;
        }
        return gson.fromJson(response.body(), responseClass);
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }

    public void clear() throws ClientException {
        var request = buildRequest("DELETE", "/db", null);
        var response = sendRequest(request);
        handleResponse(response, null);
        authToken = null;
    }
    //DTOs
    private record RegisterRequest(String username, String password, String email) {}
    private record LoginRequest(String username, String password) {}
    private static class ErrorResponse { public String message; }
    private record CreateGameRequest(String gameName) {}
    private record JoinGameRequest(ChessGame.TeamColor playerColor, Integer gameID) {}
    private record CreateGameResponse(int gameID) {}
    private record GameListInfo(int gameID, String whiteUsername, String blackUsername, String gameName) {}
    private record ListGamesResponse(Collection<GameListInfo> games) {}
}