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
import java.util.List;

import abc.flaq.apps.instastudycategories.BuildConfig;
import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.adapter.CategoryAdapter;
import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.utils.Api;
import abc.flaq.apps.instastudycategories.utils.GeneralUtils;

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

        GeneralUtils.logDebug(clazz, "Build data: " +
                BuildConfig.FLAVOR_FULLNAME +
                "/" + BuildConfig.BUILD_TYPE + " " +
                BuildConfig.VERSION_NAME
        );

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                Category selected = categoryAdapter.getItem(position);
                GeneralUtils.logDebug(clazz, "Selected position: " + position);

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

        new ProcessCategories().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.menu_join).setVisible(false);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                GeneralUtils.showMessage(clazz, "adding");
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

    private class ProcessCategories extends AsyncTask<Void, Void, Void> {
        private List<Category> categories;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            preloader.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(1000); // FIXME: showing preloader, REMOVE
                categories = Api.getAllCategories(true);    // FIXME: testing
                for (Category category : categories) {
                    GeneralUtils.logInfo(clazz, category.toString());
                }
            } catch (InterruptedException e) {
                GeneralUtils.logError(clazz, "InterruptedException: " + e.toString());
            } catch (JSONException e) {
                GeneralUtils.logError(clazz, "JSONException: " + e.toString());
            } catch (IOException e) {
                GeneralUtils.logError(clazz, "IOException: " + e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            preloader.setVisibility(View.GONE);
            categoryAdapter = new CategoryAdapter(clazz, categories);
            gridView.setAdapter(categoryAdapter);
        }
    }

}
