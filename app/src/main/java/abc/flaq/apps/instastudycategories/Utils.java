package abc.flaq.apps.instastudycategories;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import static abc.flaq.apps.instastudycategories.Constants.GRID_MAX_HEIGHT;

public class Utils {

    public static final int LOG_DEBUG = 0;
    public static final int LOG_ERROR = 1;
    public static final int LOG_INFO = 2;

    public static void log(int type, Activity activity, String msg) {
        if (isNotEmpty(activity.getClass())) {
            switch (type) {
                case LOG_DEBUG:
                    Log.d("\t\t" + activity.getClass().getSimpleName(), msg);
                    break;
                case LOG_ERROR:
                    Log.e("\t\t" + activity.getClass().getSimpleName(), msg);
                    break;
                case LOG_INFO:
                default:
                    Log.i("\t\t" + activity.getClass().getSimpleName(), msg);
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
