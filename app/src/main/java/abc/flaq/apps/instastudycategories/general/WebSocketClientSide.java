package abc.flaq.apps.instastudycategories.general;

import android.app.Dialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

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

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.adapter.ChatAdapter;
import abc.flaq.apps.instastudycategories.api.Api;
import abc.flaq.apps.instastudycategories.helper.Utils;
import abc.flaq.apps.instastudycategories.pojo.User;
import abc.flaq.apps.instastudycategories.pojo.WebSocketMessage;

import static abc.flaq.apps.instastudycategories.helper.Constants.API_WEB_SOCKET_ORIGIN;
import static abc.flaq.apps.instastudycategories.helper.Constants.API_WEB_SOCKET_URL;
import static abc.flaq.apps.instastudycategories.helper.Constants.WEB_SOCKET_DATA;
import static abc.flaq.apps.instastudycategories.helper.Constants.WEB_SOCKET_MAX_MESSAGES;
import static abc.flaq.apps.instastudycategories.helper.Constants.WEB_SOCKET_TYPE;
import static abc.flaq.apps.instastudycategories.helper.Constants.WEB_SOCKET_TYPE_MESSAGE;
import static abc.flaq.apps.instastudycategories.helper.Constants.WEB_SOCKET_TYPE_TOTAL_USERS;

public class WebSocketClientSide extends WebSocketClient {

    private AppCompatActivity clazz;
    private CoordinatorLayout layout;
    private ChatAdapter adapter;
    private TextView chatHeader;
    private FloatingActionButton fab;
    private Dialog chatDialog;
    private ObjectMapper mapper = new ObjectMapper();

    public static WebSocketClientSide createWebSocketClientSide(AppCompatActivity clazz, CoordinatorLayout layout, ChatAdapter adapter, View view, Dialog chatDialog) {
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
        return new WebSocketClientSide(uri, draft, headers, timeout, clazz, layout, adapter, view, chatDialog);
    }
    private WebSocketClientSide(URI uri, Draft draft, Map<String, String> headers, int timeout, AppCompatActivity clazz, CoordinatorLayout layout, ChatAdapter adapter, View view, Dialog chatDialog) {
        super(uri, draft, headers, timeout);
        this.clazz = clazz;
        this.layout = layout;
        this.adapter = adapter;
        this.chatHeader = (TextView) view.findViewById(R.id.user_chat_header);
        this.fab = (FloatingActionButton) layout.findViewById(R.id.user_fab);
        this.chatDialog = chatDialog;

        connect();
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        fab.setClickable(true);
        Utils.logDebug(clazz, "Opened WebSocket");
    }

    @Override
    public void onMessage(String s) {
        try {
            JSONObject json = new JSONObject(s);
            String type = json.getString(WEB_SOCKET_TYPE);

            if (WEB_SOCKET_TYPE_MESSAGE.equals(type)) {
                final WebSocketMessage message = mapper.readValue(json.getString(WEB_SOCKET_DATA), WebSocketMessage.class);
                Utils.logDebug(clazz, message.toString());

                User user = Api.getUserByUsername(message.getName());
                if (Utils.isEmpty(user)) {
                    message.setProfilePic("");
                } else {
                    message.setProfilePic(user.getProfilePicUrl());
                }
                clazz.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.addItem(message);
                        if (adapter.getCount() > WEB_SOCKET_MAX_MESSAGES) {
                            fab.setImageResource(R.drawable.ic_chat_white_24dp);
                        }
                    }
                });
            } else if (WEB_SOCKET_TYPE_TOTAL_USERS.equals(type)) {
                final Integer totalUsers = json.getInt(WEB_SOCKET_DATA);
                clazz.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatHeader.setText(clazz.getString(R.string.chat_total_users, totalUsers));
                    }
                });
                Utils.logDebug(clazz, totalUsers.toString());
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
        Utils.logDebug(clazz, "Error WebSocket: " + e.getMessage());
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
        fab.setClickable(false);
        chatDialog.dismiss();
        Utils.showConnectionError(layout, logMessage, clazz.getString(R.string.error_chat_connection));
    }

}
