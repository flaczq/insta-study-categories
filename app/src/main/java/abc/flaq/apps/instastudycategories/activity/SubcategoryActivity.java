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

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.adapter.SubcategoryAdapter;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;
import abc.flaq.apps.instastudycategories.utils.Api;
import abc.flaq.apps.instastudycategories.utils.Session;
import abc.flaq.apps.instastudycategories.utils.Utils;

import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_ID;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_SUBCATEGORY_ID;

public class SubcategoryActivity extends MenuActivity {

    private final Activity clazz = this;
    private SubcategoryAdapter subcategoryAdapter;
    private Intent intent;
    private StaggeredGridView gridView;
    private CrystalPreloader preloader;
    private String categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subcategory);
        gridView = (StaggeredGridView) findViewById(R.id.subcategory_grid);
        preloader = (CrystalPreloader) findViewById(R.id.subcategory_preloader);

        intent = getIntent();
        categoryId = intent.getStringExtra(INTENT_CATEGORY_ID);

        if (Utils.isEmpty(categoryId)) {
            Utils.afterError(clazz, "categoryId is empty");
            finish();
        } else {
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                    String selected = subcategoryAdapter.getItemRealId(position);
                    Utils.logDebug(clazz, "Selected position: " + position);
                    Intent nextIntent = new Intent(clazz, UserActivity.class);
                    nextIntent.putExtra(INTENT_SUBCATEGORY_ID, selected);
                    clazz.startActivity(nextIntent);
                }
            });

            // Remember subcategories
            if (Utils.isEmpty(Session.getInstance().getSubcategories(categoryId))) {
                new ProcessSubcategories().execute();
            } else {
                subcategoryAdapter = new SubcategoryAdapter(clazz, Session.getInstance().getSubcategories(categoryId));
                gridView.setAdapter(subcategoryAdapter);
            }
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

    private class ProcessSubcategories extends AsyncTask<Void, Void, List<Subcategory>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            preloader.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Subcategory> doInBackground(Void... params) {
            List<Subcategory> subcategories = new ArrayList<>();
            try {
                subcategories = Api.getSubcategoriesByCategoryId(categoryId);
                subcategories = Api.getAllSubcategories(true); // FIXME: testing
                for (Subcategory subcategory : subcategories) {
                    Utils.logInfo(clazz, subcategory.toString());
                }
            } catch (JSONException e) {
                Utils.logError(clazz, "JSONException: " + e.toString());
            } catch (IOException e) {
                Utils.logError(clazz, "IOException: " + e.toString());
            }
            return subcategories;
        }

        @Override
        protected void onPostExecute(List<Subcategory> result) {
            // TODO: check if there are any subcategories
            super.onPostExecute(result);
            preloader.setVisibility(View.GONE);
            subcategoryAdapter = new SubcategoryAdapter(clazz, result);
            gridView.setAdapter(subcategoryAdapter);
            Session.getInstance().setSubcategories(categoryId, result);
        }
    }

}
