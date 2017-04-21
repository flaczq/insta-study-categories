package abc.flaq.apps.instastudycategories.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import abc.flaq.apps.instastudycategories.BuildConfig;
import abc.flaq.apps.instastudycategories.utils.Session;
import abc.flaq.apps.instastudycategories.utils.Utils;

import static abc.flaq.apps.instastudycategories.utils.Constants.SETTINGS_NAME;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.logDebug(this, "Build data: " +
                BuildConfig.FLAVOR_FULLNAME +
                "/" + BuildConfig.BUILD_TYPE + " " +
                BuildConfig.VERSION_NAME
        );

        if (BuildConfig.IS_DEBUG) {
            Utils.setLocale(this, "pl");
        }

        SharedPreferences settings = getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);
        Session.getInstance().setSettings(settings);

        Intent intent = new Intent(this, CategoryActivity.class);
        startActivity(intent);
        finish();
    }

}
