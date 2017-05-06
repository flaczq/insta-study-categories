package abc.flaq.apps.instastudycategories.utils;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.Normalizer;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import abc.flaq.apps.instastudycategories.BuildConfig;
import abc.flaq.apps.instastudycategories.R;

import static abc.flaq.apps.instastudycategories.utils.Constants.DATE_FORMAT;
import static abc.flaq.apps.instastudycategories.utils.Constants.GRID_HEIGHT_DIFF;
import static abc.flaq.apps.instastudycategories.utils.Constants.GRID_MAX_HEIGHT;
import static abc.flaq.apps.instastudycategories.utils.Constants.STRINGS_CATEGORY_PREFIX;
import static abc.flaq.apps.instastudycategories.utils.Constants.STRINGS_SUBCATEGORY_PREFIX;

public class Utils {

    private static final Calendar calendar = Calendar.getInstance();

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

    public static void showInfo(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }
    public static void showQuickInfo(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
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
        log(LOG_ERROR, view.getContext(), message);
        Snackbar.make(view, "General error", Snackbar.LENGTH_LONG).show();
    }
    public static void showQuickError(View view, String message) {
        log(LOG_ERROR, view.getContext(), message);
        Snackbar.make(view, "General error", Snackbar.LENGTH_SHORT).show();
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
    public static void showConnectionError(final View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(ContextCompat.getColor(view.getContext(), R.color.colorError))
                .setAction("DALEJ", new View.OnClickListener() {
                    @Override
                    public void onClick(View snackbarView) {
                        showErrorDismiss(view, "Sprawdź połączenie z Internetem i spróbuj ponownie");
                    }
                }).show();
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
    public static boolean isNumeric(String element) {
        if (isEmpty(element)) {
            return false;
        }
        final int sz = element.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(element.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    public static boolean isNotNumeric(String element) {
        return !isNumeric(element);
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
        Boolean isAvailable = (list.size() > 0);
        return isAvailable;
    }

    public static String formatDate(Date date) {
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, 2);
        String formattedDate = DateFormat.format(DATE_FORMAT, calendar.getTime()).toString();
        return formattedDate;
    }

    private static String getStringByName(Context context, String name) {
        int stringId = context.getResources().getIdentifier(name, "string", context.getPackageName());
        if (stringId == 0 ||
                (isNumeric(name) && stringId == Integer.parseInt(name))) {
            logDebug(context, "Nie znaleziono tłumaczenia dla nazwy: " + name);
            return "";
        }
        return context.getString(stringId);
    }
    public static String getStringByCategoryName(Context context, String name) {
        String result = getStringByName(context, STRINGS_CATEGORY_PREFIX + name);
        if (isEmpty(result)) {
            return name;
        }
        return result;
    }
    public static String getStringBySubcategoryName(Context context, String name) {
        String result = getStringByName(context, STRINGS_SUBCATEGORY_PREFIX + name);
        if (isEmpty(result)) {
            return name;
        }
        return result;
    }
    private static Drawable getDrawableByName(Context context, String name) {
        int drawableId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        if (drawableId == 0 ||
                (isNumeric(name) && drawableId == Integer.parseInt(name))) {
            logDebug(context, "Nie znaleziono obrazu dla nazwy: " + name);
            return null;
        }
        return ResourcesCompat.getDrawable(context.getResources(), drawableId, null);
    }
    public static Drawable getCategoryDrawable(Context context, String name) {
        return getDrawableByName(context, name);
    }
    public static Drawable getSubcategoryDrawable(Context context, String name) {
        // Remove subcategory prefix (hs_)
        int index = name.indexOf("_");
        if (index > 0) {
            name = name.substring(index + 1);
        }
        return getDrawableByName(context, name);
    }

    private static void setColorByPosition(int position, TextView textView) {
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
    public static void setCategoryGridDesign(int position, int size, int maxSize, TextView textView) {
        // FIXME: sort elements and set different sizes
        int minSize = 10;

        if (size >= maxSize) {
            textView.setMinimumHeight(GRID_MAX_HEIGHT);
            textView.setHeight(GRID_MAX_HEIGHT);
            textView.setMaxHeight(GRID_MAX_HEIGHT);
        } else if (size > minSize) {
            textView.setMinimumHeight(GRID_MAX_HEIGHT - GRID_HEIGHT_DIFF);
            textView.setHeight(GRID_MAX_HEIGHT - GRID_HEIGHT_DIFF);
            textView.setMaxHeight(GRID_MAX_HEIGHT - GRID_HEIGHT_DIFF);
        } else {
            textView.setMinimumHeight(GRID_MAX_HEIGHT - 2*GRID_HEIGHT_DIFF);
            textView.setHeight(GRID_MAX_HEIGHT - 2*GRID_HEIGHT_DIFF);
            textView.setMaxHeight(GRID_MAX_HEIGHT - 2*GRID_HEIGHT_DIFF);
        }

        setColorByPosition(position, textView);
    }
    public static void setSubcategoryGridDesign(int size, ImageView imageView) {
        // FIXME: sort elements and set different sizes
        if (size >= 20) {
            imageView.setMinimumHeight(GRID_MAX_HEIGHT);
            imageView.setMaxHeight(GRID_MAX_HEIGHT);
        } else if (size > 10) {
            imageView.setMinimumHeight(GRID_MAX_HEIGHT - GRID_HEIGHT_DIFF);
            imageView.setMaxHeight(GRID_MAX_HEIGHT - GRID_HEIGHT_DIFF);
        } else {
            imageView.setMinimumHeight(GRID_MAX_HEIGHT - 2*GRID_HEIGHT_DIFF);
            imageView.setMaxHeight(GRID_MAX_HEIGHT - 2*GRID_HEIGHT_DIFF);
        }
    }

    public static void setActionBarTitle(AppCompatActivity activity, String title) {
        ActionBar actionBar = activity.getActionBar();
        String formattedTitle = (title.substring(0, 1).toUpperCase() + title.substring(1));
        if (isNotEmpty(actionBar)) {
            actionBar.setTitle(formattedTitle);
        }
        android.support.v7.app.ActionBar supportActionBar = activity.getSupportActionBar();
        if (isNotEmpty(supportActionBar)) {
            supportActionBar.setTitle(formattedTitle);
        }
    }
    public static void setLocale(Context context, String language) {
        final Locale locale = new Locale(language);
        Locale.setDefault(locale);
        final Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }
    public static void removeBarShadow(AppCompatActivity activity) {
        ActionBar actionBar = activity.getActionBar();
        if (isNotEmpty(actionBar) && Build.VERSION.SDK_INT >= 21) {
            actionBar.setElevation(0);
        }
        android.support.v7.app.ActionBar supportActionBar = activity.getSupportActionBar();
        if (isNotEmpty(supportActionBar)) {
            supportActionBar.setElevation(0);
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

    public static String simplifyCharacters(String characters) {
        String noExtraCharacters = Normalizer
                .normalize(characters, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noExtraCharacters;
    }

}
