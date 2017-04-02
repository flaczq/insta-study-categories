package abc.flaq.apps.instastudycategories;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;

import com.crystal.crystalpreloaders.widgets.CrystalPreloader;
import com.etsy.android.grid.StaggeredGridView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static abc.flaq.apps.instastudycategories.Constants.INTENT_CATEGORY_ID;

public class CategoryActivity extends AppCompatActivity {

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

        Utils.log(Utils.LOG_DEBUG, clazz, "Build data: " +
                BuildConfig.FLAVOR_FULLNAME +
                "/" + BuildConfig.BUILD_TYPE + " " +
                BuildConfig.VERSION_NAME
        );

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                Category selected = categoryAdapter.getItem(position);
                Utils.log(Utils.LOG_DEBUG, clazz, "Selected position: " + position);

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
                Thread.sleep(1000); // FIXME: showing preloader, REMOVE
                categories = Api.getAllCategories(false);
                for (Category category : categories) {
                    Utils.log(Utils.LOG_DEBUG, clazz, category.toString());
                }
            } catch (InterruptedException e) {
                Utils.log(Utils.LOG_ERROR, clazz, "InterruptedException: " + e.toString());
            } catch (JSONException e) {
                Utils.log(Utils.LOG_ERROR, clazz, "JSONException: " + e.toString());
            } catch (IOException e) {
                Utils.log(Utils.LOG_ERROR, clazz, "IOException: " + e.toString());
            }

            return categories;
        }

        @Override
        protected void onPostExecute(List<Category> result) {
            super.onPostExecute(result);

            preloader.setVisibility(View.GONE);
            categoryAdapter = new CategoryAdapter(clazz, result);
            gridView.setAdapter(categoryAdapter);
        }
    }

}
