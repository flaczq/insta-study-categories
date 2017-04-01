package abc.flaq.apps.instastudycategories;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;

import static abc.flaq.apps.instastudycategories.Constants.GRID_MAX_HEIGHT;

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

    public static void afterError(Context context) {
        Toast.makeText(context, "Shit happend", Toast.LENGTH_LONG).show();
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

}
