package websocket.messages;
import chess.ChessGame;
import com.google.gson.Gson;

//game (can be any type, just needs to be called game)
//Used by the server to send the current game state to a client.
//When a client receives this message, it will redraw the chess board.
public class LoadGameMessage extends ServerMessage {

    private final ChessGame game;

    public LoadGameMessage(ChessGame game) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
    }

    public ChessGame getGame() {
        return game;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}