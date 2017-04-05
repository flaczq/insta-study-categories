package abc.flaq.apps.instastudycategories.utils;

import android.content.Context;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import abc.flaq.apps.instastudycategories.pojo.instagram.InstagramAccessToken;
import abc.flaq.apps.instastudycategories.pojo.instagram.InstagramResponse;

import static abc.flaq.apps.instastudycategories.utils.Constants.INSTAGRAM_AUTH_URL;
import static abc.flaq.apps.instastudycategories.utils.Constants.INSTAGRAM_CODE_URL;
import static abc.flaq.apps.instastudycategories.utils.Constants.INSTAGRAM_REDIRECT_URL;
import static abc.flaq.apps.instastudycategories.utils.Constants.INSTAGRAM_REQUEST_ACCESS_TOKEN_URL;
import static abc.flaq.apps.instastudycategories.utils.GeneralUtils.isNotEmpty;

public class InstagramUtils {

    private static Map<String, Object> jsonObjectToMap(JSONObject json) throws JSONException {
        Map<String, Object> map = new HashMap<>();

        if(json != JSONObject.NULL) {
            map = toMap(json);
        }
        return map;
    }
    private static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = object.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            Object value = object.get(key);

            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if (value instanceof JSONObject) {
                value = jsonObjectToMap((JSONObject) value);
            }
            map.put(key, value);
        }

        return map;
    }
    private static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);

            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }

        return list;
    }

    public static String getAuthUrl(Constants.INSTAGRAM_SCOPES... scopes) throws URISyntaxException {
        String authUrl = INSTAGRAM_AUTH_URL;
        for (int i = 0; i < scopes.length; i++) {
            authUrl += scopes[i];
            if (i < scopes.length - 1) {
                authUrl += "+";
            }
        }
        return new URI("https", "api.instagram.com", "/oauth/authorize", authUrl, null).toString();
    }

    public static String getAccessTokenFromUrl(Context context, String url) {
        if (isNotEmpty(url)) {
            int errorIndex = url.indexOf("?error=");
            if (errorIndex >= 0) {
                Toast.makeText(context, url.substring(errorIndex + "?error=".length()), Toast.LENGTH_SHORT).show();
                return null;
            }

            int accessTokenIndex = url.lastIndexOf(INSTAGRAM_REDIRECT_URL + "/#access_token=");
            if (accessTokenIndex >= 0) {
                return url.substring(accessTokenIndex + (INSTAGRAM_REDIRECT_URL + "/#access_token=").length());
            }
        }
        return null;
    }

    public static String getCodeFromUrl(Context context, String url) {
        if (isNotEmpty(url)) {
            int errorIndex = url.indexOf("?error=");
            if (errorIndex >= 0) {
                Toast.makeText(context, url.substring(errorIndex + "?error=".length()), Toast.LENGTH_SHORT).show();
                return null;
            }

            int codeIndex = url.lastIndexOf(INSTAGRAM_REDIRECT_URL + "/?code=");
            if (codeIndex >= 0) {
                return url.substring(codeIndex + (INSTAGRAM_REDIRECT_URL + "/?code=").length());
            }
        }
        return null;
    }

    public static InstagramAccessToken getAccessTokenByCode(String code) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = Api.simplePost(INSTAGRAM_REQUEST_ACCESS_TOKEN_URL, INSTAGRAM_CODE_URL + code);
        String stream = Api.getStream(is);

        InstagramAccessToken accessToken = mapper.readValue(stream, InstagramAccessToken.class);
        if (accessToken.isError()) {
            return accessToken;
        }

        accessToken = mapper.readValue(stream, InstagramAccessToken.class);
        return accessToken;
    }

    public static Map<String, Object> getData(String url) throws IOException, JSONException {
        Map<String, Object> values;
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = Api.simpleGet(url);
        String stream = Api.getStream(is);

        JSONObject jsonObject = new JSONObject(stream);
        String meta = jsonObject.getString("meta");
        InstagramResponse response = mapper.readValue(meta, InstagramResponse.class);
        if (response.isError()) {
            values = new android.support.v4.util.ArrayMap<>();
            values.put("type", response.getType());
            values.put("message", response.getMessage());
        } else {
            JSONObject data = jsonObject.getJSONObject("data");
            values = jsonObjectToMap(data);
        }

        return values;
    }

}
