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
import static abc.flaq.apps.instastudycategories.Constants.INTENT_SUBCATEGORY_ID;

public class SubcategoryActivity extends AppCompatActivity {

    private final Activity clazz = this;
    private SubcategoryAdapter subcategoryAdapter;
    private Intent intent;

    private StaggeredGridView gridView;
    private CrystalPreloader preloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_subcategory);
        gridView = (StaggeredGridView) findViewById(R.id.subcategory_gridview);
        preloader = (CrystalPreloader) findViewById(R.id.subcategory_preloader);

        intent = getIntent();
        String categoryId = intent.getStringExtra(INTENT_CATEGORY_ID);

        if (Utils.isEmpty(categoryId)) {
            Utils.afterError(clazz);
            finish();
        } else {
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                    String selected = subcategoryAdapter.getItemRealId(position);
                    Utils.log(Utils.LOG_DEBUG, clazz, "Selected position: " + position);
                    Intent nextIntent = new Intent(clazz, UserActivity.class);
                    nextIntent.putExtra(INTENT_SUBCATEGORY_ID, selected);
                    clazz.startActivity(nextIntent);
                }
            });

            new ProcessSubcategories().execute(categoryId);
        }
    }

    private class ProcessSubcategories extends AsyncTask<String, Void, List<Subcategory>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            preloader.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Subcategory> doInBackground(String... params) {
            String categoryId = params[0];
            List<Subcategory> subcategories = new ArrayList<>();

            try {
                Thread.sleep(1000); // FIXME: showing preloader, REMOVE
                subcategories = Api.getSubcategoriesByCategoryId(categoryId);
                for (Subcategory subcategory : subcategories) {
                    Utils.log(Utils.LOG_DEBUG, clazz, subcategory.toString());
                }
            } catch (InterruptedException e) {
                Utils.log(Utils.LOG_ERROR, clazz, "InterruptedException: " + e.toString());
            } catch (JSONException e) {
                Utils.log(Utils.LOG_ERROR, clazz, "JSONException: " + e.toString());
            } catch (IOException e) {
                Utils.log(Utils.LOG_ERROR, clazz, "IOException: " + e.toString());
            }

            return subcategories;
        }

        @Override
        protected void onPostExecute(List<Subcategory> result) {
            super.onPostExecute(result);

            preloader.setVisibility(View.GONE);
            subcategoryAdapter = new SubcategoryAdapter(clazz, result);
            gridView.setAdapter(subcategoryAdapter);
        }
    }

}
