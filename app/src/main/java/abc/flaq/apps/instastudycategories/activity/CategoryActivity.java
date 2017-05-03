package abc.flaq.apps.instastudycategories.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.adapter.TabAdapter;
import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.utils.Api;
import abc.flaq.apps.instastudycategories.utils.Session;
import abc.flaq.apps.instastudycategories.utils.Utils;

import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_LIST;

public class CategoryActivity extends SessionActivity {

    private AppCompatActivity clazz = this;
    private ArrayList<Category> categories;
    private ViewPager pager;
    private TabLayout tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {     // FIXME: podnieść widok gdy wyświetla się snackbar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_container);

        pager = (ViewPager) findViewById(R.id.category_pager);
        tabs = (TabLayout) findViewById(R.id.category_tabs);

        new ProcessCategories().execute();
        pager.setAdapter(new TabAdapter(getSupportFragmentManager()));
        tabs.setupWithViewPager(pager);
    }

    private class ProcessCategories extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                categories = Api.getAllCategories(true);
                for (Category category : categories) {
                    Utils.logInfo(clazz, category.toString());
                }
            } catch (JSONException e) {
                Utils.logError(clazz, "JSONException: " + e.getMessage());
            } catch (IOException e) {
                Utils.logError(clazz, "IOException: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (categories.size() == 0) {
                /*Snackbar.make(rootView, "Nie znaleziono kategorii", Snackbar.LENGTH_INDEFINITE)
                        .setActionTextColor(ContextCompat.getColor(clazz, R.color.colorError))
                        .setAction("ODŚWIEŻ", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // TODO: odswiez
                            }
                        }).show();*/
            } else {
                /*categoryAdapter = new CategoryAdapter(clazz, categories);
                gridView.setAdapter(categoryAdapter);*/
                Session.getInstance().setCategories(categories);
            }

            Intent intent = new Intent(INTENT_CATEGORY);
            intent.putParcelableArrayListExtra(INTENT_CATEGORY_LIST, categories);
            LocalBroadcastManager.getInstance(clazz).sendBroadcast(intent);
        }
    }

}
