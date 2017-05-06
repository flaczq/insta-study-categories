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

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.adapter.CategoryTabAdapter;
import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.utils.Api;
import abc.flaq.apps.instastudycategories.utils.Session;
import abc.flaq.apps.instastudycategories.utils.Utils;

import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_ACTIVE;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_INACTIVE;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_INACTIVE_ADD_NEW;
import static abc.flaq.apps.instastudycategories.utils.Constants.TAB_INACTIVE;

public class CategoryActivity extends SessionActivity {

    private AppCompatActivity clazz = this;
    private View rootView;
    private ViewPager pager;

    private ArrayList<Category> categories = new ArrayList<>();
    private Boolean isApiWorking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {     // FIXME: podnieść widok gdy wyświetla się snackbar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_container);

        rootView = findViewById(android.R.id.content);

        pager = (ViewPager) findViewById(R.id.category_pager);
        pager.setAdapter(new CategoryTabAdapter(getSupportFragmentManager()));

        TabLayout tabs = (TabLayout) findViewById(R.id.category_tabs);
        tabs.setupWithViewPager(pager);

        new ProcessCategories().execute();
    }
    /*@Override
    public void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        // Update categories when going back from User or Subcategory activity
        if (Utils.isNotEmpty(categoryAdapter) && categoryAdapter.getCount() > 0) {
            categoryAdapter = new CategoryAdapter(getActivity(), Session.getInstance().getCategories());
            gridView.setAdapter(categoryAdapter);
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
                showSuggestCategoryDialog();
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
                        /*if (Utils.isNotEmpty(input) && API_ALL_CATEGORY_NAME.equals(input.toString())) {
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                        }*/
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (Utils.isNotEmpty(dialog.getInputEditText()) && Utils.isNotEmpty(dialog.getInputEditText().getText())) {
                            Utils.showInfo(rootView, "Zaproponowano nową kategorię: " + dialog.getInputEditText().getText());
                            new ProcessAddCategory().execute(dialog.getInputEditText().getText().toString());
                        }
                        dialog.dismiss();
                    }
                })
                .alwaysCallInputCallback()
                .show();
    }
    
    private class ProcessCategories extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isApiWorking = true;
        }

        @Override
        protected Void doInBackground(Void... params) {
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
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (categories.size() == 0) {
                Utils.showConnectionError(rootView, "Nie znaleziono kategorii");
            } else {
                isApiWorking = false;
                Session.getInstance().setCategories(categories);
            }

            ArrayList<Category> activeCategories = new ArrayList<>();
            ArrayList<Category> inactiveCategories = new ArrayList<>();
            for (Category category : categories) {
                if (category.isActive()) {
                    activeCategories.add(category);
                } else {
                    inactiveCategories.add(category);
                }
            }

            Intent activeIntent = new Intent(INTENT_CATEGORY);
            activeIntent.putParcelableArrayListExtra(INTENT_CATEGORY_ACTIVE, activeCategories);
            LocalBroadcastManager.getInstance(clazz).sendBroadcast(activeIntent);
            Intent inactiveIntent = new Intent(INTENT_CATEGORY);
            inactiveIntent.putParcelableArrayListExtra(INTENT_CATEGORY_INACTIVE, inactiveCategories);
            LocalBroadcastManager.getInstance(clazz).sendBroadcast(inactiveIntent);
        }
    }
    private class ProcessAddCategory extends AsyncTask<String, Void, Boolean> {
        Category newCategory;   // TODO: ogranicz mozliwosc do x-razy

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isApiWorking = true;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String categoryName = params[0];
            Boolean result = Boolean.FALSE;
            try {
                newCategory = Api.addCategory(categoryName);
                if (Utils.isNotEmpty(newCategory)) {
                    result = Boolean.TRUE;
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
                Utils.showInfoDismiss(rootView, "Kategoria będzie aktywna po uzyskaniu 10 głosów");
                pager.setCurrentItem(TAB_INACTIVE, true);

                categories.add(newCategory);
                Session.getInstance().setCategories(categories);

                ArrayList<Category> inactiveCategories = new ArrayList<>();
                for (Category category : categories) {
                    if (!category.isActive()) {
                        inactiveCategories.add(category);
                    }
                }

                Intent intent = new Intent(INTENT_CATEGORY);
                intent.putParcelableArrayListExtra(INTENT_CATEGORY_INACTIVE, inactiveCategories);
                intent.putExtra(INTENT_CATEGORY_INACTIVE_ADD_NEW, true);
                LocalBroadcastManager.getInstance(clazz).sendBroadcast(intent);
            } else {
                Utils.showError(rootView, "Dodanie nowej kategorii zakończone niepowodzeniem");
            }
        }
    }

}
