package server.websocket;
import com.google.gson.Gson;
import io.javalin.websocket.*;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import java.util.HashSet;
import java.util.Set;
import model.AuthData;
import model.Game;
import chess.ChessMove;
import chess.ChessGame;
import chess.InvalidMoveException;
import java.io.IOException;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import dataaccess.DataAccessException;
import chess.ChessPosition;

//this is just like petshop ex:
public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();
    private final AuthDAO auths;
    private final GameDAO games;
    private final Set<Integer> gamesRes = new HashSet<>();

    //from petshop example!!
    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    public WebSocketHandler(AuthDAO auths, GameDAO games) {
        this.auths=auths;
        this.games=games;
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        Session session = ctx.session;
        try {
            UserGameCommand action = gson.fromJson(ctx.message(), UserGameCommand.class);
            switch (action.getCommandType()) {
                case CONNECT -> connect(session, action);
                case MAKE_MOVE -> makeMove(session, action);
                case LEAVE -> leave(session, action);
                case RESIGN -> resign(session, action);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("WebSocket closed");
        connections.remove(ctx.session);
    }

    private void connect(Session session, UserGameCommand command) {
        try {
            String token = command.getAuthToken();
            if (token==null) {
                throw new DataAccessException("unauthorized");
            }
            AuthData auth = auths.getAuth(token);
            if (auth==null) {
                throw new DataAccessException("unauthorized");
            }
            String user = auth.username();
            int gameID = command.getGameID();

            Game game =games.getGame(gameID);
            if (game==null) {
                throw new DataAccessException("game not found");
            }
            String colorOrRole;
            if (user.equals(game.whiteUser())) {
                colorOrRole= "white";
            } else if (user.equals(game.blackUser()) ) {
                colorOrRole= "black";
            } else{
                colorOrRole= "observer";
            }
            connections.add(session, user, gameID);//reg
            connections.send(session,new LoadGameMessage(game.game()));//LOAD_GAME
            String notification = user + " joined the game as "+colorOrRole;//NOTIFICATION
            connections.broadcast(gameID, session,new NotificationMessage(notification)) ;
        } catch (DataAccessException ex){
            try {
                connections.send(session, new ErrorMessage(ex.getMessage()));
            } catch (IOException ignored) {}
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    private void makeMove(Session session, UserGameCommand command) {
        try {
            int gameID = command.getGameID();
            if (gamesRes.contains(gameID)) {//no moves after resign
                throw new DataAccessException("game is over");
            }
            //who is making move-->
            String token = command.getAuthToken();
            if (token==null){
                throw new DataAccessException("unauthorized");
            }
            AuthData auth = auths.getAuth(token);
            if (auth== null) {
                throw new DataAccessException("unauthorized");
            }
            String user =auth.username();

            ChessMove move = command.getMove();
            Game game = games.getGame(gameID);
            if (game==null) {
                throw new DataAccessException("game not found");
            }
            ChessGame chessGame =game.game();
            //turn
            moveAllowed(game, chessGame, user, move);
            chessGame.makeMove(move);//to use ChessGame for moves
            Game updated = new Game(game.gameId(),game.whiteUser(),game.blackUser(),game.gameName(),chessGame);
            games.updateGame(updated);
            connections.broadcast(gameID, null,new LoadGameMessage(chessGame));//LOAD_GAME
            //NOTIFICATION
            String moveText = user+" moved from "+formattingHelper(move.getStartPosition())+" to "+formattingHelper(move.getEndPosition());
            connections.broadcast(gameID,session,new NotificationMessage(moveText));

            ChessGame.TeamColor toMove = chessGame.getTeamTurn();//who's turn
            if (chessGame.isInCheckmate(toMove)) {
                connections.broadcast(gameID, null,new NotificationMessage(toMove +" in checkmate"));
                gamesRes.add(gameID);//game done
            } else if (chessGame.isInStalemate(toMove)) {
                connections.broadcast(gameID, null,new NotificationMessage("stalemate"));
                gamesRes.add(gameID);
            } else if (chessGame.isInCheck(toMove)) {
                connections.broadcast(gameID, null,new NotificationMessage(toMove + " in check"));
            }
        } catch (DataAccessException |InvalidMoveException ex) {
            try {
                connections.send(session, new ErrorMessage(ex.getMessage()));
            } catch (IOException ignored) {}
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    private void leave(Session session, UserGameCommand command) {
        var connection = connections.get(session);
        if (connection==null) {
            return;
        }
        String username=connection.username();
        int gameID = command.getGameID();
        try {
            Game game = games.getGame(gameID);
            if (game==null){
                throw new DataAccessException("game not found");
            }
            Game upToDateGame = game;
            if (username.equals(game.whiteUser())){
                upToDateGame = new Game(game.gameId(), null, game.blackUser(),game.gameName(),game.game());
            } else if (username.equals(game.blackUser())) {
                upToDateGame = new Game(game.gameId(), game.whiteUser(), null,game.gameName(), game.game()) ;
            }
            games.updateGame(upToDateGame);
            connections.broadcast(gameID, session,new NotificationMessage(username + " left the game"));
        } catch (DataAccessException ignored) {
        } catch (IOException io) {
            io.printStackTrace();
        }
        connections.remove(session);
    }

    private void resign(Session session, UserGameCommand command) {
        var connection = connections.get(session);
        if (connection==null){
            return;
        }
        String username = connection.username();
        int gameID = command.getGameID();
        try {
            Game game =games.getGame(gameID);
            if (game==null) {
                throw new DataAccessException("game not found");
            }
            ChessGame chessGame = game.game();
            boolean whiteYes = username.equals(game.whiteUser());
            boolean blackYes = username.equals(game.blackUser()) ;
            //this bc observers dont resign
            if (!whiteYes &&!blackYes) {
                throw new DataAccessException("observers can't resign ");
            }
            if ((chessGame.isInCheckmate(ChessGame.TeamColor.WHITE) )||(chessGame.isInCheckmate(ChessGame.TeamColor.BLACK)) ||
                    (chessGame.isInStalemate(ChessGame.TeamColor.BLACK)) ||(chessGame.isInStalemate(ChessGame.TeamColor.WHITE))||
                    gamesRes.contains(gameID)){
                throw new DataAccessException("game already over");
            }
            gamesRes.add(gameID);
            connections.broadcast(gameID, null,new NotificationMessage(username +" resigned"));
        } catch (DataAccessException ex) {
            try {
                connections.send(session,new ErrorMessage(ex.getMessage()));
            } catch (IOException ignored) {}
        } catch (IOException io){
            io.printStackTrace();
        }
    }
    //helperFuncs here::
    private String formattingHelper(ChessPosition position) {
        int col = position.getColumn();
        int row = position.getRow();
        char borderCol = (char)('a'+(col-1));//to convert here-->
        return "" + borderCol +row;
    }
    private void moveAllowed(Game game, ChessGame chessGame, String username, ChessMove move) throws DataAccessException {
        //game not over, so ORs
        if ((chessGame.isInCheckmate(ChessGame.TeamColor.WHITE))||(chessGame.isInCheckmate(ChessGame.TeamColor.BLACK))||
                (chessGame.isInStalemate(ChessGame.TeamColor.WHITE))||(chessGame.isInStalemate(ChessGame.TeamColor.BLACK))) {
            throw new DataAccessException("game already over");
        }
        //turn logic-->
        ChessGame.TeamColor turn=chessGame.getTeamTurn();
        boolean blackPlayerYes = username.equals(game.blackUser());
        boolean whitePlayerYes = username.equals(game.whiteUser());
        //observer can't move-->
        if (!whitePlayerYes && !blackPlayerYes) {
            throw new DataAccessException("observers can't move");
        }
        if ((!blackPlayerYes) && (turn==ChessGame.TeamColor.BLACK)) {
            throw new DataAccessException("not your turn");
        }
        if ((!whitePlayerYes)&&(turn==ChessGame.TeamColor.WHITE)) {
            throw new DataAccessException("not your turn");
        }
        var piece= chessGame.getBoard().getPiece(move.getStartPosition());
        if (piece ==null) {
            throw new DataAccessException("can't move empty square");
        }
        if (turn != piece.getTeamColor()) {//turn?
            throw new DataAccessException("can't move your opponent's piece");
        }
    }
}