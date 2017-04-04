package abc.flaq.apps.instastudycategories.utils;

import android.content.Context;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import abc.flaq.apps.instastudycategories.pojo.InstagramOAuthResponse;

import static abc.flaq.apps.instastudycategories.utils.Constants.INSTAGRAM_AUTH_URL;
import static abc.flaq.apps.instastudycategories.utils.Constants.INSTAGRAM_CODE_URL;
import static abc.flaq.apps.instastudycategories.utils.Constants.INSTAGRAM_REDIRECT_URL;
import static abc.flaq.apps.instastudycategories.utils.Constants.INSTAGRAM_REQUEST_ACCESS_TOKEN_URL;
import static abc.flaq.apps.instastudycategories.utils.GeneralUtils.isNotEmpty;

public class InstagramUtils {

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

    public static InstagramOAuthResponse getAccessTokenByCode(String code) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = Api.post(INSTAGRAM_REQUEST_ACCESS_TOKEN_URL, INSTAGRAM_CODE_URL + code);
        String data = Api.getStream(is);

        InstagramOAuthResponse oAuthToken = mapper.readValue(data, InstagramOAuthResponse.class);
        if (oAuthToken.isError()) {
            return oAuthToken;
        }

        oAuthToken = mapper.readValue(data, InstagramOAuthResponse.class);
        return oAuthToken;
    }

}
