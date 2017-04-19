package abc.flaq.apps.instastudycategories.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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

import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_FOREIGN_ID;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_SUBCATEGORY_FOREIGN_ID;

public class SubcategoryActivity extends MenuActivity {

    private final Activity clazz = this;
    private SubcategoryAdapter subcategoryAdapter;
    private Intent intent;
    private StaggeredGridView gridView;
    private CrystalPreloader preloader;
    private String categoryForeignId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subcategory);
        gridView = (StaggeredGridView) findViewById(R.id.subcategory_grid);
        preloader = (CrystalPreloader) findViewById(R.id.subcategory_preloader);

        intent = getIntent();
        categoryForeignId = intent.getStringExtra(INTENT_CATEGORY_FOREIGN_ID);

        if (Utils.isEmpty(categoryForeignId)) {
            Utils.afterError(clazz, "categoryForeignId is empty");
            finish();
        } else {
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                    Subcategory selected = subcategoryAdapter.getItem(position);
                    Intent nextIntent = new Intent(clazz, UserActivity.class);
                    nextIntent.putExtra(INTENT_SUBCATEGORY_FOREIGN_ID, selected.getForeignId());
                    clazz.startActivity(nextIntent);
                }
            });

            // Remember subcategories
            if (Utils.isEmpty(Session.getInstance().getSubcategories(categoryForeignId))) {
                new ProcessSubcategories().execute();
            } else {
                subcategoryAdapter = new SubcategoryAdapter(clazz, Session.getInstance().getSubcategories(categoryForeignId));
                gridView.setAdapter(subcategoryAdapter);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.menu_join).setVisible(false);
        menu.findItem(R.id.menu_leave).setVisible(false);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (preloader.isShown()) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_suggest:
                suggestSubcategoryDialog();
                break;
            case R.id.menu_join:
                // not available from here
                break;
            case R.id.menu_leave:
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

    @Override
    protected void onResume() {
        super.onResume();
        // Update subcategories when going back from User activity
        if (Utils.isNotEmpty(subcategoryAdapter)) {
            subcategoryAdapter = new SubcategoryAdapter(clazz, Session.getInstance().getSubcategories(categoryForeignId));
            gridView.setAdapter(subcategoryAdapter);
        }
    }

    private void suggestSubcategoryDialog() {
        new MaterialDialog.Builder(clazz)
                .title("Nowa podkategoria")
                .content("Zaproponuj nową podkategorię")
                .positiveText("Zaproponuj")
                .negativeText("Anuluj")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .inputRangeRes(1, -1, R.color.colorError)
                .input("Nowa podkategoria...", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        // nothing
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (Utils.isNotEmpty(dialog.getInputEditText())) {
                            Utils.showMessage(clazz, "Zaproponowano " + dialog.getInputEditText().getText());
                            // i -> Api.addSubcategory(subcategory);
                        }
                        dialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).show();
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
                subcategories = Api.getSubcategoriesByCategoryForeignId(true, categoryForeignId);
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
            super.onPostExecute(result);
            if (result.size() == 0) {
                Utils.showMessage(clazz, "No subcategories found");
            } else {
                preloader.setVisibility(View.GONE);
                subcategoryAdapter = new SubcategoryAdapter(clazz, result);
                gridView.setAdapter(subcategoryAdapter);
                Session.getInstance().setSubcategories(categoryForeignId, result);
            }
        }
    }

}
