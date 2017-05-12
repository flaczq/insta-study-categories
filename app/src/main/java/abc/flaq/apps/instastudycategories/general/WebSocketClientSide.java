package abc.flaq.apps.instastudycategories.general;

import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import abc.flaq.apps.instastudycategories.adapter.ChatAdapter;
import abc.flaq.apps.instastudycategories.helper.Utils;
import abc.flaq.apps.instastudycategories.pojo.WebSocketMessage;

import static abc.flaq.apps.instastudycategories.helper.Constants.API_WEB_SOCKET_ORIGIN;
import static abc.flaq.apps.instastudycategories.helper.Constants.API_WEB_SOCKET_URL;

public class WebSocketClientSide extends WebSocketClient {

    private AppCompatActivity clazz;
    private CoordinatorLayout layout;
    private ChatAdapter adapter;
    private ObjectMapper mapper = new ObjectMapper();

    public static WebSocketClientSide createWebSocketClientSide(AppCompatActivity clazz, CoordinatorLayout layout, ChatAdapter adapter) {
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
        return new WebSocketClientSide(uri, draft, headers, timeout, clazz, layout, adapter);
    }
    private WebSocketClientSide(URI uri, Draft draft, Map<String, String> headers, int timeout, AppCompatActivity clazz, CoordinatorLayout layout, ChatAdapter adapter) {
        super(uri, draft, headers, timeout);
        this.clazz = clazz;
        this.layout = layout;
        this.adapter = adapter;
        connect();
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        Utils.logDebug(clazz, "Opened WebSocket");
    }

    @Override
    public void onMessage(String s) {
        try {
            final WebSocketMessage message = mapper.readValue(s, WebSocketMessage.class);
            Utils.logDebug(clazz, message.toString());
            clazz.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.addItem(message);
                }
            });
        } catch (JsonParseException e) {
            Utils.logError(clazz, "JsonParseException: " + e.getMessage());
        } catch (JsonMappingException e) {
            Utils.logError(clazz, "JsonMappingException: " + e.getMessage());
        } catch (IOException e) {
            Utils.logError(clazz, "IOException: " + e.getMessage());
            Utils.showConnectionError(layout, "Błąd parsowania odpowiedzi WebSocket");
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        Utils.logDebug(clazz, "Closed WebSocket: " + s);
    }

    @Override
    public void onError(Exception e) {
        Utils.logDebug(clazz, "Error WebSocket: " + e.getMessage());
    }

    public void sendMessage(String message) {
        if (getConnection().isOpen()) {
            try {
                WebSocketMessage newMessage = new WebSocketMessage(message, Session.getInstance().getUser().getUsername(), new Date());
                String json = mapper.writeValueAsString(newMessage);
                send(json);
            } catch (JsonProcessingException e) {
                Utils.logError(clazz, "JsonProcessingException: " + e.getMessage());
            }
        } else {
            Utils.showConnectionError(layout, "Błąd połączenia z czatem");
        }
    }

}
