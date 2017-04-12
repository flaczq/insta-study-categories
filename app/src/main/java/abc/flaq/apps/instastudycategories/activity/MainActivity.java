package abc.flaq.apps.instastudycategories.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import abc.flaq.apps.instastudycategories.utils.Session;

import static abc.flaq.apps.instastudycategories.utils.Constants.SETTINGS_NAME;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);
        Session.getInstance().setSettings(settings);

        Intent intent = new Intent(this, CategoryActivity.class);
        startActivity(intent);
        finish();
    }

}
