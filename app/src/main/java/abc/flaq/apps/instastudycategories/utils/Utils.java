package abc.flaq.apps.instastudycategories.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import abc.flaq.apps.instastudycategories.BuildConfig;
import abc.flaq.apps.instastudycategories.R;

import static abc.flaq.apps.instastudycategories.utils.Constants.DATE_FORMAT;
import static abc.flaq.apps.instastudycategories.utils.Constants.GRID_HEIGHT_DIFF;
import static abc.flaq.apps.instastudycategories.utils.Constants.GRID_MAX_HEIGHT;

public class Utils {

    private static final int LOG_DEBUG = 0;
    private static final int LOG_ERROR = 1;
    private static final int LOG_INFO = 2;

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

    public static String formatDate(Date date) {
        String formattedDate = DateFormat.format(DATE_FORMAT, date).toString();
        return formattedDate;
    }

    public static String getStringByName(Context context, String name) {
        int nameId = context.getResources().getIdentifier(name, "string", context.getPackageName());
        return context.getString(nameId);
    }

    public static int getDrawableByName(Context context, String name) {
        int drawableId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        return drawableId;
    }

    public static void setCategoryGridDesign(int position, int size, int maxSize, TextView textView) {
        // FIXME: sort elements and set different sizes
        int minSize = 10;

        if (size >= maxSize) {
            textView.setHeight(GRID_MAX_HEIGHT);
        } else if (size > minSize) {
            textView.setHeight(GRID_MAX_HEIGHT - GRID_HEIGHT_DIFF);
        } else {
            textView.setHeight(GRID_MAX_HEIGHT - 2*GRID_HEIGHT_DIFF);
        }

        // Max ten colors
        switch (position % 10) {
            case 0:
                textView.setBackgroundColor(ContextCompat.getColor(textView.getContext(), R.color.colorCategoryBlue));
                break;
            case 1:
                textView.setBackgroundColor(ContextCompat.getColor(textView.getContext(), R.color.colorCategoryOrange));
                break;
            case 2:
                textView.setBackgroundColor(ContextCompat.getColor(textView.getContext(), R.color.colorCategoryPink));
                break;
            case 3:
                textView.setBackgroundColor(ContextCompat.getColor(textView.getContext(), R.color.colorCategoryPurple));
                break;
            case 4:
                textView.setBackgroundColor(ContextCompat.getColor(textView.getContext(), R.color.colorCategoryBlueLight));
                break;
            case 5:
                textView.setBackgroundColor(ContextCompat.getColor(textView.getContext(), R.color.colorCategoryRed));
                break;
            case 6:
                textView.setBackgroundColor(ContextCompat.getColor(textView.getContext(), R.color.colorCategoryGreenLight));
                break;
            case 7:
                textView.setBackgroundColor(ContextCompat.getColor(textView.getContext(), R.color.colorCategoryCreamy));
                break;
            case 8:
                textView.setBackgroundColor(ContextCompat.getColor(textView.getContext(), R.color.colorCategoryGreen));
                break;
            case 9:
            default:
                textView.setBackgroundColor(ContextCompat.getColor(textView.getContext(), R.color.colorCategoryGray));
                break;
        }
    }
    public static void setSubcategoryGridDesign(int size, int maxSize, ImageView imageView) {
        // FIXME: sort elements and set different sizes
        int minSize = 10;

        if (size >= maxSize) {
            imageView.setMaxHeight(GRID_MAX_HEIGHT);
        } else if (size > minSize) {
            imageView.setMaxHeight(GRID_MAX_HEIGHT - GRID_HEIGHT_DIFF);
        } else {
            imageView.setMaxHeight(GRID_MAX_HEIGHT - 2*GRID_HEIGHT_DIFF);
        }
    }

    public static void setLocale(Context context, String language) {
        final Locale locale = new Locale(language);
        Locale.setDefault(locale);
        final Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
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

    public static void showInfo(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }
    public static void showInfoDismiss(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(ContextCompat.getColor(view.getContext(), R.color.colorAccent))
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // dismiss
                    }
                }).show();
    }
    public static void showError(View view, String message) {
        Utils.log(LOG_ERROR, view.getContext(), message);
        Snackbar.make(view, "General error", Snackbar.LENGTH_LONG).show();
    }
    public static void showErrorDismiss(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(ContextCompat.getColor(view.getContext(), R.color.colorError))
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // dismiss
                    }
                }).show();
    }

}
