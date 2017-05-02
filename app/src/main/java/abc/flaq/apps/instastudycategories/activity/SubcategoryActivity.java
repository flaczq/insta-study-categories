package abc.flaq.apps.instastudycategories.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_NAME;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_SUBCATEGORY_FOREIGN_ID;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_SUBCATEGORY_NAME;

public class SubcategoryActivity extends SessionActivity {

    private final AppCompatActivity clazz = this;
    private View rootView;
    private StaggeredGridView gridView;
    private SubcategoryAdapter subcategoryAdapter;
    private CrystalPreloader preloader;

    private List<Subcategory> subcategories = new ArrayList<>();
    private String categoryForeignId;
    private Boolean isApiWorking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subcategory);

        rootView = findViewById(android.R.id.content);
        gridView = (StaggeredGridView) findViewById(R.id.subcategory_grid);
        preloader = (CrystalPreloader) findViewById(R.id.subcategory_preloader);

        Intent intent = getIntent();
        categoryForeignId = intent.getStringExtra(INTENT_CATEGORY_FOREIGN_ID);

        if (Utils.isEmpty(categoryForeignId)) {
            Utils.showError(rootView, "Puste categoryForeignId");
        } else {
            String categoryName = intent.getStringExtra(INTENT_CATEGORY_NAME);
            Utils.setActionBarTitle(clazz, Utils.getStringByCategoryName(clazz, categoryName));

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                    Subcategory selected = subcategoryAdapter.getItem(position);
                    Intent nextIntent = new Intent(clazz, UserActivity.class);
                    nextIntent.putExtra(INTENT_SUBCATEGORY_FOREIGN_ID, selected.getForeignId());
                    nextIntent.putExtra(INTENT_SUBCATEGORY_NAME, selected.getName());
                    clazz.startActivity(nextIntent);
                }
            });

            // FIXME: Remember subcategories - prawdopodobnie do wywalenia z powodu przesuwania podkategorii z aktywnych do nieaktywnych?
            if (Utils.isEmpty(Session.getInstance().getSubcategories(categoryForeignId)) ||
                    Session.getInstance().getSubcategories(categoryForeignId).size() == 0) {
                new ProcessSubcategories().execute();
            } else {
                subcategoryAdapter = new SubcategoryAdapter(clazz, Session.getInstance().getSubcategories(categoryForeignId));
                gridView.setAdapter(subcategoryAdapter);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        // Update subcategories when going back from User activity
        if (Utils.isNotEmpty(subcategoryAdapter) && subcategoryAdapter.getCount() > 0) {
            subcategories = Session.getInstance().getSubcategories(categoryForeignId);
            subcategoryAdapter = new SubcategoryAdapter(clazz, subcategories);
            gridView.setAdapter(subcategoryAdapter);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        setMainMenuVisibility(menu);
        menu.findItem(R.id.menu_join).setVisible(false);
        menu.findItem(R.id.menu_leave).setVisible(false);
        menu.findItem(R.id.menu_sort).setVisible(false);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isApiWorking) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_suggest:
                showSuggestSubcategoryDialog();
                break;
            case R.id.menu_join:
                // not available from here
                break;
            case R.id.menu_leave:
                // not available from here
                break;
            case R.id.menu_sort:
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

    private void showSuggestSubcategoryDialog() {
        new MaterialDialog.Builder(clazz)
                .title("Nowa podkategoria")
                .content("Zaproponuj nową podkategorię")
                .positiveText("Zaproponuj")
                .negativeText("Anuluj")
                .titleColorRes(R.color.colorPrimaryDark)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .inputRangeRes(1, 20, R.color.colorError)
                .input("Wpisz nazwę...", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        // nothing
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (Utils.isNotEmpty(dialog.getInputEditText()) && Utils.isNotEmpty(dialog.getInputEditText().getText())) {
                            Utils.showInfo(rootView, "Zaproponowano podkategorię: " + dialog.getInputEditText().getText());
                            new ProcessAddSubcategory().execute(dialog.getInputEditText().getText().toString());
                        }
                        dialog.dismiss();
                    }
                }).show();
    }

    private class ProcessSubcategories extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isApiWorking = true;
            preloader.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                subcategories = Api.getSubcategoriesByCategoryForeignId(true, categoryForeignId);
                for (Subcategory subcategory : subcategories) {
                    Utils.logInfo(clazz, subcategory.toString());
                }
            } catch (JSONException e) {
                Utils.logError(clazz, "JSONException: " + e.getMessage());
            } catch (IOException e) {
                Utils.logError(clazz, "IOException: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            isApiWorking = false;
            preloader.setVisibility(View.GONE);

            if (subcategories.size() == 0) {
                Snackbar.make(rootView, "Brak podkategorii", Snackbar.LENGTH_INDEFINITE)
                        .setActionTextColor(ContextCompat.getColor(clazz, R.color.colorError))
                        .setAction("ODŚWIEŻ", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // TODO: odswiez
                            }
                        }).show();
            } else {
                subcategoryAdapter = new SubcategoryAdapter(clazz, subcategories);
                gridView.setAdapter(subcategoryAdapter);
                Session.getInstance().setSubcategories(categoryForeignId, subcategories);
            }
        }
    }

    private class ProcessAddSubcategory extends AsyncTask<String, Void, Boolean> {
        Subcategory newSubcategory;   // TODO: ogranicz mozliwosc do x-razy

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isApiWorking = true;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String subcategoryName = params[0];
            Boolean result = Boolean.FALSE;
            try {
                newSubcategory = Api.addSubcategory(subcategoryName, categoryForeignId);
                if (Utils.isNotEmpty(newSubcategory)) {
                    result = Boolean.TRUE;
                    Api.addUserToSubcategory(Session.getInstance().getUser(), newSubcategory.getForeignId());
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
            isApiWorking = false;

            if (result) {
                // TODO: idz do nowej podkategorii - zakladka nieaktywne
                //Utils.showInfo(rootView, "Dodano nową podkategorię");
                //subcategoryAdapter = new SubcategoryAdapter(clazz, Session.getInstance().getSubcategories(categoryForeignId));
                //gridView.setAdapter(subcategoryAdapter);
                subcategories.add(newSubcategory);
                //Session.getInstance().setSubcategories(categoryForeignId, subcategories);
                subcategoryAdapter.notifyDataSetChanged();
            } else {
                Utils.showError(rootView, "Dodanie nowej podkategorii zakończone niepowodzeniem");
            }
        }
    }

}
