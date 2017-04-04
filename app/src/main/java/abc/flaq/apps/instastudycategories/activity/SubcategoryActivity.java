package abc.flaq.apps.instastudycategories.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;

import com.crystal.crystalpreloaders.widgets.CrystalPreloader;
import com.etsy.android.grid.StaggeredGridView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.adapter.MenuActivity;
import abc.flaq.apps.instastudycategories.adapter.SubcategoryAdapter;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;
import abc.flaq.apps.instastudycategories.utils.Api;
import abc.flaq.apps.instastudycategories.utils.GeneralUtils;

import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_ID;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_SUBCATEGORY_ID;

public class SubcategoryActivity extends MenuActivity {

    private final Activity clazz = this;
    private SubcategoryAdapter subcategoryAdapter;
    private Intent intent;

    private StaggeredGridView gridView;
    private CrystalPreloader preloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_subcategory);
        gridView = (StaggeredGridView) findViewById(R.id.subcategory_grid);
        preloader = (CrystalPreloader) findViewById(R.id.subcategory_preloader);

        intent = getIntent();
        String categoryId = intent.getStringExtra(INTENT_CATEGORY_ID);

        if (GeneralUtils.isEmpty(categoryId)) {
            GeneralUtils.afterError(clazz, "categoryId is empty");
            finish();
        } else {
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                    String selected = subcategoryAdapter.getItemRealId(position);
                    GeneralUtils.logDebug(clazz, "Selected position: " + position);
                    Intent nextIntent = new Intent(clazz, UserActivity.class);
                    nextIntent.putExtra(INTENT_SUBCATEGORY_ID, selected);
                    clazz.startActivity(nextIntent);
                }
            });

            new ProcessSubcategories().execute(categoryId);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
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
                subcategories = Api.getAllSubcategories(false);
                for (Subcategory subcategory : subcategories) {
                    GeneralUtils.logInfo(clazz, subcategory.toString());
                }
            } catch (InterruptedException e) {
                GeneralUtils.logError(clazz, "InterruptedException: " + e.toString());
            } catch (JSONException e) {
                GeneralUtils.logError(clazz, "JSONException: " + e.toString());
            } catch (IOException e) {
                GeneralUtils.logError(clazz, "IOException: " + e.toString());
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
