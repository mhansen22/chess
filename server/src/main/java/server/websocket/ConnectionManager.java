package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    record Connection(Session session, String username, int gameID) {}
    private final ConcurrentHashMap<Session, Connection> connections = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public void add(Session session, String username, int gameID) {
        connections.put(session, new Connection(session, username, gameID));
    }

    public void remove(Session session) {
        connections.remove(session);
    }

    public Connection get(Session session) {
        return connections.get(session);
    }

    public void send(Session session, ServerMessage message) throws IOException {
        if ((session != null) &&(session.isOpen()) ) {
            session.getRemote().sendString(gson.toJson(message));
        }
    }

    public void broadcast(int gameID, Session excludeSession, ServerMessage message) throws IOException {
        String msg = gson.toJson(message);
        for (Connection c : connections.values()) {
            Session s = c.session();
            if ((c.gameID()==gameID) && (s.isOpen()) && (!s.equals(excludeSession))) {
                s.getRemote().sendString(msg);
            }
        }
    }
}