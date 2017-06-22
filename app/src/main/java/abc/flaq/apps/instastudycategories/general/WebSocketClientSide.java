package abc.flaq.apps.instastudycategories.general;

import android.support.v7.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import abc.flaq.apps.instastudycategories.api.Api;
import abc.flaq.apps.instastudycategories.helper.Utils;
import abc.flaq.apps.instastudycategories.pojo.User;
import abc.flaq.apps.instastudycategories.pojo.WebSocketMessage;

import static abc.flaq.apps.instastudycategories.helper.Constants.API_WEB_SOCKET_ORIGIN;
import static abc.flaq.apps.instastudycategories.helper.Constants.API_WEB_SOCKET_URL;
import static abc.flaq.apps.instastudycategories.helper.Constants.WEB_SOCKET_DATA;
import static abc.flaq.apps.instastudycategories.helper.Constants.WEB_SOCKET_TYPE;
import static abc.flaq.apps.instastudycategories.helper.Constants.WEB_SOCKET_TYPE_MESSAGE;
import static abc.flaq.apps.instastudycategories.helper.Constants.WEB_SOCKET_TYPE_TOTAL_USERS;

// FIXME: szybkie otwarcie czatu wywala błąd
public class WebSocketClientSide extends WebSocketClient {

    private AppCompatActivity clazz;
    private ObjectMapper mapper = new ObjectMapper();
    private WebSocketListener webSocketListener;

    public static WebSocketClientSide createWebSocketClientSide(AppCompatActivity clazz) {
        URI uri = null;
        try {
            uri = new URI(API_WEB_SOCKET_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Draft draft = new Draft_17();
        Map<String, String> headers = new HashMap<>();
        headers.put("Origin", API_WEB_SOCKET_ORIGIN);
        int timeout = 0;
        return new WebSocketClientSide(uri, draft, headers, timeout, clazz);
    }
    private WebSocketClientSide(URI uri, Draft draft, Map<String, String> headers, int timeout, AppCompatActivity clazz) {
        super(uri, draft, headers, timeout);
        this.clazz = clazz;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        Utils.logDebug(clazz, "Opened WebSocket");
        webSocketListener.onConnectionOpen();
    }

    @Override
    public void onMessage(String s) {
        try {
            JSONObject json = new JSONObject(s);
            String type = json.getString(WEB_SOCKET_TYPE);

            if (WEB_SOCKET_TYPE_MESSAGE.equals(type)) {
                final WebSocketMessage message = mapper.readValue(json.getString(WEB_SOCKET_DATA), WebSocketMessage.class);
                Utils.logDebug(clazz, "Message in chat: " + message.toString());
                User user = Api.getUserByUsername(message.getName());
                if (Utils.isEmpty(user)) {
                    message.setProfilePic("");
                } else {
                    message.setProfilePic(user.getProfilePicUrl());
                }
                webSocketListener.onMessageTypeMsg(message);
            } else if (WEB_SOCKET_TYPE_TOTAL_USERS.equals(type)) {
                final Integer totalUsers = json.getInt(WEB_SOCKET_DATA);
                Utils.logDebug(clazz, "Total users in chat: " + totalUsers.toString());
                webSocketListener.onMessageTypeNum(totalUsers);
            } else {
                handleConnectionError("Unknown WebSocket message type");
            }
        } catch (JSONException e) {
            handleConnectionError("JSONException: " + e.getMessage());
        } catch (JsonParseException e) {
            handleConnectionError("JsonParseException: " + e.getMessage());
        } catch (JsonMappingException e) {
            handleConnectionError("JsonMappingException: " + e.getMessage());
        } catch (IOException e) {
            handleConnectionError("IOException: " + e.getMessage());
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        Utils.logDebug(clazz, "Closed WebSocket: " + s);
    }

    @Override
    public void onError(Exception e) {
        handleConnectionError("Error WebSocket: " + e.getMessage());
    }

    public void sendMessage(String message) {
        if (getConnection().isOpen() && Utils.isNotEmpty(Session.getInstance().getUser())) {
            try {
                WebSocketMessage newMessage = new WebSocketMessage(message, Session.getInstance().getUser().getUsername(), new Date());
                String json = mapper.writeValueAsString(newMessage);
                send(json);
            } catch (JsonProcessingException e) {
                handleConnectionError("JsonProcessingException: " + e.getMessage());
            }
        } else {
            handleConnectionError("Connection closed: " + getConnection().isOpen() + " or User not logged in");
        }
    }

    private void handleConnectionError(String logMessage) {
        webSocketListener.onConnectionError(logMessage);
    }

    public void setWebSocketListener(WebSocketListener webSocketListener) {
        this.webSocketListener = webSocketListener;
    }

    public interface WebSocketListener {
        void onConnectionOpen();
        void onConnectionError(String logMessage);
        void onMessageTypeMsg(WebSocketMessage message);
        void onMessageTypeNum(Integer totalUsers);
    }

}
