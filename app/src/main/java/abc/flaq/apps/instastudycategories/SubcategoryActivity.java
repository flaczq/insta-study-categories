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

public class SubcategoryActivity extends AppCompatActivity {

    private final Activity clazz = this;
    private StaggeredGridView gridView;
    private CrystalPreloader preloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_subcategory);
        gridView = (StaggeredGridView) findViewById(R.id.subcategory_gridview);
        preloader = (CrystalPreloader) findViewById(R.id.subcategory_preloader);

        new ProcessSubcategories().execute();
    }

    private class ProcessSubcategories extends AsyncTask<Void, Void, List<Subcategory>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            preloader.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Subcategory> doInBackground(Void... voids) {
            List<Subcategory> subcategories = new ArrayList<>();

            try {
                Thread.sleep(5000); // FIXME: showing preloader, REMOVE
                subcategories = Api.getSubcategories();
                for (Subcategory subcategory : subcategories) {
                    Utils.log(Utils.LOG_DEBUG, clazz, subcategory.toString());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Utils.log(Utils.LOG_ERROR, clazz, e.getMessage());
            } catch (JSONException e) {
                e.printStackTrace();
                Utils.log(Utils.LOG_ERROR, clazz, e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Utils.log(Utils.LOG_ERROR, clazz, e.getMessage());
            }

            return subcategories;
        }

        @Override
        protected void onPostExecute(List<Subcategory> subcategories) {
            super.onPostExecute(subcategories);

            preloader.setVisibility(View.GONE);
            SubcategoryAdapter subcategoryAdapter = new SubcategoryAdapter(clazz, subcategories);
            gridView.setAdapter(subcategoryAdapter);
        }
    }

}
