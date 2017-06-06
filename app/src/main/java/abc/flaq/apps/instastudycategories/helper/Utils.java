package abc.flaq.apps.instastudycategories.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import abc.flaq.apps.instastudycategories.BuildConfig;
import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.general.Session;
import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;
import abc.flaq.apps.instastudycategories.pojo.User;

import static abc.flaq.apps.instastudycategories.helper.Constants.FULL_DATE_FORMAT;
import static abc.flaq.apps.instastudycategories.helper.Constants.INSTAGRAM_PACKAGE;
import static abc.flaq.apps.instastudycategories.helper.Constants.INSTAGRAM_URL;
import static abc.flaq.apps.instastudycategories.helper.Constants.STRINGS_CATEGORY_PREFIX;
import static abc.flaq.apps.instastudycategories.helper.Constants.STRINGS_SUBCATEGORY_PREFIX;

public class Utils {

    private static final Calendar calendar = Calendar.getInstance();

    private static final int LOG_DEBUG = 0;
    private static final int LOG_ERROR = 1;
    private static final int LOG_INFO = 2;

    public static void logDebug(Object activity, String message) {
        if (BuildConfig.IS_DEBUG) {
            log(LOG_DEBUG, activity, message);
        }
    }
    public static void logError(Object activity, String message) {
        // TODO: save to database
        log(LOG_ERROR, activity, message);
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

    public static View findSnackbarView(View view) {
        // Get current view - from farthest
        View snackbarView = view.findViewById(R.id.user_layout);
        if (isEmpty(snackbarView)) {
            snackbarView = view.findViewById(R.id.subcategory_tabs);
            if (isEmpty(snackbarView)) {
                snackbarView = view.findViewById(R.id.category_tabs);
                if (isEmpty(snackbarView)) {
                    snackbarView = view;
                }
            }
        }
        return snackbarView;
    }
    public static void showInfo(View view, String message) {
        Snackbar snackbar = Snackbar.make(findSnackbarView(view), message, Snackbar.LENGTH_LONG);
        TextView snackbarTextView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        if (isNotEmpty(snackbarTextView)) {
            snackbarTextView.setMaxLines(6);
        }
        snackbar.show();
    }
    public static void showQuickInfo(View view, String message) {
        Snackbar snackbar = Snackbar.make(findSnackbarView(view), message, Snackbar.LENGTH_SHORT);
        TextView snackbarTextView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        if (isNotEmpty(snackbarTextView)) {
            snackbarTextView.setMaxLines(6);
        }
        snackbar.show();
    }
    public static void showInfoDismiss(View view, String message) {
        Snackbar snackbar = Snackbar.make(findSnackbarView(view), message, Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(ContextCompat.getColor(view.getContext(), R.color.colorAccent))
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // dismiss
                    }
                });
        TextView snackbarTextView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        if (isNotEmpty(snackbarTextView)) {
            snackbarTextView.setMaxLines(6);
        }
        snackbar.show();
    }
    public static void showError(View view, String message) {
        logError(view.getContext(), message);

        Snackbar snackbar = Snackbar.make(findSnackbarView(view), R.string.error_general, Snackbar.LENGTH_LONG);
        TextView snackbarTextView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        if (isNotEmpty(snackbarTextView)) {
            snackbarTextView.setMaxLines(6);
        }
        snackbar.show();
    }
    public static void showQuickError(View view, String message) {
        logError(view.getContext(), message);

        Snackbar snackbar = Snackbar.make(findSnackbarView(view), R.string.error_general, Snackbar.LENGTH_SHORT);
        TextView snackbarTextView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        if (isNotEmpty(snackbarTextView)) {
            snackbarTextView.setMaxLines(6);
        }
        snackbar.show();
    }
    public static void showErrorDismiss(View view, String message) {
        Snackbar snackbar = Snackbar.make(findSnackbarView(view), message, Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(ContextCompat.getColor(view.getContext(), R.color.colorError))
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // dismiss
                    }
                });
        TextView snackbarTextView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        if (isNotEmpty(snackbarTextView)) {
            snackbarTextView.setMaxLines(6);
        }
        snackbar.show();
    }
    public static void showConnectionError(final View view, String message) {
        logError(view.getContext(), message);

        Snackbar snackbar = Snackbar.make(findSnackbarView(view), message, Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(ContextCompat.getColor(view.getContext(), R.color.colorError))
                .setAction(R.string.next, new View.OnClickListener() {
                    @Override
                    public void onClick(View snackbarView) {
                        showErrorDismiss(view, view.getResources().getString(R.string.error_connection));
                    }
                });
        TextView snackbarTextView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        if (isNotEmpty(snackbarTextView)) {
            snackbarTextView.setMaxLines(6);
        }
        snackbar.show();
    }
    public static void showLoginError(final View view, String message) {
        Snackbar snackbar = Snackbar.make(findSnackbarView(view), message, Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(ContextCompat.getColor(view.getContext(), R.color.colorError))
                .setAction(R.string.next, new View.OnClickListener() {
                    @Override
                    public void onClick(View snackbarView) {
                        showErrorDismiss(view, view.getResources().getString(R.string.error_login_try_again));
                    }
                });
        TextView snackbarTextView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        if (isNotEmpty(snackbarTextView)) {
            snackbarTextView.setMaxLines(6);
        }
        snackbar.show();
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
    public static Intent getInstagramIntent(String username) {
        Uri instagramUri = Uri.parse(INSTAGRAM_URL + "_u/" + username);
        Intent nextIntent = new Intent(Intent.ACTION_VIEW, instagramUri);
        nextIntent.setPackage(INSTAGRAM_PACKAGE);
        return nextIntent;
    }

    public static Date moveDateByDays(Date date, int days) {
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }
    public static String formatDate(Date date, String format) {
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, 2);
        String formattedDate = DateFormat.format(format, calendar.getTime()).toString();
        return formattedDate;
    }
    public static String formatJoinedDate(Date date) {
        String formattedDate = DateFormat.format(FULL_DATE_FORMAT, date).toString();
        return formattedDate;
    }
    public static Date parseStringDate(String date, String format) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        Date parsedDate = dateFormat.parse(date);
        calendar.setTime(parsedDate);
        calendar.add(Calendar.HOUR_OF_DAY, -2);
        return calendar.getTime();
    }
    public static String formatNumber(Integer number) {
        if (number > 999) {
            String stringNumber = number.toString();
            int digit = ((number % 1000) / 100);
            String symbol = (Locale.getDefault().getLanguage().equals("en") ? "." : ",");
            return (stringNumber.substring(0, stringNumber.length() - 3) + symbol + digit + "k");
        }
        return number.toString();
    }

    private static String getStringByName(Context context, String name) {
        int stringId = context.getResources().getIdentifier(name, "string", context.getPackageName());
        if (stringId == 0 ||
                (isNumeric(name) && stringId == Integer.parseInt(name))) {
            logDebug(context, "No string found for: " + name);
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
            logDebug(context, "No drawable found for: " + name);
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

    public static List<String> getCategoriesNames(Context context) {
        List<String> names = new ArrayList<>();
        if (isNotEmpty(Session.getInstance().getCategories())) {
            for (Category category : Session.getInstance().getCategories()) {
                names.add(category.getName().trim().toLowerCase());
                names.add(getStringByCategoryName(context, category.getName()).trim().toLowerCase());
            }
        }
        return names;
    }
    public static List<String> getSubcategoriesNames(Context context, String foreignCategoryId) {
        List<String> names = new ArrayList<>();
        if (isNotEmpty(Session.getInstance().getSubcategories(foreignCategoryId))) {
            for (Subcategory subcategory : Session.getInstance().getSubcategories(foreignCategoryId)) {
                names.add(subcategory.getName().trim().toLowerCase());
                names.add(getStringBySubcategoryName(context, subcategory.getName()).trim().toLowerCase());
            }
        }
        return names;
    }

    public static DisplayMetrics getScreenMetrics(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }
    public static int getScreenHeight(Activity activity) {
        DisplayMetrics metrics = getScreenMetrics(activity);
        int screenHeight = metrics.heightPixels;
        return screenHeight;
    }
    public static int getScreenWidth(Activity activity) {
        DisplayMetrics metrics = getScreenMetrics(activity);
        int screenWidth = metrics.widthPixels;
        return screenWidth;
    }

    public static String listToString(List list) {
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

    private static void swapNumbers(List<User> list, int i, int j) {
        User temp;
        temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
    public static void sortByFollowers(List<User> users, Boolean rev) {
        int length = users.size();
        int k;
        for (int i = length; i >= 0; i--) {
            for (int j = 0; j < length - 1; j++) {
                k = j + 1;
                if (rev) {
                    if (users.get(j).getFollowers() > users.get(k).getFollowers()) {
                        swapNumbers(users, j, k);
                    }
                } else {
                    if (users.get(j).getFollowers() < users.get(k).getFollowers()) {
                        swapNumbers(users, j, k);
                    }
                }
            }
        }
    }
    public static void sortByJoinedDate(List<User> users, Boolean rev) {
        Collections.sort(users);
        if (rev) {
            Collections.reverse(users);
        }
    }
    public static void sortAlphabetically(List<User> users, Boolean rev) {
        int length = users.size();
        int k;
        for (int i = length; i >= 0; i--) {
            for (int j = 0; j < length - 1; j++) {
                k = j + 1;
                if (rev) {
                    if (users.get(j).getUsername().compareTo(users.get(k).getUsername()) < 0) {
                        swapNumbers(users, j, k);
                    }
                } else {
                    if (users.get(j).getUsername().compareTo(users.get(k).getUsername()) > 0) {
                        swapNumbers(users, j, k);
                    }
                }
            }
        }
    }

    public static String simplifyCharacters(String characters) {
        return characters;
        /*String noExtraCharacters = Normalizer
                .normalize(characters, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noExtraCharacters;*/
    }

}
