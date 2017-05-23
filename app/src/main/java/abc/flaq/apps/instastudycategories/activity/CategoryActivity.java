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
import abc.flaq.apps.instastudycategories.adapter.CategoryTabAdapter;
import abc.flaq.apps.instastudycategories.api.Api;
import abc.flaq.apps.instastudycategories.general.Session;
import abc.flaq.apps.instastudycategories.helper.Utils;
import abc.flaq.apps.instastudycategories.pojo.Category;

import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_ACTIVE;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_ACTIVE_END;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_ACTIVE_START;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_INACTIVE;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_INACTIVE_END;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_INACTIVE_START;
import static abc.flaq.apps.instastudycategories.helper.Constants.TAB_ACTIVE;
import static abc.flaq.apps.instastudycategories.helper.Constants.TAB_INACTIVE;

public class CategoryActivity extends SessionActivity {

    private AppCompatActivity clazz = this;
    private ViewPager pager;
    private TabLayout tabs;

    private ArrayList<Category> categories = new ArrayList<>();
    private Boolean isApiWorking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_container);

        pager = (ViewPager) findViewById(R.id.category_pager);
        pager.setAdapter(new CategoryTabAdapter(getSupportFragmentManager()));

        tabs = (TabLayout) findViewById(R.id.category_tabs);
        tabs.setupWithViewPager(pager);

        if (Utils.isEmpty(Session.getInstance().getCategories())) {
            new ProcessCategories().execute();
        } else {
            categories = Session.getInstance().getCategories();

            // Hack to wait for fragments to init
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    endProcessCategoryFragment();
                }
            }, 50);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        // Update categories when going back from User activity and user has joined or left the subcategory in category
        if (Session.getInstance().isCategoryChanged()) {
            new ProcessCategories().execute();
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
        final List<String> categoriesNames = Utils.getCategoriesNames(clazz);

        new MaterialDialog.Builder(clazz)
                .title(R.string.new_category)
                .content(R.string.suggest_new_category)
                .positiveText(R.string.suggest)
                .neutralText(R.string.cancel)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .inputRangeRes(1, 50, R.color.colorError)
                .input(getString(R.string.type_name), null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (Utils.isNotEmpty(input) && Utils.isNotEmpty(input.toString()) &&
                                categoriesNames.contains(input.toString().trim().toLowerCase())) {
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                        }
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (Utils.isNotEmpty(dialog.getInputEditText()) && Utils.isNotEmpty(dialog.getInputEditText().getText())) {
                            new ProcessAddCategory().execute(dialog.getInputEditText().getText().toString().trim().toLowerCase());
                        }
                        dialog.dismiss();
                    }
                })
                .alwaysCallInputCallback()
                .show();
    }

    private void handleConnectionError(String message) {
        isApiWorking = true;
        Utils.showConnectionError(tabs, message);
    }

    private void startCategoryFragment() {
        Intent intent = new Intent(INTENT_CATEGORY);
        intent.putExtra(INTENT_CATEGORY_ACTIVE_START, true);
        intent.putExtra(INTENT_CATEGORY_INACTIVE_START, true);
        LocalBroadcastManager.getInstance(clazz).sendBroadcast(intent);
    }
    private void endProcessCategoryFragment() {
        if (categories.size() == 0) {
            Utils.showConnectionError(tabs, getString(R.string.error_categories_not_found));
        } else {
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

        if (activeCategories.size() == 0 && inactiveCategories.size() > 0) {
            pager.setCurrentItem(TAB_INACTIVE, true);
        }

        Intent activeIntent = new Intent(INTENT_CATEGORY);
        activeIntent.putParcelableArrayListExtra(INTENT_CATEGORY_ACTIVE, activeCategories);
        activeIntent.putExtra(INTENT_CATEGORY_ACTIVE_END, true);
        LocalBroadcastManager.getInstance(clazz).sendBroadcast(activeIntent);
        Intent inactiveIntent = new Intent(INTENT_CATEGORY);
        inactiveIntent.putParcelableArrayListExtra(INTENT_CATEGORY_INACTIVE, inactiveCategories);
        inactiveIntent.putExtra(INTENT_CATEGORY_INACTIVE_END, true);
        LocalBroadcastManager.getInstance(clazz).sendBroadcast(inactiveIntent);
    }

    private class ProcessCategories extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isApiWorking = true;

            // Don't show preloader when going back from User activity
            if (!Session.getInstance().isCategoryChanged()) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startCategoryFragment();
                    }
                }, 50);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                categories = Api.getAllCategories(true);
            } catch (JSONException e) {
                handleConnectionError(getString(R.string.error_categories_load));
                Utils.logError(clazz, "JSONException: " + e.getMessage());
            } catch (IOException e) {
                handleConnectionError(getString(R.string.error_categories_load));
                Utils.logError(clazz, "IOException: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            isApiWorking = false;

            Session.getInstance().setCategoryChanged(false);
            endProcessCategoryFragment();
        }
    }
    private class ProcessAddCategory extends AsyncTask<String, Void, Boolean> {
        //TODO!!: ogranicz mozliwosc do x-razy
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pager.setCurrentItem(TAB_ACTIVE, true);
            isApiWorking = true;

            startCategoryFragment();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String categoryName = params[0];
            Boolean result = Boolean.FALSE;
            try {
                Category newCategory = Api.addCategory(categoryName);
                if (Utils.isNotEmpty(newCategory)) {
                    result = Boolean.TRUE;
                }
            } catch (JSONException e) {
                handleConnectionError(getString(R.string.error_category_add));
                Utils.logError(clazz, "JSONException: " + e.getMessage());
            } catch (IOException e) {
                handleConnectionError(getString(R.string.error_category_add));
                Utils.logError(clazz, "IOException: " + e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            isApiWorking = false;

            if (result) {
                Utils.showInfoDismiss(tabs,
                        getString(R.string.category_add_success_1) + "\n" +
                                getString(R.string.category_add_success_2)
                );

                // Don't show preloa der, because categories are loaded just after
                Session.getInstance().setCategoryChanged(true);
                new ProcessCategories().execute();
            } else {
                Utils.showConnectionError(tabs, getString(R.string.error_category_add));
            }
        }
    }

}
