package abc.flaq.apps.instastudycategories.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import abc.flaq.apps.instastudycategories.BuildConfig;
import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.general.Session;
import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.pojo.EveObject;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;
import abc.flaq.apps.instastudycategories.pojo.User;

import static abc.flaq.apps.instastudycategories.helper.Constants.EMAIL_NAME;
import static abc.flaq.apps.instastudycategories.helper.Constants.FULL_DATE_FORMAT;
import static abc.flaq.apps.instastudycategories.helper.Constants.INSTAGRAM_PACKAGE;
import static abc.flaq.apps.instastudycategories.helper.Constants.INSTAGRAM_URL;
import static abc.flaq.apps.instastudycategories.helper.Constants.STRINGS_CATEGORY_PREFIX;
import static abc.flaq.apps.instastudycategories.helper.Constants.STRINGS_SUBCATEGORY_PREFIX;

public class Utils {

    private static final Calendar calendar = Calendar.getInstance();
    private static final Handler handler = new Handler();

    private static final int LOG_DEBUG = 0;
    private static final int LOG_ERROR = 1;
    private static final int LOG_INFO = 2;

    public static void logDebug(Object activity, String message) {
        if (BuildConfig.IS_DEBUG) {
            log(LOG_DEBUG, activity, message);
        }
    }
    public static void logError(Object activity, String message) {
        // TODO SOMEDAY: save to database
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
        final Snackbar snackbar = Snackbar.make(findSnackbarView(view), message, Snackbar.LENGTH_LONG);
        TextView snackbarTextView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        if (isNotEmpty(snackbarTextView)) {
            snackbarTextView.setMaxLines(6);
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                snackbar.show();
            }
        }, 150);
    }
    public static void showQuickInfo(View view, String message) {
        final Snackbar snackbar = Snackbar.make(findSnackbarView(view), message, Snackbar.LENGTH_SHORT);
        TextView snackbarTextView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        if (isNotEmpty(snackbarTextView)) {
            snackbarTextView.setMaxLines(6);
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                snackbar.show();
            }
        }, 150);
    }
    public static void showInfoDismiss(View view, String message) {
        final Snackbar snackbar = Snackbar.make(findSnackbarView(view), message, Snackbar.LENGTH_INDEFINITE)
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
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                snackbar.show();
            }
        }, 150);
    }
    public static void showError(View view, String message) {
        logError(view.getContext(), message);

        final Snackbar snackbar = Snackbar.make(findSnackbarView(view), R.string.error_general, Snackbar.LENGTH_LONG);
        TextView snackbarTextView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        if (isNotEmpty(snackbarTextView)) {
            snackbarTextView.setMaxLines(6);
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                snackbar.show();
            }
        }, 150);
    }
    public static void showQuickError(View view, String message) {
        logError(view.getContext(), message);

        final Snackbar snackbar = Snackbar.make(findSnackbarView(view), R.string.error_general, Snackbar.LENGTH_SHORT);
        TextView snackbarTextView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        if (isNotEmpty(snackbarTextView)) {
            snackbarTextView.setMaxLines(6);
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                snackbar.show();
            }
        }, 150);
    }
    public static void showErrorDismiss(View view, String message) {
        final Snackbar snackbar = Snackbar.make(findSnackbarView(view), message, Snackbar.LENGTH_INDEFINITE)
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
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                snackbar.show();
            }
        }, 150);
    }
    public static void showConnectionError(final View view, String logMessage, String showMessage) {
        logError(view.getContext(), logMessage);

        final Snackbar snackbar = Snackbar.make(findSnackbarView(view), showMessage, Snackbar.LENGTH_INDEFINITE)
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
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                snackbar.show();
            }
        }, 150);
    }
    public static void showLoginError(final View view, String message) {
        final Snackbar snackbar = Snackbar.make(findSnackbarView(view), message, Snackbar.LENGTH_INDEFINITE)
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
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                snackbar.show();
            }
        }, 150);
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
        Intent instagramIntent = new Intent(Intent.ACTION_VIEW, instagramUri);
        instagramIntent.setPackage(INSTAGRAM_PACKAGE);
        return instagramIntent;
    }
    public static Intent getEmailIntent(String subject) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { EMAIL_NAME });
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        return emailIntent;
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
    public static String getCategoryString(Context context, String name) {
        String result = getStringByName(context, STRINGS_CATEGORY_PREFIX + name);
        if (isEmpty(result)) {
            return name;
        }
        return result;
    }
    public static String getSubcategoryString(Context context, String name) {
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
                names.add(getCategoryString(context, category.getName()).trim().toLowerCase());
            }
        }
        return names;
    }
    public static List<String> getSubcategoriesNames(Context context, String foreignCategoryId) {
        List<String> names = new ArrayList<>();
        if (isNotEmpty(Session.getInstance().getSubcategories(foreignCategoryId))) {
            for (Subcategory subcategory : Session.getInstance().getSubcategories(foreignCategoryId)) {
                names.add(subcategory.getName().trim().toLowerCase());
                names.add(getSubcategoryString(context, subcategory.getName()).trim().toLowerCase());
            }
        }
        return names;
    }

    public static Map<String, List<Object>> getSubcategoriesResources(Context context, List<Subcategory> subcategories) {
        HashMap<String, List<Object>> subcategoriesResources = new HashMap<>();
        ArrayList<Object> namesResources = new ArrayList<>();
        ArrayList<Object> photosResources = new ArrayList<>();
        for (Subcategory subcategory : subcategories) {
            switch (subcategory.getName()) {
                case "c_doctor":
                    namesResources.add(context.getString(R.string.subcategory_name_c_doctor));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.doctor));
                    break;
                case "c_dentist":
                    namesResources.add(context.getString(R.string.subcategory_name_c_dentist));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.dentist));
                    break;
                case "c_pharmacy":
                    namesResources.add(context.getString(R.string.subcategory_name_c_pharmacy));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.pharmacy));
                    break;
                case "c_midwife":
                    namesResources.add(context.getString(R.string.subcategory_name_c_midwife));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.midwife));
                    break;
                case "c_nurse":
                    namesResources.add(context.getString(R.string.subcategory_name_c_nurse));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.nurse));
                    break;
                case "c_veterinary":
                    namesResources.add(context.getString(R.string.subcategory_name_c_veterinary));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.veterinary));
                    break;
                case "c_physiotherapy":
                    namesResources.add(context.getString(R.string.subcategory_name_c_physiotherapy));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.physiotherapy));
                    break;
                case "c_dietetics":
                    namesResources.add(context.getString(R.string.subcategory_name_c_dietetics));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.dietetics));
                    break;
                case "c_psychology":
                    namesResources.add(context.getString(R.string.subcategory_name_c_psychology));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.psychology));
                    break;
                case "c_cosmetology":
                    namesResources.add(context.getString(R.string.subcategory_name_c_cosmetology));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.cosmetology));
                    break;
                case "c_emergency":
                    namesResources.add(context.getString(R.string.subcategory_name_c_emergency));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.emergency));
                    break;
                case "c_law":
                    namesResources.add(context.getString(R.string.subcategory_name_c_law));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.law));
                    break;
                case "c_philology":
                    namesResources.add(context.getString(R.string.subcategory_name_c_philology));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.philology));
                    break;
                case "sc_biology":
                    namesResources.add(context.getString(R.string.subcategory_name_sc_biology));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.biology));
                    break;
                case "sc_chemistry":
                    namesResources.add(context.getString(R.string.subcategory_name_sc_chemistry));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.chemistry));
                    break;
                case "sc_physics":
                    namesResources.add(context.getString(R.string.subcategory_name_sc_physics));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.physics));
                    break;
                case "sc_math":
                    namesResources.add(context.getString(R.string.subcategory_name_sc_math));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.math));
                    break;
                case "sc_geography":
                    namesResources.add(context.getString(R.string.subcategory_name_sc_geography));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.geography));
                    break;
                case "sc_social":
                    namesResources.add(context.getString(R.string.subcategory_name_sc_social));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.social));
                    break;
                case "sc_polish":
                    namesResources.add(context.getString(R.string.subcategory_name_sc_polish));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.polish));
                    break;
                case "sc_english":
                    namesResources.add(context.getString(R.string.subcategory_name_sc_english));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.english));
                    break;
                case "hs_bio_chem":
                    namesResources.add(context.getString(R.string.subcategory_name_hs_bio_chem));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.bio_chem));
                    break;
                case "hs_hum":
                    namesResources.add(context.getString(R.string.subcategory_name_hs_hum));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.hum));
                    break;
                case "hs_mat_phy":
                    namesResources.add(context.getString(R.string.subcategory_name_hs_mat_phy));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.mat_phy));
                    break;
                case "hs_mat_geo":
                    namesResources.add(context.getString(R.string.subcategory_name_hs_mat_geo));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.mat_geo));
                    break;
                case "hs_lang":
                    namesResources.add(context.getString(R.string.subcategory_name_hs_lang));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.lang));
                    break;
                case "w_doctor":
                    namesResources.add(context.getString(R.string.subcategory_name_w_doctor));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.doctor));
                    break;
                case "w_dentist":
                    namesResources.add(context.getString(R.string.subcategory_name_w_dentist));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.dentist));
                    break;
                case "w_pharmacy":
                    namesResources.add(context.getString(R.string.subcategory_name_w_pharmacy));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.pharmacy));
                    break;
                case "w_psychology":
                    namesResources.add(context.getString(R.string.subcategory_name_w_psychology));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.psychology));
                    break;
                case "w_nurse":
                    namesResources.add(context.getString(R.string.subcategory_name_w_nurse));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.nurse));
                    break;
                case "w_midwife":
                    namesResources.add(context.getString(R.string.subcategory_name_w_midwife));
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.midwife));
                    break;
                default:
                    logDebug(context, "No string and drawable found for: " + subcategory.getName());
                    namesResources.add(subcategory.getName());
                    photosResources.add(ContextCompat.getDrawable(context, R.drawable.placeholder_category));
                    break;
            }
        }
        subcategoriesResources.put("names", namesResources);
        subcategoriesResources.put("photos", photosResources);
        return subcategoriesResources;
    }
    public static Map<String, List<Object>> getCategoriesResources(Context context, List<EveObject> categories) {
        HashMap<String, List<Object>> categoriesResources = new HashMap<>();
        ArrayList<Object> namesResources = new ArrayList<>();
        ArrayList<Object> photosResources = new ArrayList<>();
        for (EveObject eveObject : categories) {
            if (eveObject instanceof Category) {
                Category category = (Category) eveObject;
                switch (category.getName()) {
                    case "all":
                        namesResources.add(context.getString(R.string.category_name_all));
                        photosResources.add(ContextCompat.getDrawable(context, R.drawable.all));
                        break;
                    case "elementary_school":
                        namesResources.add(context.getString(R.string.category_name_elementary_school));
                        photosResources.add(ContextCompat.getDrawable(context, R.drawable.elementary_school));
                        break;
                    case "junior_high_school":
                        namesResources.add(context.getString(R.string.category_name_junior_high_school));
                        photosResources.add(ContextCompat.getDrawable(context, R.drawable.junior_high_school));
                        break;
                    case "high_school":
                        namesResources.add(context.getString(R.string.category_name_high_school));
                        photosResources.add(ContextCompat.getDrawable(context, R.drawable.high_school));
                        break;
                    case "school_certificate":
                        namesResources.add(context.getString(R.string.category_name_school_certificate));
                        photosResources.add(ContextCompat.getDrawable(context, R.drawable.school_certificate));
                        break;
                    case "college":
                        namesResources.add(context.getString(R.string.category_name_college));
                        photosResources.add(ContextCompat.getDrawable(context, R.drawable.college));
                        break;
                    case "work":
                        namesResources.add(context.getString(R.string.category_name_work));
                        photosResources.add(ContextCompat.getDrawable(context, R.drawable.work));
                        break;
                    default:
                        logDebug(context, "No string and drawable found for: " + category.getName());
                        namesResources.add(category.getName());
                        photosResources.add(ContextCompat.getDrawable(context, R.drawable.placeholder_category));
                        break;
                }
            } else {
                namesResources.add("");
                photosResources.add("");
            }
        }
        categoriesResources.put("names", namesResources);
        categoriesResources.put("photos", photosResources);
        return categoriesResources;
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
