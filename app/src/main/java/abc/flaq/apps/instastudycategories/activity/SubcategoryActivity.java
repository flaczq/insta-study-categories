package abc.flaq.apps.instastudycategories.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.adapter.SubcategoryTabAdapter;
import abc.flaq.apps.instastudycategories.api.Api;
import abc.flaq.apps.instastudycategories.general.Session;
import abc.flaq.apps.instastudycategories.helper.Utils;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;

import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_FOREIGN_ID;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_NAME;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_ACTIVE;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_ACTIVE_END;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_ACTIVE_START;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_INACTIVE;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_INACTIVE_END;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_INACTIVE_START;
import static abc.flaq.apps.instastudycategories.helper.Constants.TAB_ACTIVE;
import static abc.flaq.apps.instastudycategories.helper.Constants.TAB_INACTIVE;

public class SubcategoryActivity extends SessionActivity {

    private final AppCompatActivity clazz = this;
    private ViewPager pager;
    private TabLayout tabs;

    private List<Subcategory> subcategories = new ArrayList<>();
    private String categoryForeignId;
    private Boolean isApiWorking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subcategory_container);

        pager = (ViewPager) findViewById(R.id.subcategory_pager);
        pager.setAdapter(new SubcategoryTabAdapter(getSupportFragmentManager()));

        tabs = (TabLayout) findViewById(R.id.subcategory_tabs);
        tabs.setupWithViewPager(pager);

        Intent intent = getIntent();
        categoryForeignId = intent.getStringExtra(INTENT_CATEGORY_FOREIGN_ID);

        if (Utils.isEmpty(categoryForeignId)) {
            Utils.showError(tabs, "Puste categoryForeignId");
        } else {
            String categoryName = intent.getStringExtra(INTENT_CATEGORY_NAME);
            Utils.setActionBarTitle(clazz, Utils.getStringByCategoryName(clazz, categoryName), null);

            if (Utils.isEmpty(Session.getInstance().getSubcategories(categoryForeignId))) {
                new ProcessSubcategories().execute();
            } else {
                subcategories = Session.getInstance().getSubcategories(categoryForeignId);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        endProcessSubcategoryFragment();
                    }
                }, 50);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        // Load subcategories when going back from User activity and user has joined or left the subcategory
        if (Session.getInstance().isSubcategoryChanged()) {
            new ProcessSubcategories().execute();
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
        final List<String> subcategoriesNames = Utils.getSubcategoriesNames(clazz, categoryForeignId);

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
                        if (Utils.isNotEmpty(input) && Utils.isNotEmpty(input.toString()) &&
                                subcategoriesNames.contains(input.toString().trim().toLowerCase())) {
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                        }
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (Utils.isNotEmpty(dialog.getInputEditText()) && Utils.isNotEmpty(dialog.getInputEditText().getText())) {
                            //Utils.showInfo(tabs, "Zaproponowano nową podkategorię: " + dialog.getInputEditText().getText());
                            new ProcessAddSubcategory().execute(dialog.getInputEditText().getText().toString());
                        }
                        dialog.dismiss();
                    }
                })
                .alwaysCallInputCallback()
                .show();
    }

    private void startSubcategoryFragment() {
        Intent intent = new Intent(INTENT_SUBCATEGORY);
        intent.putExtra(INTENT_SUBCATEGORY_ACTIVE_START, true);
        intent.putExtra(INTENT_SUBCATEGORY_INACTIVE_START, true);
        LocalBroadcastManager.getInstance(clazz).sendBroadcast(intent);
    }
    private void endProcessSubcategoryFragment() {
        if (subcategories.size() == 0) {
            Utils.showQuickInfo(tabs, "Brak podkategorii");
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

        if (activeSubcategories.size() == 0 && inactiveSubcategories.size() > 0) {
            pager.setCurrentItem(TAB_INACTIVE, true);
        }

        Intent activeIntent = new Intent(INTENT_SUBCATEGORY);
        activeIntent.putParcelableArrayListExtra(INTENT_SUBCATEGORY_ACTIVE, activeSubcategories);
        activeIntent.putExtra(INTENT_SUBCATEGORY_ACTIVE_END, true);
        LocalBroadcastManager.getInstance(clazz).sendBroadcast(activeIntent);
        Intent inactiveIntent = new Intent(INTENT_SUBCATEGORY);
        inactiveIntent.putParcelableArrayListExtra(INTENT_SUBCATEGORY_INACTIVE, inactiveSubcategories);
        inactiveIntent.putExtra(INTENT_SUBCATEGORY_INACTIVE_END, true);
        LocalBroadcastManager.getInstance(clazz).sendBroadcast(inactiveIntent);
    }

    private class ProcessSubcategories extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isApiWorking = true;

            // Don't show preloader when going back from User activity
            if (!Session.getInstance().isSubcategoryChanged()) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startSubcategoryFragment();
                    }
                }, 50);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                subcategories = Api.getSubcategoriesByCategoryForeignId(true, categoryForeignId);
                for (Subcategory subcategory : subcategories) {
                    Utils.logDebug(clazz, subcategory.toString());
                }
            } catch (JSONException e) {
                Utils.logError(clazz, "JSONException: " + e.getMessage());
            } catch (IOException e) {
                Utils.logError(clazz, "IOException: " + e.getMessage());
                Utils.showConnectionError(tabs, "Błąd pobierania podkategorii");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            isApiWorking = false;

            Session.getInstance().setSubcategoryChanged(false);
            endProcessSubcategoryFragment();
        }
    }

    private class ProcessAddSubcategory extends AsyncTask<String, Void, Boolean> {
        Subcategory newSubcategory;   // TODO: ogranicz mozliwosc do x-razy

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pager.setCurrentItem(TAB_ACTIVE, true);
            isApiWorking = true;

            startSubcategoryFragment();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String subcategoryName = params[0];
            Boolean result = Boolean.FALSE;
            try {
                newSubcategory = Api.addSubcategory(subcategoryName, categoryForeignId);
                if (Utils.isNotEmpty(newSubcategory)) {
                    result = Boolean.TRUE;
                    Api.addUserToSubcategory(Session.getInstance().getUser(), newSubcategory);
                }
            } catch (JSONException e) {
                Utils.logError(clazz, "JSONException: " + e.getMessage());
            } catch (IOException e) {
                Utils.logError(clazz, "IOException: " + e.getMessage());
                Utils.showConnectionError(tabs, "Błąd dodawania podkategorii");
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            isApiWorking = false;

            if (result) {
                Utils.showInfoDismiss(tabs,
                        "Nowa podkategoria znajduje się w zakładce NIEAKTYWNE.\n" +
                                "Stanie się aktywna po dołączeniu do niej 10 użytkowników."
                );

                // Don't show preloader, because subcategories are loaded just after
                Session.getInstance().setSubcategoryChanged(true);
                Session.getInstance().setCategoryChanged(true);
                new ProcessSubcategories().execute();
            } else {
                Utils.showError(tabs, "Dodanie nowej podkategorii zakończone niepowodzeniem");
            }
        }
    }

}
