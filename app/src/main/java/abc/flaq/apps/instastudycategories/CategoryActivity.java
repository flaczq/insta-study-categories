package abc.flaq.apps.instastudycategories;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.crystal.crystalpreloaders.widgets.CrystalPreloader;
import com.etsy.android.grid.StaggeredGridView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private final Activity clazz = this;
    private StaggeredGridView gridView;
    private CrystalPreloader preloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_category);
        gridView = (StaggeredGridView) findViewById(R.id.category_gridview);
        preloader = (CrystalPreloader) findViewById(R.id.category_preloader);

        Utils.log(Utils.LOG_DEBUG, clazz, "Build data: " +
                BuildConfig.FLAVOR_FULLNAME +
                "/" + BuildConfig.BUILD_TYPE + " " +
                BuildConfig.VERSION_NAME
        );

        new ProcessCategories().execute();
    }

    private class ProcessCategories extends AsyncTask<Void, Void, List<Category>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            preloader.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Category> doInBackground(Void... voids) {
            List<Category> categories = new ArrayList<>();
            try {
                Thread.sleep(5000); // FIXME: showing preloader, REMOVE
                categories = Api.getCategories();
                for (Category category : categories) {
                    Utils.log(Utils.LOG_DEBUG, clazz, category.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Utils.log(Utils.LOG_ERROR, clazz, e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Utils.log(Utils.LOG_ERROR, clazz, e.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
                Utils.log(Utils.LOG_ERROR, clazz, e.getMessage());
            }
            return categories;
        }

        @Override
        protected void onPostExecute(List<Category> result) {
            super.onPostExecute(result);

            preloader.setVisibility(View.GONE);
            CategoryAdapter categoryAdapter = new CategoryAdapter(clazz, result);
            gridView.setAdapter(categoryAdapter);
        }
    }

}
