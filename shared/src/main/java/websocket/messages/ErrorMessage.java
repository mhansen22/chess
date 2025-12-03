package websocket.messages;
import com.google.gson.Gson;

//String errorMessage
//This message is sent to a client when it sends an invalid command.
//The message must include the word Error.
public class ErrorMessage extends ServerMessage {
    private final String errorMessage;

    public ErrorMessage(String errorMessage) {
        super(ServerMessageType.ERROR);
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}