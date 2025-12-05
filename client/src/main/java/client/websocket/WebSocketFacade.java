package client.websocket;

import com.google.gson.Gson;
import client.ClientException;
import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ErrorMessage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

//need to extend Endpoint for websocket to work properly
@ClientEndpoint
public class WebSocketFacade extends Endpoint {
//    private Session session;
    Session session;
    private final ServerMessageHandler handler;
    private final Gson gson = new Gson();

    public WebSocketFacade(String url, ServerMessageHandler handler) throws ClientException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");

            this.handler = handler;
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {

                @Override
                public void onMessage(String message) {
                    ServerMessage envelope = gson.fromJson(message, ServerMessage.class);
                    ServerMessage.ServerMessageType type = envelope.getServerMessageType();
                    ServerMessage full;
                    switch (type) {
                        case LOAD_GAME -> full = gson.fromJson(message, LoadGameMessage.class);
                        case NOTIFICATION -> full = gson.fromJson(message, NotificationMessage.class);
                        case ERROR -> full = gson.fromJson(message, ErrorMessage.class);
                        default ->full = envelope;
                    }
                    handler.handle(full);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ClientException("could not connect to websocket " + ex.getMessage());
        }
    }
    //from petshop example!!
    @Override
    public void onOpen(Session session, EndpointConfig config) {
    }

    public void send(UserGameCommand command) throws ClientException {
        try {
            String json = gson.toJson(command);
            session.getBasicRemote().sendText(json);
        } catch (IOException ex) {
            throw new ClientException("trouble sending websocket command! " + ex.getMessage());
        }
    }

    public void close() {
        try {
            if ((session != null) &&(session.isOpen())) {
                session.close();
            }
        } catch (IOException ignored) {
        }
    }
}