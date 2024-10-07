import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.json.*;

// ServerEndpoint annotation specifies the WebSocket URI.
@ServerEndpoint("/multi-quiz/{sessionId}")
public class QuizWebSocket {

    // Map to store each quiz session's participants (both moderators and players).
    private static final Map<String, Set<Session>> sessionMap = new ConcurrentHashMap<>();

    // Map to store the type of each session (either "MODERATOR" or "PLAYER").
    private static final Map<Session, String> sessionType = new ConcurrentHashMap<>();

    // Track the session ID for each WebSocket session.
    private static final Map<Session, String> sessionIds = new ConcurrentHashMap<>();

    // Track if a session already has a moderator.
    private static final Map<String, Session> sessionModerators = new ConcurrentHashMap<>();

    // The session ID for the current WebSocket instance.
    private String currentSessionId;

    // Invoked when a new WebSocket connection is opened.
    @OnOpen
    public void onOpen(Session session, @PathParam("sessionId") String sessionId) {
        currentSessionId = sessionId;

        // Initialize the session set if it doesn't exist.
        sessionMap.putIfAbsent(sessionId, Collections.newSetFromMap(new ConcurrentHashMap<>()));
        WebSocketSessionServlet.sessionIDs.add(sessionId);

        // Add the new session to the set of participants.
        sessionMap.get(sessionId).add(session);

        System.out.println("New connection: " + session.getId() + " for session: " + sessionId);
    }

    // Invoked when a message is received from a client.
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Received message: " + message + " from session: " + session.getId());

        // Parse the message to identify the type and content.
        JSONObject messageJSON = new JSONObject(message);

        switch (messageJSON.getString("action")) {
            case "MODERATOR_JOIN":
                handleModeratorJoin(session);
                break;

            case "PLAYER_JOIN":
                handlePlayerJoin(session);
                break;
            default:
                broadcast(message, currentSessionId);
        }
    }

    // Invoked when a WebSocket connection is closed.
    @OnClose
    public void onClose(Session session) {
        String sessionId = sessionIds.get(session);

        // Remove the session from all mappings.
        if (sessionId != null) {
            sessionMap.getOrDefault(sessionId, Collections.emptySet()).remove(session);
            sessionType.remove(session);
            sessionIds.remove(session);

            // Remove the moderator reference if the session being closed was a moderator.
            if (sessionModerators.get(sessionId) == session) {
                sessionModerators.remove(sessionId);
                System.out.println("Moderator for session " + sessionId + " has disconnected.");
            }
        }

        System.out.println("Connection closed: " + session.getId() + " for session: " + sessionId);
    }

    // Invoked when there is an error with a WebSocket session.
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Error on session " + session.getId() + ": " + throwable.getMessage());
    }

    // Handle when a client wants to join as a moderator.
    private void handleModeratorJoin(Session session) {
        JSONObject messageJSON = new JSONObject();

        if (sessionModerators.containsKey(currentSessionId)) {
            // If a moderator is already present, send an error and close the session.
            messageJSON.put("message", "Session already has a moderator. Connection closed.");
            messageJSON.put("action", "MODERATOR_EXISTS");
            sendMessage(session, messageJSON.toString());
        } else {
            // Register the session as a moderator and store the session information.
            sessionType.put(session, "MODERATOR");
            sessionIds.put(session, currentSessionId);
            sessionModerators.put(currentSessionId, session);
            messageJSON.put("message", "You have joined as the moderator.");
            messageJSON.put("action", "MODERATOR_JOINED");
            broadcast(messageJSON.toString(), currentSessionId);
            System.out.println("Moderator joined for session: " + currentSessionId);
        }
    }

    // Handle when a client wants to join as a player.
    private void handlePlayerJoin(Session session) {
        JSONObject messageJSON = new JSONObject();

        // Allow players to join without restriction.
        sessionType.put(session, "PLAYER");
        sessionIds.put(session, currentSessionId);
        messageJSON.put("message", "You have joined as a player.");
        messageJSON.put("action", "PLAYER_JOINED");
        broadcast(messageJSON.toString(), currentSessionId);
        System.out.println("Player joined for session: " + currentSessionId);
    }

    // Broadcasts a message to all participants in a given session.
    private void broadcast(String message, String sessionId) {
        Set<Session> participants = sessionMap.getOrDefault(sessionId, Collections.emptySet());
        System.out.println("Participants in session " + currentSessionId + ": " + sessionMap.get(currentSessionId).size());
        for (Session participant : participants) {
            try {
                participant.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Sends a message to a specific session.
    private void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Close a specific WebSocket session with a reason.
    private void closeSession(Session session, String reason) {
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, reason));
        } catch (IOException e) {
            e.printStackTrace();
        }
        WebSocketSessionServlet.sessionIDs.remove(currentSessionId);
    }
}