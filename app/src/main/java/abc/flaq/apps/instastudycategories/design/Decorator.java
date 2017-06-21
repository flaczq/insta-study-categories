package abc.flaq.apps.instastudycategories.design;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.general.Session;

import static abc.flaq.apps.instastudycategories.helper.Utils.isNotEmpty;
import static android.util.TypedValue.COMPLEX_UNIT_SP;

public class Decorator {

    public static void setBackNavigation(AppCompatActivity activity) {
        ActionBar actionBar = activity.getActionBar();
        if (isNotEmpty(actionBar)) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        android.support.v7.app.ActionBar supportActionBar = activity.getSupportActionBar();
        if (isNotEmpty(supportActionBar)) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    public static void setActionBarTitle(AppCompatActivity activity, String title, String subtitle) {
        ActionBar actionBar = activity.getActionBar();
        String formattedTitle = (title.substring(0, 1).toUpperCase() + title.substring(1));
        if (isNotEmpty(actionBar)) {
            actionBar.setTitle(formattedTitle);
            actionBar.setSubtitle(subtitle);
        }
        android.support.v7.app.ActionBar supportActionBar = activity.getSupportActionBar();
        if (isNotEmpty(supportActionBar)) {
            supportActionBar.setTitle(formattedTitle);
            supportActionBar.setSubtitle(subtitle);
        }
    }
    public static void removeActionBarShadow(AppCompatActivity activity) {
        ActionBar actionBar = activity.getActionBar();
        if (isNotEmpty(actionBar) && Build.VERSION.SDK_INT >= 21) {
            actionBar.setElevation(0);
        }
        android.support.v7.app.ActionBar supportActionBar = activity.getSupportActionBar();
        if (isNotEmpty(supportActionBar)) {
            supportActionBar.setElevation(0);
        }
    }
    public static void setStatusBarColor(AppCompatActivity activity) {
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= 21) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
        }
    }
    public static void setLocale(Context context, String language) {
        final Locale locale = new Locale(language);
        Locale.setDefault(locale);
        final Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
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
    public static void setGridHeight(int size, ImageView imageView) {
        int height = Session.getInstance().getMaxGridSize();
        // TODO SOMEDAY: how to set imageview height properly?
    }

    public static void fitFont(TextView textView) {
        String text = textView.getText().toString();
        if (text.length() >= 13) {
            textView.setTextSize(COMPLEX_UNIT_SP, 19);
        } else if (text.length() >= 10) {
            textView.setTextSize(COMPLEX_UNIT_SP, 22);
        } else {
            textView.setTextSize(COMPLEX_UNIT_SP, 24);
        }
    }

}
