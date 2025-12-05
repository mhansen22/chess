package client.websocket;

import websocket.messages.ServerMessage;

public interface ServerMessageHandler {
    void handle(ServerMessage message);
}


