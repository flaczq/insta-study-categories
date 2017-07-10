package abc.flaq.apps.instastudycategories.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import abc.flaq.apps.instastudycategories.BuildConfig;
import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.adapter.CategoryTabAdapter;
import abc.flaq.apps.instastudycategories.api.Api;
import abc.flaq.apps.instastudycategories.general.Session;
import abc.flaq.apps.instastudycategories.helper.Utils;
import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.pojo.Info;

import static abc.flaq.apps.instastudycategories.helper.Constants.ADMOB_TEST_DEVICE_ID;
import static abc.flaq.apps.instastudycategories.helper.Constants.DATE_FORMAT;
import static abc.flaq.apps.instastudycategories.helper.Constants.INSTAGRAM_URL;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_ACTIVE;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_ACTIVE_END;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_ACTIVE_INFO;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_ACTIVE_START;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_INACTIVE;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_INACTIVE_END;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_INACTIVE_INFO;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_INACTIVE_START;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SESSION;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SESSION_LOGIN;
import static abc.flaq.apps.instastudycategories.helper.Constants.SETTINGS_SUGGEST_CATEGORY;
import static abc.flaq.apps.instastudycategories.helper.Constants.SETTINGS_SUGGEST_CATEGORY_DATE;
import static abc.flaq.apps.instastudycategories.helper.Constants.SUGGEST_MAX_COUNT;
import static abc.flaq.apps.instastudycategories.helper.Constants.TAB_INACTIVE;

public class CategoryActivity extends SessionActivity {

    private AppCompatActivity clazz = this;
    private Toolbar toolbar;
    private ViewPager pager;
    private TabLayout tabs;
    private Drawer drawer;
    private AccountHeader drawerHeader;
    private IProfile drawerProfile;
    private Handler handler = new Handler();
    private AdView adView;

    private ArrayList<Category> categories = new ArrayList<>();
    private ArrayList<Info> infos = new ArrayList<>();
    private Boolean isApiWorking = false;
    private Integer addCategoryCounter;
    private Long addCategoryDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_container);

        toolbar = (Toolbar) findViewById(R.id.category_toolbar);
        setSupportActionBar(toolbar);

        pager = (ViewPager) findViewById(R.id.category_pager);
        pager.setAdapter(new CategoryTabAdapter(getSupportFragmentManager(), clazz));

        tabs = (TabLayout) findViewById(R.id.category_tabs);
        tabs.setupWithViewPager(pager);

        adView = (AdView) findViewById(R.id.category_adView);
        AdRequest adRequest = (
                BuildConfig.IS_DEBUG ?
                        new AdRequest.Builder().addTestDevice(ADMOB_TEST_DEVICE_ID).build() :
                        new AdRequest.Builder().build()
        );
        adView.loadAd(adRequest);

        addCategoryCounter = Session.getInstance().getSettings().getInt(SETTINGS_SUGGEST_CATEGORY, SUGGEST_MAX_COUNT);
        addCategoryDate = Session.getInstance().getSettings().getLong(SETTINGS_SUGGEST_CATEGORY_DATE, 0);
        Long minsSinceLastSuggestion = (new Date().getTime() / 60000) - addCategoryDate;
        if (minsSinceLastSuggestion >= 1440) {
            addCategoryCounter = SUGGEST_MAX_COUNT;
            Session.getInstance().getSettings().edit()
                    .putInt(SETTINGS_SUGGEST_CATEGORY, addCategoryCounter)
                    .apply();
        } else {
            addCategoryCounter = 0;
        }

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
            }, 10);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        adView.resume();
        invalidateOptionsMenu();
        setBroadcastReceivers();
        // Update categories when going back from User activity and user has joined or left the subcategory in category
        if (Session.getInstance().isCategoryChanged()) {
            if (Utils.isEmpty(Session.getInstance().getUser())) {
                updateDrawerProfile(false);
            } else {
                updateDrawerProfile(true);
            }
            new ProcessCategories().execute();
        }
    }
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(clazz).unregisterReceiver(receiver);
        adView.pause();
        super.onPause();
    }
    @Override
    public void onBackPressed() {
        if (Utils.isNotEmpty(drawer) && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            super.onBackPressed();
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
                if (addCategoryCounter > 0) {
                    showSuggestCategoryDialog();
                } else {
                    Utils.showInfo(tabs, getString(R.string.cant_add_category));
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

    private void updateDrawerProfile(boolean loggedIn) {
        if (Utils.isNotEmpty(drawerProfile) && Utils.isNotEmpty(drawerHeader)) {
            if (loggedIn) {
                drawerProfile.withName(Session.getInstance().getUser().getUsername());
                drawerProfile.withEmail(Utils.formatDate(Session.getInstance().getUser().getCreated(), DATE_FORMAT));
                drawerProfile.withIcon(Session.getInstance().getUserProfilePic().getDrawable());
            } else {
                drawerProfile.withName("Log in");
                drawerProfile.withEmail("to see your profile");
                drawerProfile.withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.placeholder_profile_pic_72, null));
            }
            drawerHeader.updateProfile(drawerProfile);
        }
    }

    private void initDrawer() {
        final String name, info, profilePic;
        if (Utils.isEmpty(Session.getInstance().getUser())) {
            name = getString(R.string.drawer_login);
            info = getString(R.string.drawer_login_2);
            profilePic = "";
        } else {
            name = Session.getInstance().getUser().getUsername();
            info = Utils.formatDate(Session.getInstance().getUser().getCreated(), DATE_FORMAT);
            profilePic = Session.getInstance().getUser().getProfilePicUrl();
        }

        drawerProfile = new ProfileDrawerItem()
                .withIdentifier(123)
                .withName(name)
                .withEmail(info);
        if (Utils.isEmpty(profilePic)) {
            drawerProfile.withIcon(R.drawable.placeholder_profile_pic_72);
        } else {
            drawerProfile.withIcon(profilePic);
        }

        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                UrlImageViewHelper.setUrlDrawable(imageView, profilePic, placeholder);
            }

            @Override
            public Drawable placeholder(Context ctx) {
                return ResourcesCompat.getDrawable(getResources(), R.drawable.placeholder_profile_pic_72, null);
            }
        });

        drawerHeader = new AccountHeaderBuilder()
                .withActivity(clazz)
                .withHeaderBackground(R.drawable.drawer_background)
                .withSelectionListEnabledForSingleProfile(false)
                .withAlternativeProfileHeaderSwitching(false)
                .withOnAccountHeaderProfileImageListener(new AccountHeader.OnAccountHeaderProfileImageListener() {
                    @Override
                    public boolean onProfileImageClick(View view, IProfile profile, boolean current) {
                        if (Utils.isEmpty(Session.getInstance().getUser())) {
                            showLoginDialog();
                            return false;
                        }

                        Utils.showQuickInfo(tabs, getString(R.string.ig_profile_open) + Session.getInstance().getUser().getUsername() + "...");
                        Intent nextIntent = Utils.getInstagramIntent(Session.getInstance().getUser().getUsername());
                        if (Utils.isIntentAvailable(clazz, nextIntent)) {
                            Utils.logDebug(clazz, "Instagram intent is available");
                            clazz.startActivity(nextIntent);
                        } else {
                            Utils.logDebug(clazz, "Instagram intent is NOT available");
                            clazz.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL + Session.getInstance().getUser().getUsername())));
                        }
                        return false;
                    }

                    @Override
                    public boolean onProfileImageLongClick(View view, IProfile profile, boolean current) {
                        return false;
                    }
                })
                .addProfiles(drawerProfile)
                .build();
        PrimaryDrawerItem itemBack = new PrimaryDrawerItem().withIdentifier(1).withTag("back").withIcon(FontAwesome.Icon.faw_home).withName(R.string.drawer_back);
        PrimaryDrawerItem itemProfile = new PrimaryDrawerItem().withIdentifier(2).withTag("profile").withIcon(FontAwesome.Icon.faw_info).withName(R.string.drawer_profile).withSelectable(false);
        PrimaryDrawerItem itemBuy = new PrimaryDrawerItem().withIdentifier(3).withTag("buy").withIcon(FontAwesome.Icon.faw_money).withName(R.string.drawer_buy).withSelectable(false);
        PrimaryDrawerItem itemHelp = new PrimaryDrawerItem().withIdentifier(4).withTag("help").withIcon(FontAwesome.Icon.faw_question).withName(R.string.drawer_help).withSelectable(false);
        PrimaryDrawerItem itemAbout = new PrimaryDrawerItem().withIdentifier(5).withTag("about").withIcon(FontAwesome.Icon.faw_bullhorn).withName(R.string.drawer_about).withSelectable(false);

        drawer = new DrawerBuilder()
                .withActivity(clazz)
                .withToolbar(toolbar)
                .withActionBarDrawerToggleAnimated(true)
                .withAccountHeader(drawerHeader)
                .addDrawerItems(
                        itemBack,
                        itemProfile,
                        itemBuy,
                        new DividerDrawerItem(),
                        itemHelp,
                        itemAbout
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch ((String)drawerItem.getTag()) {
                            case "back":
                                // dismiss
                                break;
                            case "profile":
                                if (Utils.isEmpty(Session.getInstance().getUser())) {
                                    showLoginDialog();
                                } else {
                                    showInfoDialog();
                                }
                                break;
                            case "buy":
                                Utils.showQuickInfo(view, getString(R.string.drawer_buy_not_yet));
                                return true;
                            case "help":
                                IconicsDrawable helpIcon = new IconicsDrawable(clazz).icon(FontAwesome.Icon.faw_question).colorRes(R.color.primary).actionBar();
                                new MaterialDialog.Builder(clazz)
                                        .icon(helpIcon)
                                        .title(R.string.drawer_help)
                                        .content(R.string.drawer_help_info)
                                        .typeface("Roboto-Medium.ttf", "RobotoCondensed-Light.ttf")
                                        .positiveText(R.string.back)
                                        .neutralText(R.string.send_email)
                                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                Intent emailIntent = Utils.getEmailIntent("StudyGram");
                                                startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)));
                                                dialog.dismiss();
                                            }
                                        })
                                        .show();
                                break;
                            case "about":
                                IconicsDrawable aboutIcon = new IconicsDrawable(clazz).icon(FontAwesome.Icon.faw_bullhorn).colorRes(R.color.primary).actionBar();
                                new MaterialDialog.Builder(clazz)
                                        .icon(aboutIcon)
                                        .title(R.string.drawer_about)
                                        .content(
                                                getString(R.string.drawer_about_dev_info) + "\n\n" +
                                                getString(R.string.drawer_about_libraries_info) + "\n\n" +
                                                getString(R.string.drawer_about_info_apache_jackson) + "\n\n" +
                                                getString(R.string.drawer_about_info_apache_grid) + "\n\n" +
                                                getString(R.string.drawer_about_info_apache_preloader) + "\n\n" +
                                                getString(R.string.drawer_about_info_apache_image_helper) + "\n\n" +
                                                getString(R.string.drawer_about_info_apache_circle_image) + "\n\n" +
                                                getString(R.string.drawer_about_info_apache_material_drawer) + "\n\n" +
                                                getString(R.string.drawer_about_info_apache_iconics) + "\n\n" +
                                                getString(R.string.drawer_about_info_apache_fontawesome) + "\n\n" +
                                                getString(R.string.drawer_about_apache_license) + "\n" +
                                                getString(R.string.drawer_about_apache_license_info) + "\n\n" +
                                                getString(R.string.drawer_about_info_mit_java_websocket) + "\n\n" +
                                                getString(R.string.drawer_about_info_mit_material_dialog) + "\n\n" +
                                                getString(R.string.drawer_about_mit_license) + "\n" +
                                                getString(R.string.drawer_about_mit_license_info)
                                        )
                                        .typeface("Roboto-Medium.ttf", "RobotoCondensed-Light.ttf")
                                        .positiveText(R.string.back)
                                        .show();
                                break;
                        }
                        return false;
                    }
                })
                .build();
    }

    private void setBroadcastReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_SESSION);
        LocalBroadcastManager.getInstance(clazz).registerReceiver(receiver, filter);
    }
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(INTENT_SESSION_LOGIN)
                    && Utils.isNotEmpty(drawerHeader)
                    && Utils.isNotEmpty(Session.getInstance().getUser())) {
                updateDrawerProfile(true);
                drawerHeader.updateProfile(drawerProfile);
            }
        }
    };

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
    private void showInfoDialog() {
        MaterialDialog.Builder infoDialogBuilder = new MaterialDialog.Builder(clazz)
                .title(Session.getInstance().getUser().getUsername())
                .content(Session.getInstance().getUser().getInfoContent(clazz))
                .positiveText(R.string.back)
                .neutralText(R.string.logout)
                .negativeText(R.string.delete_account)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Utils.showQuickInfo(tabs, getString(R.string.logged_out));
                        updateDrawerProfile(false);
                        logOut();
                        dialog.hide();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                        Snackbar.make(Utils.findSnackbarView(tabs), R.string.remove_account_info, Snackbar.LENGTH_LONG)
                                .setActionTextColor(ContextCompat.getColor(clazz, R.color.colorError))
                                .setAction(R.string.remove, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                        new ProcessDeleteUser().execute();
                                    }
                                }).show();
                        dialog.hide();
                    }
                });

        if (Utils.isEmpty(Session.getInstance().getUser().getProfilePicUrl())) {
            infoDialogBuilder.iconRes(R.drawable.placeholder_profile_pic_72);
        } else {
            infoDialogBuilder.icon(Session.getInstance().getUserProfilePic().getDrawable());
        }

        final MaterialDialog infoDialog = infoDialogBuilder.build();
        infoDialog.getIconView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.showQuickInfo(tabs, getString(R.string.ig_profile_open) + Session.getInstance().getUser().getUsername() + "...");
                Intent nextIntent = Utils.getInstagramIntent(Session.getInstance().getUser().getUsername());
                if (Utils.isIntentAvailable(clazz, nextIntent)) {
                    Utils.logDebug(clazz, "Instagram intent is available");
                    clazz.startActivity(nextIntent);
                } else {
                    Utils.logDebug(clazz, "Instagram intent is NOT available");
                    clazz.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL + Session.getInstance().getUser().getUsername())));
                }
                infoDialog.hide();
            }
        });
        infoDialog.getTitleView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.showQuickInfo(tabs, getString(R.string.ig_profile_open) + Session.getInstance().getUser().getUsername() + "...");
                Intent nextIntent = Utils.getInstagramIntent(Session.getInstance().getUser().getUsername());
                if (Utils.isIntentAvailable(clazz, nextIntent)) {
                    Utils.logDebug(clazz, "Instagram intent is available");
                    clazz.startActivity(nextIntent);
                } else {
                    Utils.logDebug(clazz, "Instagram intent is NOT available");
                    clazz.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL + Session.getInstance().getUser().getUsername())));
                }
                infoDialog.hide();
            }
        });
        infoDialog.getActionButton(DialogAction.NEGATIVE).setMaxLines(2);
        infoDialog.getActionButton(DialogAction.NEGATIVE).setPadding(0, 0, 0, 0);
        infoDialog.show();
    }

    private void handleConnectionError(String logMessage, String showMessage) {
        //isApiWorking = true;
        Utils.showConnectionError(tabs, logMessage, showMessage);
    }

    private void startCategoryFragment() {
        Intent intent = new Intent(INTENT_CATEGORY);
        intent.putExtra(INTENT_CATEGORY_ACTIVE_START, true);
        intent.putExtra(INTENT_CATEGORY_INACTIVE_START, true);
        LocalBroadcastManager.getInstance(clazz).sendBroadcast(intent);
    }
    private void endProcessCategoryFragment() {
        if (categories.size() == 0) {
            handleConnectionError("Empty categories", getString(R.string.error_categories_not_found));
        } else if (infos.size() == 0) {
            new ProcessInfos().execute();
            //Session.getInstance().setCategories(categories);
            // Get first (not "all") category's size
            //Session.getInstance().setMaxGridSize(categories.get(1).getSubcategoriesSize());
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
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pager.setCurrentItem(TAB_INACTIVE, true);
                }
            }, 100);
        }

        Intent activeIntent = new Intent(INTENT_CATEGORY);
        activeIntent.putParcelableArrayListExtra(INTENT_CATEGORY_ACTIVE, activeCategories);
        activeIntent.putExtra(INTENT_CATEGORY_ACTIVE_END, true);
        LocalBroadcastManager.getInstance(clazz).sendBroadcast(activeIntent);
        Intent inactiveIntent = new Intent(INTENT_CATEGORY);
        inactiveIntent.putParcelableArrayListExtra(INTENT_CATEGORY_INACTIVE, inactiveCategories);
        inactiveIntent.putExtra(INTENT_CATEGORY_INACTIVE_END, true);
        LocalBroadcastManager.getInstance(clazz).sendBroadcast(inactiveIntent);

        initDrawer();
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
                }, 10);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                categories = Api.getAllCategories(true);
            } catch (JSONException e) {
                handleConnectionError("JSONException: " + e.getMessage(), getString(R.string.error_categories_load));
            } catch (IOException e) {
                handleConnectionError("IOException: " + e.getMessage(), getString(R.string.error_categories_load));
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
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                handleConnectionError("JSONException: " + e.getMessage(), getString(R.string.error_category_add));
            } catch (IOException e) {
                handleConnectionError("IOException: " + e.getMessage(), getString(R.string.error_category_add));
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            isApiWorking = false;

            if (result) {
                Utils.showInfo(tabs,
                        getString(R.string.category_add_success_short)
                );

                pager.setCurrentItem(TAB_INACTIVE, true);

                addCategoryCounter--;
                addCategoryDate = new Date().getTime() / 60000;
                Session.getInstance().getSettings().edit()
                        .putInt(SETTINGS_SUGGEST_CATEGORY, addCategoryCounter)
                        .putLong(SETTINGS_SUGGEST_CATEGORY_DATE, addCategoryDate)
                        .apply();


                // Don't show preloader, because categories are loaded just after
                Session.getInstance().setCategoryChanged(true);
                new ProcessCategories().execute();
            } else {
                handleConnectionError("ProcessAddCategory - empty result", getString(R.string.error_category_add));
            }
        }
    }

    private class ProcessDeleteUser extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isApiWorking = true;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean result = Boolean.FALSE;
            try {
                result = Api.removeUser(Session.getInstance().getUser());
            } catch (IOException e) {
                handleConnectionError("IOException: " + e.getMessage(), getString(R.string.error_account_remove));
            } catch (JSONException e) {
                handleConnectionError("JSONException: " + e.getMessage(), getString(R.string.error_account_remove));
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            isApiWorking = false;

            if (result) {
                Utils.showQuickInfo(tabs, getString(R.string.remove_account_success));
                //updateDrawerProfile(false);
                logOut();
                Session.getInstance().setCategoryChanged(true);
                Intent nextIntent = new Intent(clazz, MainActivity.class);
                startActivity(nextIntent);
            }
        }
    }
    private class ProcessInfos extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                infos = Api.getAllInfos(true);
            } catch (JSONException e) {
                handleConnectionError("JSONException: " + e.getMessage(), getString(R.string.error_categories_load));
            } catch (IOException e) {
                handleConnectionError("IOException: " + e.getMessage(), getString(R.string.error_categories_load));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (infos.size() > 0) {
                Session.getInstance().setNewestInfo(infos.get(0));

                Intent activeIntent = new Intent(INTENT_CATEGORY);
                activeIntent.putExtra(INTENT_CATEGORY_ACTIVE_INFO, infos.get(0));
                LocalBroadcastManager.getInstance(clazz).sendBroadcast(activeIntent);
                Intent inactiveIntent = new Intent(INTENT_CATEGORY);
                inactiveIntent.putExtra(INTENT_CATEGORY_INACTIVE_INFO, infos.get(0));
                LocalBroadcastManager.getInstance(clazz).sendBroadcast(inactiveIntent);
            }
        }
    }

}
