package server.websocket;
import com.google.gson.Gson;
import io.javalin.websocket.*;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import java.util.HashSet;
import java.util.Set;

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
}