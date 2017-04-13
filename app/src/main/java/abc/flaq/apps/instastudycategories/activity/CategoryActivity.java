package abc.flaq.apps.instastudycategories.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.crystal.crystalpreloaders.widgets.CrystalPreloader;
import com.etsy.android.grid.StaggeredGridView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import abc.flaq.apps.instastudycategories.BuildConfig;
import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.adapter.CategoryAdapter;
import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.utils.Api;
import abc.flaq.apps.instastudycategories.utils.Utils;
import abc.flaq.apps.instastudycategories.utils.Session;

import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_ID;

public class CategoryActivity extends MenuActivity {

    private final Activity clazz = this;
    private CategoryAdapter categoryAdapter;
    private StaggeredGridView gridView;
    private CrystalPreloader preloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        gridView = (StaggeredGridView) findViewById(R.id.category_grid);
        preloader = (CrystalPreloader) findViewById(R.id.category_preloader);

        Utils.logDebug(clazz, "Build data: " +
                BuildConfig.FLAVOR_FULLNAME +
                "/" + BuildConfig.BUILD_TYPE + " " +
                BuildConfig.VERSION_NAME
        );

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                Category selected = categoryAdapter.getItem(position);
                Utils.logDebug(clazz, "Selected position: " + position);

                Intent nextIntent;
                if (selected.isAsSubcategory()) {
                    nextIntent = new Intent(clazz, UserActivity.class);
                } else {
                    nextIntent = new Intent(clazz, SubcategoryActivity.class);
                }
                nextIntent.putExtra(INTENT_CATEGORY_ID, selected.getId());
                clazz.startActivity(nextIntent);
            }
        });

        // Remember categories
        if (Utils.isEmpty(Session.getInstance().getCategories())) {
            new ProcessCategories().execute();
        } else {
            categoryAdapter = new CategoryAdapter(clazz, Session.getInstance().getCategories());
            gridView.setAdapter(categoryAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.menu_join).setVisible(false);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (preloader.isShown()) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_add:
                Utils.showMessage(clazz, "adding");
                break;
            case R.id.menu_join:
                // not available from here
                break;
            case R.id.menu_info:
                return super.onOptionsItemSelected(item);
            case R.id.menu_login:
                return super.onOptionsItemSelected(item);
            default:
                break;
        }
        return true;
    }

    private class ProcessCategories extends AsyncTask<Void, Void, List<Category>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            preloader.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Category> doInBackground(Void... params) {
            List<Category> categories = new ArrayList<>();
            try {
                categories = Api.getAllCategories(true);
                for (Category category : categories) {
                    Utils.logInfo(clazz, category.toString());
                }
            } catch (JSONException e) {
                Utils.logError(clazz, "JSONException: " + e.toString());
            } catch (IOException e) {
                Utils.logError(clazz, "IOException: " + e.toString());
            }
            return categories;
        }

        @Override
        protected void onPostExecute(List<Category> result) {
            // TODO: check if there are any categories
            super.onPostExecute(result);
            preloader.setVisibility(View.GONE);
            categoryAdapter = new CategoryAdapter(clazz, result);
            gridView.setAdapter(categoryAdapter);
            Session.getInstance().setCategories(result);
        }
    }

}
