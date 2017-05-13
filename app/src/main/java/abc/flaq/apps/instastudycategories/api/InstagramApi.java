package abc.flaq.apps.instastudycategories.api;

import android.view.View;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.pojo.instagram.InstagramAccessToken;
import abc.flaq.apps.instastudycategories.pojo.instagram.InstagramMeta;
import abc.flaq.apps.instastudycategories.helper.Constants;
import abc.flaq.apps.instastudycategories.helper.Utils;

import static abc.flaq.apps.instastudycategories.helper.Constants.INSTAGRAM_AUTH_URL;
import static abc.flaq.apps.instastudycategories.helper.Constants.INSTAGRAM_CODE_URL;
import static abc.flaq.apps.instastudycategories.helper.Constants.INSTAGRAM_REDIRECT_URL;
import static abc.flaq.apps.instastudycategories.helper.Constants.INSTAGRAM_REQUEST_ACCESS_TOKEN_URL;

public class InstagramApi {

    private static ObjectMapper mapper = new ObjectMapper();

    private static Map<String, Object> jsonObjectToMap(JSONObject json) throws JSONException {
        Map<String, Object> map = new HashMap<>();

        if (json != JSONObject.NULL) {
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
            } else if (value instanceof JSONObject) {
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
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }

            list.add(value);
        }

        return list;
    }

    public static Map<String, Object> getDataToMap(String url) throws IOException, JSONException {
        Map<String, Object> values;
        ObjectMapper mapper = new ObjectMapper();
        InputStreamReader isr = Api.simpleGet(url);
        String stream = Api.getStream(isr);

        JSONObject jsonObject = new JSONObject(stream);
        String meta = jsonObject.getString("meta");
        InstagramMeta response = mapper.readValue(meta, InstagramMeta.class);

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

    public static String getAuthUrl(Constants.INSTAGRAM_SCOPES... scopes) throws URISyntaxException {
        String authUrl = INSTAGRAM_AUTH_URL;

        for (int i = 0; i < scopes.length; i++) {
            authUrl += scopes[i];
            if (i < scopes.length - 1) {
                authUrl += "+";
            }
        }
        URI uri = new URI("https", "api.instagram.com", "/oauth/authorize", authUrl, null);

        return uri.toString();
    }

    public static String getAccessTokenFromUrl(View view, String url) {
        int errorIndex = url.indexOf("?error=");
        if (errorIndex >= 0) {
            Utils.logError(view.getContext(), url.substring(errorIndex + "?error=".length()));
            Utils.showConnectionError(view, view.getResources().getString(R.string.error_ig_login));
            return null;
        }

        int accessTokenIndex = url.lastIndexOf(INSTAGRAM_REDIRECT_URL + "/#access_token=");
        if (accessTokenIndex >= 0) {
            return url.substring(accessTokenIndex + (INSTAGRAM_REDIRECT_URL + "/#access_token=").length());
        }

        return null;
    }

    public static String getCodeFromUrl(View view, String url) {
        int errorIndex = url.indexOf("?error=");
        if (errorIndex >= 0) {
            Utils.logError(view.getContext(), url.substring(errorIndex + "?error=".length()));
            Utils.showConnectionError(view, view.getResources().getString(R.string.error_ig_login));
            return null;
        }

        int codeIndex = url.lastIndexOf(INSTAGRAM_REDIRECT_URL + "/?code=");
        if (codeIndex >= 0) {
            return url.substring(codeIndex + (INSTAGRAM_REDIRECT_URL + "/?code=").length());
        }

        return null;
    }

    public static InstagramAccessToken getAccessTokenByCode(String code) throws IOException {
        InputStreamReader isr = Api.simplePost(INSTAGRAM_REQUEST_ACCESS_TOKEN_URL, INSTAGRAM_CODE_URL + code);
        String stream = Api.getStream(isr);

        InstagramAccessToken accessToken = mapper.readValue(stream, InstagramAccessToken.class);
        if (accessToken.isError()) {
            // error
        }

        return accessToken;
    }

    private static String getMeta(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        JSONObject metaObject = jsonObject.getJSONObject("meta");

        if (Utils.isEmpty(metaObject)) {
            return null;
        }

        return metaObject.toString();
    }
    private static String getData(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        JSONObject dataObject = jsonObject.getJSONObject("data");

        if (Utils.isEmpty(dataObject)) {
            return null;
        }

        return dataObject.toString();
    }

    public static <T extends InstagramMeta> InstagramMeta postDataToClass(Class<T> clazz, String url, String data) throws IOException, JSONException {
        InputStreamReader isr = Api.simplePost(url, data);
        String stream = Api.getStream(isr);

        T response = mapper.readValue(stream, clazz);
        return response;
    }

    public static <T> T getDataToClass(Class<T> clazz, String url) throws IOException, JSONException {
        InputStreamReader isr = Api.simpleGet(url);
        String stream = Api.getStream(isr);

        T response = mapper.readValue(stream, clazz);
        return response;
    }

}
