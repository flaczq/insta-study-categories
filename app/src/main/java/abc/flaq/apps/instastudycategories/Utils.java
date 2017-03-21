package abc.flaq.apps.instastudycategories;

import android.app.Activity;
import android.util.Log;

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

}
