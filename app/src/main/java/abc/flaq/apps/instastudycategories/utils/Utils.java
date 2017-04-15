package abc.flaq.apps.instastudycategories.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;

import abc.flaq.apps.instastudycategories.BuildConfig;

import static abc.flaq.apps.instastudycategories.utils.Constants.GRID_MAX_HEIGHT;

public class Utils {

    public static final int LOG_DEBUG = 0;
    public static final int LOG_ERROR = 1;
    public static final int LOG_INFO = 2;

    public static void logDebug(Object activity, String message) {
        // TODO: save to database
        log(LOG_DEBUG, activity, message);
    }
    public static void logError(Object activity, String message) {
        // TODO: save to database
        log(LOG_ERROR, activity, message);
    }
    public static void logInfo(Object activity, String message) {
        if (BuildConfig.IS_DEBUG) {
            log(LOG_INFO, activity, message);
        }
    }
    private static void log(int type, Object activity, String message) {
        String className = (activity instanceof String ?
                (String) activity :
                (isEmpty(activity.getClass()) ? null : activity.getClass().getSimpleName())
        );
        if (isNotEmpty(className)) {
            switch (type) {
                case LOG_DEBUG:
                    Log.d("\t\t" + className, message);
                    break;
                case LOG_ERROR:
                    Log.e("\t\t" + className, message);
                    break;
                case LOG_INFO:
                default:
                    Log.i("\t\t" + className, message);
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

    public static String doForeignId(String id) {
        return "fq" + id;
    }
    public static String undoForeignId(String foreignId) {
        return foreignId.replace("fq", "");
    }

    public static Boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return (list.size() > 0);
    }

    public static int getDrawableByName(Context context, String name) {
        int drawableId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        return drawableId;
    }

    public static void setGridDesign(int position, int size, int maxSize, RelativeLayout layout) {
        // FIXME: sort elements and set different sizes
        int minSize = 10;

        if (size >= maxSize) {
            layout.setMinimumHeight(GRID_MAX_HEIGHT);
        } else if (size > minSize) {
            layout.setMinimumHeight(GRID_MAX_HEIGHT - 50);
        } else {
            layout.setMinimumHeight(GRID_MAX_HEIGHT - 100);
        }

        // Ten colors max
        switch (position % 10) {
            case 0:
                layout.setBackgroundColor(Color.parseColor("#B176B1"));
                break;
            case 1:
                layout.setBackgroundColor(Color.parseColor("#968089"));
                break;
            case 3:
                layout.setBackgroundColor(Color.parseColor("#B176B1"));
                break;
            case 4:
                layout.setBackgroundColor(Color.parseColor("#968089"));
                break;
            case 5:
                layout.setBackgroundColor(Color.parseColor("#B176B1"));
                break;
            case 6:
                layout.setBackgroundColor(Color.parseColor("#968089"));
                break;
            case 7:
                layout.setBackgroundColor(Color.parseColor("#B176B1"));
                break;
            case 8:
                layout.setBackgroundColor(Color.parseColor("#968089"));
                break;
            case 9:
            default:
                layout.setBackgroundColor(Color.parseColor("#F19C7F"));
                break;
        }
    }

    public static String listToString(List<String> list) {
        String string = "[";
        for (int i = 0; i < list.size(); i++) {
            string += "\"";
            string += list.get(i);
            string += "\"";
            if (i < list.size() - 1) {
                string += ",";
            }
        }
        string += "]";
        return string;
    }

    public static void afterError(Context context, String message) {
        Utils.log(LOG_ERROR, context, message);
        showMessage(context, "General error");
    }
    // FIXME: better - snackbar https://lab.getbase.com/introduction-to-coordinator-layout-on-android/
    public static void showMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}
