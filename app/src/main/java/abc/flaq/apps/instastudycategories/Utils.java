package abc.flaq.apps.instastudycategories;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static abc.flaq.apps.instastudycategories.Constants.GRID_MAX_HEIGHT;
import static abc.flaq.apps.instastudycategories.Constants.INSTAGRAM_ACCESS_TOKEN_URL;
import static abc.flaq.apps.instastudycategories.Constants.INSTAGRAM_AUTH_AUTHORITY;
import static abc.flaq.apps.instastudycategories.Constants.INSTAGRAM_AUTH_PATH;
import static abc.flaq.apps.instastudycategories.Constants.INSTAGRAM_AUTH_REDIRECT_URI;
import static abc.flaq.apps.instastudycategories.Constants.INSTAGRAM_AUTH_RESPONSE_TYPE_CLIENT_ID;
import static abc.flaq.apps.instastudycategories.Constants.INSTAGRAM_AUTH_SCHEME;
import static abc.flaq.apps.instastudycategories.Constants.INSTAGRAM_AUTH_SCOPE;
import static abc.flaq.apps.instastudycategories.Constants.INSTAGRAM_CLIENT_ID;
import static abc.flaq.apps.instastudycategories.Constants.INSTAGRAM_CODE_URL;
import static abc.flaq.apps.instastudycategories.Constants.INSTAGRAM_REDIRECT_URL;

public class Utils {

    public static final int LOG_DEBUG = 0;
    public static final int LOG_ERROR = 1;
    public static final int LOG_INFO = 2;

    public static void log(int type, Object activity, String msg) {
        String className = (activity instanceof String ?
                (String) activity :
                (isEmpty(activity.getClass()) ? null : activity.getClass().getSimpleName())
        );
        if (isNotEmpty(className)) {
            switch (type) {
                case LOG_DEBUG:
                    Log.d("\t\t" + className, msg);
                    break;
                case LOG_ERROR:
                    Log.e("\t\t" + className, msg);
                    break;
                case LOG_INFO:
                default:
                    Log.i("\t\t" + className, msg);
                    break;
            }
        }
    }

    public static <T> boolean isEmpty(T element) {
        if (element == null || "".equals(element) || "".equals(element.toString())) {
            return true;
        }
        return false;
    }
    public static <T> boolean isNotEmpty(T element) {
        return !isEmpty(element);
    }

    public static String getInstagramAuthUrl(Constants.INSTAGRAM_SCOPES... scopes) throws URISyntaxException {
        String authUrl = (
                INSTAGRAM_AUTH_RESPONSE_TYPE_CLIENT_ID +
                INSTAGRAM_CLIENT_ID +
                INSTAGRAM_AUTH_REDIRECT_URI +
                INSTAGRAM_REDIRECT_URL +
                INSTAGRAM_AUTH_SCOPE
        );
        for (int i = 0; i < scopes.length; i++) {
            authUrl += scopes[i];
            if (i < scopes.length - 1) {
                authUrl += "+";
            }
        }
        return new URI(INSTAGRAM_AUTH_SCHEME, INSTAGRAM_AUTH_AUTHORITY, INSTAGRAM_AUTH_PATH, authUrl, null).toString();
    }
    public static String getInstagramAccessToken(String url) {
        if (isNotEmpty(url)) {
            // TODO: check for error
            String accessToken = url.replace(INSTAGRAM_REDIRECT_URL + INSTAGRAM_ACCESS_TOKEN_URL, "");
            return accessToken;
        }
        return null;
    }
    public static String getInstagramCode(String url) {
        if (isNotEmpty(url)) {
            // TODO: check for error
            String code = url.replace(INSTAGRAM_REDIRECT_URL + INSTAGRAM_CODE_URL, "");
            return code;
        }
        return null;
    }

    public static int getDrawableByName(Context context, String name) {
        int drawableId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        return drawableId;
    }

    public static void afterError(Context context) {
        Toast.makeText(context, "Shit happens", Toast.LENGTH_LONG).show();
    }

    public static Boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return (list.size() > 0);
    }

    public static void setGridDesign(int position, int size, int maxSize, RelativeLayout layout) {
        int minSize = 10;

        if (size >= maxSize) {
            layout.setMinimumHeight(GRID_MAX_HEIGHT);
        } else if (size > minSize) {
            layout.setMinimumHeight(GRID_MAX_HEIGHT - 30);
        } else {
            layout.setMinimumHeight(GRID_MAX_HEIGHT - 60);
        }

        // Ten colors max
        switch (position % 10) {
            case 0:
                layout.setBackgroundColor(Color.parseColor("#B176B1"));
                break;
            case 1:
                layout.setBackgroundColor(Color.parseColor("#968089"));
                break;
            case 2:
            default:
                layout.setBackgroundColor(Color.parseColor("#F19C7F"));
                break;
        }
    }

    // FIXME: better!
    public static void showMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}
