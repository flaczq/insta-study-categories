package abc.flaq.apps.instastudycategories.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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
import abc.flaq.apps.instastudycategories.adapter.CategoryAdapter;
import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.utils.Api;
import abc.flaq.apps.instastudycategories.utils.Session;
import abc.flaq.apps.instastudycategories.utils.Utils;

import static abc.flaq.apps.instastudycategories.utils.Constants.API_ALL_CATEGORY_NAME;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_FOREIGN_ID;

public class CategoryActivity extends SessionActivity {

    private final Activity clazz = this;
    private View rootView;
    private StaggeredGridView gridView;
    private CategoryAdapter categoryAdapter;
    private CrystalPreloader preloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = findViewById(android.R.id.content);
        setContentView(R.layout.activity_category);
        gridView = (StaggeredGridView) findViewById(R.id.category_grid);
        preloader = (CrystalPreloader) findViewById(R.id.category_preloader);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                Category selected = categoryAdapter.getItem(position);
                Intent nextIntent;
                if (selected.isAsSubcategory()) {
                    nextIntent = new Intent(clazz, UserActivity.class);
                } else {
                    nextIntent = new Intent(clazz, SubcategoryActivity.class);
                }
                nextIntent.putExtra(INTENT_CATEGORY_FOREIGN_ID, selected.getForeignId());
                clazz.startActivity(nextIntent);
            }
        });

        // Remember categories
        if (Utils.isEmpty(Session.getInstance().getCategories()) || Session.getInstance().getCategories().size() == 0) {
            new ProcessCategories().execute();
        } else {
            categoryAdapter = new CategoryAdapter(clazz, Session.getInstance().getCategories());
            gridView.setAdapter(categoryAdapter);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        // Update categories when going back from User or Subcategory activity
        if (Utils.isNotEmpty(categoryAdapter) && categoryAdapter.getCount() > 0) {
            categoryAdapter = new CategoryAdapter(clazz, Session.getInstance().getCategories());
            gridView.setAdapter(categoryAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        setMainMenuVisibility(menu);
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
                showSuggestCategoryDialog();
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

    private void showSuggestCategoryDialog() {
        new MaterialDialog.Builder(clazz)
                .title("Nowa kategoria")
                .content("Zaproponuj nową kategorię")
                .positiveText("Zaproponuj")
                .negativeText("Anuluj")
                .titleColorRes(R.color.colorPrimaryDark)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .inputRangeRes(1, 20, R.color.colorError)
                .input("Wpisz nazwę...", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        // TODO: sprawdzać, czy różne od nazw wszystkich kategorii
                        if (Utils.isNotEmpty(input) && API_ALL_CATEGORY_NAME.equals(input.toString())) {
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                        }
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (Utils.isNotEmpty(dialog.getInputEditText()) && Utils.isNotEmpty(dialog.getInputEditText().getText())) {
                            Utils.showInfo(rootView, "Zaproponowano kategorię: " + dialog.getInputEditText().getText());
                            new ProcessAddCategory().execute(dialog.getInputEditText().getText().toString());
                        }
                        dialog.dismiss();
                    }
                })
                .alwaysCallInputCallback()
                .show();
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
                Utils.logError(clazz, "JSONException: " + e.getMessage());
            } catch (IOException e) {
                Utils.logError(clazz, "IOException: " + e.getMessage());
            }
            return categories;
        }

        @Override
        protected void onPostExecute(List<Category> result) {
            super.onPostExecute(result);
            preloader.setVisibility(View.GONE);

            if (result.size() == 0) {
                Snackbar.make(rootView, "Nie znaleziono kategorii", Snackbar.LENGTH_INDEFINITE)
                        .setActionTextColor(ContextCompat.getColor(clazz, R.color.colorError))
                        .setAction("ODŚWIEŻ", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // TODO: odswiez
                            }
                        }).show();
            } else {
                categoryAdapter = new CategoryAdapter(clazz, result);
                gridView.setAdapter(categoryAdapter);
                Session.getInstance().setCategories(result);
            }
        }
    }

    private class ProcessAddCategory extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String categoryName = params[0];
            Boolean result = Boolean.FALSE;
            try {
                String categoryForeignId = Api.addCategory(categoryName);
                if (Utils.isNotEmpty(categoryForeignId)) {
                    result = Boolean.TRUE;
                    Api.addUserToCategory(Session.getInstance().getUser(), categoryForeignId);
                }
            } catch (JSONException e) {
                Utils.logError(clazz, "JSONException: " + e.getMessage());
            } catch (IOException e) {
                Utils.logError(clazz, "IOException: " + e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result) {
                Utils.showInfo(rootView, "Dodano nową kategorię");
                categoryAdapter = new CategoryAdapter(clazz, Session.getInstance().getCategories());
                gridView.setAdapter(categoryAdapter);
            } else {
                Utils.showError(rootView, "Dodanie nowej kategorii zakończone niepowodzeniem");
            }
        }
    }

}
