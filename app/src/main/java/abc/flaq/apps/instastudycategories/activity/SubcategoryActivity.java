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
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import abc.flaq.apps.instastudycategories.BuildConfig;
import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.adapter.SubcategoryTabAdapter;
import abc.flaq.apps.instastudycategories.api.Api;
import abc.flaq.apps.instastudycategories.design.Decorator;
import abc.flaq.apps.instastudycategories.general.Session;
import abc.flaq.apps.instastudycategories.helper.Utils;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;

import static abc.flaq.apps.instastudycategories.helper.Constants.ADMOB_TEST_DEVICE_ID;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_FOREIGN_ID;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_NAME;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_ACTIVE;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_ACTIVE_END;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_ACTIVE_START;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_INACTIVE;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_INACTIVE_END;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_INACTIVE_START;
import static abc.flaq.apps.instastudycategories.helper.Constants.SETTINGS_SUGGEST_SUBCATEGORY;
import static abc.flaq.apps.instastudycategories.helper.Constants.SETTINGS_SUGGEST_SUBCATEGORY_DATE;
import static abc.flaq.apps.instastudycategories.helper.Constants.SUGGEST_MAX_COUNT;
import static abc.flaq.apps.instastudycategories.helper.Constants.TAB_INACTIVE;

public class SubcategoryActivity extends SessionActivity {

    private final AppCompatActivity clazz = this;
    private ViewPager pager;
    private TabLayout tabs;
    private Handler handler = new Handler();
    private AdView adView;

    private List<Subcategory> subcategories = new ArrayList<>();
    private String categoryForeignId;
    private Boolean isApiWorking = false;
    private Integer addSubcategoryCounter;
    private Long addSubcategoryDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subcategory_container);

        Toolbar toolbar = (Toolbar) findViewById(R.id.subcategory_toolbar);
        setSupportActionBar(toolbar);

        pager = (ViewPager) findViewById(R.id.subcategory_pager);
        pager.setAdapter(new SubcategoryTabAdapter(getSupportFragmentManager(), clazz));

        tabs = (TabLayout) findViewById(R.id.subcategory_tabs);
        tabs.setupWithViewPager(pager);

        adView = (AdView) findViewById(R.id.subcategory_adView);
        AdRequest adRequest = (
                BuildConfig.IS_DEBUG ?
                new AdRequest.Builder().addTestDevice(ADMOB_TEST_DEVICE_ID).build() :
                new AdRequest.Builder().build()
        );
        adView.loadAd(adRequest);

        Intent intent = getIntent();
        categoryForeignId = intent.getStringExtra(INTENT_CATEGORY_FOREIGN_ID);

        addSubcategoryCounter = Session.getInstance().getSettings().getInt(SETTINGS_SUGGEST_SUBCATEGORY, SUGGEST_MAX_COUNT);
        addSubcategoryDate = Session.getInstance().getSettings().getLong(SETTINGS_SUGGEST_SUBCATEGORY_DATE, 0);
        Long minsSinceLastSuggestion = (new Date().getTime() / 60000) - addSubcategoryDate;
        if (minsSinceLastSuggestion >= 1440) {
            addSubcategoryCounter = SUGGEST_MAX_COUNT;
            Session.getInstance().getSettings().edit()
                    .putInt(SETTINGS_SUGGEST_SUBCATEGORY, addSubcategoryCounter)
                    .apply();
        } else {
            addSubcategoryCounter = 0;
        }

        if (Utils.isEmpty(categoryForeignId)) {
            handleConnectionError("Empty 'categoryForeignId'", getString(R.string.error_subcategories_load));
        } else {
            String categoryName = intent.getStringExtra(INTENT_CATEGORY_NAME);
            Decorator.setActionBarTitle(clazz, Utils.getCategoryString(clazz, categoryName), null);
            Session.getInstance().setCategoryName(categoryName);

            if (Utils.isEmpty(Session.getInstance().getSubcategories(categoryForeignId))) {
                new ProcessSubcategories().execute();
            } else {
                subcategories = Session.getInstance().getSubcategories(categoryForeignId);

                // Hack to wait for fragments to init
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        endProcessSubcategoryFragment();
                    }
                }, 10);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        adView.resume();
        invalidateOptionsMenu();
        // Load subcategories when going back from User activity and user has joined or left the subcategory
        if (Session.getInstance().isSubcategoryChanged()) {
            new ProcessSubcategories().execute();
        }
    }

    @Override
    protected void onPause() {
        adView.pause();
        super.onPause();
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
                if (addSubcategoryCounter > 0) {
                    showSuggestSubcategoryDialog();
                } else {
                    Utils.showInfo(tabs, getString(R.string.cant_add_subcategory));
                }
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
                .title(R.string.new_subcategory)
                .content(R.string.suggest_new_subcategory)
                .positiveText(R.string.suggest)
                .neutralText(R.string.cancel)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .inputRangeRes(1, 50, R.color.colorError)
                .input(getString(R.string.type_name), null, new MaterialDialog.InputCallback() {
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
                            new ProcessAddSubcategory().execute(dialog.getInputEditText().getText().toString().trim().toLowerCase());
                        }
                        dialog.dismiss();
                    }
                })
                .alwaysCallInputCallback()
                .show();
    }

    private void handleConnectionError(String logMessage, String showMessage) {
        //isApiWorking = true;
        Utils.showConnectionError(tabs, logMessage, showMessage);
    }

    private void startSubcategoryFragment() {
        Intent intent = new Intent(INTENT_SUBCATEGORY);
        intent.putExtra(INTENT_SUBCATEGORY_ACTIVE_START, true);
        intent.putExtra(INTENT_SUBCATEGORY_INACTIVE_START, true);
        LocalBroadcastManager.getInstance(clazz).sendBroadcast(intent);
    }
    private void endProcessSubcategoryFragment() {
        if (subcategories.size() == 0) {
            Utils.showInfo(tabs, getString(R.string.error_subcategories_not_found));
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
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pager.setCurrentItem(TAB_INACTIVE, true);
                }
            }, 100);
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
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startSubcategoryFragment();
                    }
                }, 10);
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
                handleConnectionError("JSONException: " + e.getMessage(), getString(R.string.error_subcategories_load));
            } catch (IOException e) {
                handleConnectionError("IOException: " + e.getMessage(), getString(R.string.error_subcategories_load));
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
        Subcategory newSubcategory;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                handleConnectionError("JSONException: " + e.getMessage(), getString(R.string.error_subcategory_add));
            } catch (IOException e) {
                handleConnectionError("IOException: " + e.getMessage(), getString(R.string.error_subcategory_add));
            } catch (ParseException e) {
                handleConnectionError("ParseException: " + e.getMessage(), getString(R.string.error_subcategory_add));
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            isApiWorking = false;

            if (result) {
                Utils.showInfo(tabs,
                        getString(R.string.subcategory_add_success_short)
                );

                pager.setCurrentItem(TAB_INACTIVE, true);

                addSubcategoryCounter--;
                addSubcategoryDate = new Date().getTime() / 60000;
                Session.getInstance().getSettings().edit()
                        .putInt(SETTINGS_SUGGEST_SUBCATEGORY, addSubcategoryCounter)
                        .putLong(SETTINGS_SUGGEST_SUBCATEGORY_DATE, addSubcategoryDate)
                        .apply();

                // Don't show preloader, because subcategories are loaded just after
                Session.getInstance().setSubcategoryChanged(true);
                Session.getInstance().setCategoryChanged(true);
                new ProcessSubcategories().execute();
            } else {
                handleConnectionError("ProcessAddSubcategory - empty result", getString(R.string.error_subcategory_add));
            }
        }
    }

}
