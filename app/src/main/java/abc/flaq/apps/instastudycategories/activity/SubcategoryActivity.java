package abc.flaq.apps.instastudycategories.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.adapter.SubcategoryTabAdapter;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;
import abc.flaq.apps.instastudycategories.utils.Api;
import abc.flaq.apps.instastudycategories.utils.Session;
import abc.flaq.apps.instastudycategories.utils.Utils;

import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_FOREIGN_ID;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_NAME;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_SUBCATEGORY;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_SUBCATEGORY_ACTIVE;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_SUBCATEGORY_INACTIVE;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_SUBCATEGORY_INACTIVE_ADD_NEW;
import static abc.flaq.apps.instastudycategories.utils.Constants.TAB_INACTIVE;

public class SubcategoryActivity extends SessionActivity {

    private final AppCompatActivity clazz = this;
    private View rootView;
    private ViewPager pager;

    private List<Subcategory> subcategories = new ArrayList<>();
    private String categoryForeignId;
    private Boolean isApiWorking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subcategory_container);

        rootView = findViewById(android.R.id.content);

        pager = (ViewPager) findViewById(R.id.subcategory_pager);
        pager.setAdapter(new SubcategoryTabAdapter(getSupportFragmentManager()));

        TabLayout tabs = (TabLayout) findViewById(R.id.subcategory_tabs);
        tabs.setupWithViewPager(pager);

        Intent intent = getIntent();
        categoryForeignId = intent.getStringExtra(INTENT_CATEGORY_FOREIGN_ID);

        if (Utils.isEmpty(categoryForeignId)) {
            Utils.showError(rootView, "Puste categoryForeignId");
        } else {
            String categoryName = intent.getStringExtra(INTENT_CATEGORY_NAME);
            Utils.setActionBarTitle(clazz, Utils.getStringByCategoryName(clazz, categoryName));

            new ProcessSubcategories().execute();
        }
    }
    /*@Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        // Update subcategories when going back from User activity
        if (Utils.isNotEmpty(subcategoryAdapter) && subcategoryAdapter.getCount() > 0) {
            subcategories = Session.getInstance().getSubcategories(categoryForeignId);
            subcategoryAdapter = new SubcategoryAdapter(clazz, subcategories);
            gridView.setAdapter(subcategoryAdapter);

        }
    }*/

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
                        // TODO: sprawdzać, czy różne od nazw wszystkich podkategorii
                        /*if (Utils.isNotEmpty(input) && API_ALL_CATEGORY_NAME.equals(input.toString())) {
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                        }*/
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (Utils.isNotEmpty(dialog.getInputEditText()) && Utils.isNotEmpty(dialog.getInputEditText().getText())) {
                            Utils.showInfo(rootView, "Zaproponowano nową podkategorię: " + dialog.getInputEditText().getText());
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

            if (subcategories.size() == 0) {
                Utils.showQuickInfo(rootView, "Brak podkategorii");
            } else {
                Session.getInstance().setSubcategories(categoryForeignId, subcategories);
            }

            ArrayList<Subcategory> activeSubcategories = new ArrayList<>();
            ArrayList<Subcategory> inactiveSubcategories = new ArrayList<>();
            for (Subcategory subcategory : subcategories) {
                if (subcategory.isActive()) {
                    activeSubcategories.add(subcategory);
                } else {
                    inactiveSubcategories.add(subcategory);
                }
            }

            Intent activeIntent = new Intent(INTENT_SUBCATEGORY);
            activeIntent.putParcelableArrayListExtra(INTENT_SUBCATEGORY_ACTIVE, activeSubcategories);
            LocalBroadcastManager.getInstance(clazz).sendBroadcast(activeIntent);
            Intent inactiveIntent = new Intent(INTENT_SUBCATEGORY);
            inactiveIntent.putParcelableArrayListExtra(INTENT_SUBCATEGORY_INACTIVE, inactiveSubcategories);
            LocalBroadcastManager.getInstance(clazz).sendBroadcast(inactiveIntent);
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
                pager.setCurrentItem(TAB_INACTIVE, true);
                // FIXME: znajdź view drugiej zakładki i tam wyświetl snackbar
                Utils.showInfoDismiss(rootView, "Podkategoria stanie się aktywna po dołączeniu do niej 10 użytkowników");

                subcategories.add(newSubcategory);
                Session.getInstance().setSubcategories(categoryForeignId, subcategories);

                ArrayList<Subcategory> inactiveSubcategories = new ArrayList<>();
                for (Subcategory subcategory : subcategories) {
                    if (!subcategory.isActive()) {
                        inactiveSubcategories.add(subcategory);
                    }
                }

                Intent intent = new Intent(INTENT_SUBCATEGORY);
                intent.putParcelableArrayListExtra(INTENT_SUBCATEGORY_INACTIVE, inactiveSubcategories);
                intent.putExtra(INTENT_SUBCATEGORY_INACTIVE_ADD_NEW, true);
                LocalBroadcastManager.getInstance(clazz).sendBroadcast(intent);
            } else {
                Utils.showError(rootView, "Dodanie nowej podkategorii zakończone niepowodzeniem");
            }
        }
    }

}
