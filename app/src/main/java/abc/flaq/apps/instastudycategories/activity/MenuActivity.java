package abc.flaq.apps.instastudycategories.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.crystal.crystalpreloaders.widgets.CrystalPreloader;

import org.json.JSONException;

import java.io.IOException;
import java.net.URISyntaxException;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.pojo.User;
import abc.flaq.apps.instastudycategories.pojo.instagram.InstagramAccessToken;
import abc.flaq.apps.instastudycategories.pojo.instagram.InstagramUser;
import abc.flaq.apps.instastudycategories.utils.Api;
import abc.flaq.apps.instastudycategories.utils.Constants;
import abc.flaq.apps.instastudycategories.utils.GeneralUtils;
import abc.flaq.apps.instastudycategories.utils.InstagramApi;
import abc.flaq.apps.instastudycategories.utils.InstagramUtils;

import static abc.flaq.apps.instastudycategories.utils.Constants.INSTAGRAM_ENDPOINT_USER_SELF;
import static abc.flaq.apps.instastudycategories.utils.Constants.INSTAGRAM_REDIRECT_URL;
import static abc.flaq.apps.instastudycategories.utils.Constants.SETTINGS_ACCESS_TOKEN;
import static abc.flaq.apps.instastudycategories.utils.Constants.SETTINGS_NAME;

// FIXME: make it singleton!
public class MenuActivity extends AppCompatActivity {

    private final Activity clazz = this;
    private SharedPreferences settings;

    private Dialog instagramDialog;
    private User user;
    private Menu mainMenu;
    private String accessToken;

    public SharedPreferences getSettings() {
        return settings;
    }
    public User getUser() {
        return user;
    }
    public Menu getMainMenu() {
        return mainMenu;
    }
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);
        // FIXME: maybe call this once in CategoryActivity and pass User data in intent
        accessToken = settings.getString(SETTINGS_ACCESS_TOKEN, null);
        GeneralUtils.logInfo(clazz, "Settings access token: " + accessToken);
        if (GeneralUtils.isNotEmpty(accessToken)) {
            new ProcessGetUser().execute();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        mainMenu = menu;
        handleLogin(accessToken);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                // not available from here
                break;
            case R.id.menu_join:
                // not available from here
                break;
            case R.id.menu_info:
                if (GeneralUtils.isEmpty(user)) {
                    GeneralUtils.logDebug(clazz, "Instagram user data is empty");
                    new ProcessGetUser().execute();
                } else {
                    GeneralUtils.showMessage(clazz, user.toString());
                }
                break;
            case R.id.menu_login:
                if (GeneralUtils.isNotEmpty(accessToken)) {
                    GeneralUtils.logDebug(clazz, "Access token is not empty, but login icon is available");
                    handleLogin(accessToken);
                } else {
                    GeneralUtils.showMessage(clazz, "Logging in...");
                    try {
                        String instagramAuthUrl = InstagramApi.getAuthUrl(Constants.INSTAGRAM_SCOPES.public_content);
                        createInstagramDialog(instagramAuthUrl);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void createInstagramDialog(String url) {
        instagramDialog = new Dialog(clazz);
        // FIXME: better preloader
        final CrystalPreloader preloader = new CrystalPreloader(clazz);
        WebView webView = new WebView(clazz);
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (GeneralUtils.isNotEmpty(url)) {
                    if (!url.startsWith(INSTAGRAM_REDIRECT_URL)) {
                        view.loadUrl(url);
                        preloader.setVisibility(View.GONE);
                    } else {
                        String code = InstagramApi.getCodeFromUrl(clazz, url);
                        if (GeneralUtils.isEmpty(code)) {
                            GeneralUtils.afterError(clazz, "Instagram code is empty");
                            instagramDialog.dismiss();
                        } else {
                            GeneralUtils.logInfo(clazz, "Collected instagram code: " + code);
                            preloader.setVisibility(View.VISIBLE);
                            new ProcessGetAccessToken().execute(code);
                        }
                    }
                }
                return true;
            }
        });

        instagramDialog.setContentView(webView);
        instagramDialog.addContentView(preloader, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        instagramDialog.show();
    }

    private void handleLogin(String accessToken) {
        Boolean isAuthenticated = GeneralUtils.isNotEmpty(accessToken);
        mainMenu.findItem(R.id.menu_login).setVisible(!isAuthenticated);
        mainMenu.findItem(R.id.menu_add).setVisible(isAuthenticated);
        mainMenu.findItem(R.id.menu_join).setVisible(isAuthenticated);
        mainMenu.findItem(R.id.menu_info).setVisible(isAuthenticated);
    }

    private void saveAccessToken(String accessToken) {
        if (GeneralUtils.isEmpty(settings)) {
            settings = getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);
        }
        settings.edit()
                .putString(SETTINGS_ACCESS_TOKEN, accessToken)
                .apply();
    }

    public void resetAuthentication() {
        GeneralUtils.afterError(clazz, "Resetting authentication");
        accessToken = null;
        handleLogin(null);
    }

    private class ProcessGetAccessToken extends AsyncTask<String, Void, InstagramAccessToken> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected InstagramAccessToken doInBackground(String... params) {
            String code = params[0];
            InstagramAccessToken instagramToken = null;
            try {
                instagramToken = InstagramApi.getAccessTokenByCode(code);
            } catch (IOException e) {
                GeneralUtils.logError(clazz, "IOException: " + e.toString());
            }
            return instagramToken;
        }

        @Override
        protected void onPostExecute(InstagramAccessToken result) {
            super.onPostExecute(result);

            if (GeneralUtils.isNotEmpty(result)) {
                if (result.isError() || GeneralUtils.isEmpty(result.getAccessToken())) {
                    GeneralUtils.afterError(clazz, result.toString());
                    user = null;
                    accessToken = null;
                    handleLogin(null);
                } else {
                    GeneralUtils.logInfo(clazz, "Collected instagram access token: " + result.getAccessToken());
                    accessToken = result.getAccessToken();
                    new ProcessGetUser().execute();
                }
                if (GeneralUtils.isNotEmpty(instagramDialog)) {
                    instagramDialog.dismiss();
                }
            }
        }
    }

    public class ProcessGetUser extends AsyncTask<Void, Void, InstagramUser> {
        private Boolean isNewUser = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected InstagramUser doInBackground(Void... params) {
            InstagramUser instagramUser = null;
            try {
                instagramUser = InstagramApi.getDataToClass(InstagramUser.class, INSTAGRAM_ENDPOINT_USER_SELF + accessToken);
                if (GeneralUtils.isNotEmpty(instagramUser) &&
                        GeneralUtils.isNotEmpty(instagramUser.getData())) {
                    user = Api.getUserByInstagramId(instagramUser.getData().getId());
                    if (GeneralUtils.isEmpty(user)) {
                        isNewUser = true;
                        user = InstagramUtils.instagramUserToUser(instagramUser.getData());
                    } else {
                        GeneralUtils.logInfo(clazz, "User already exists: " + user);
                    }
                }
            } catch (IOException e) {
                GeneralUtils.logError(clazz, "IOException: " + e.toString());
            } catch (JSONException e) {
                GeneralUtils.logError(clazz, "JSONException: " + e.toString());
            }
            return instagramUser;
        }

        @Override
        protected void onPostExecute(InstagramUser result) {
            super.onPostExecute(result);
            if (GeneralUtils.isNotEmpty(result) &&
                    GeneralUtils.isNotEmpty(result.getMeta()) &&
                    GeneralUtils.isNotEmpty(result.getData())) {
                if (result.getMeta().isError()) {
                    GeneralUtils.afterError(clazz, result.toString());
                } else {
                    GeneralUtils.logInfo(clazz, "Collected instagram user data: " + result.toString());
                    if (isNewUser) {
                        new ProcessAddUser().execute();
                    }
                }
            }
        }
    }

    private class ProcessAddUser extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean result = Boolean.FALSE;
            try {
                Thread.sleep(1000); // FIXME: showing preloader, REMOVE!
                result = Api.addUser(user);
            } catch (InterruptedException e) {
                GeneralUtils.logError(clazz, "InterruptedException: " + e.toString());
            } catch (JSONException e) {
                GeneralUtils.logError(clazz, "JSONException: " + e.toString());
            } catch (IOException e) {
                GeneralUtils.logError(clazz, "IOException: " + e.toString());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                handleLogin(accessToken);
                saveAccessToken(accessToken);
                GeneralUtils.showMessage(clazz, "Logged in");
            }
        }
    }

    private class ProcessDeleteUser extends AsyncTask<User, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(User... params) {
            User user = params[0];
            Boolean result = Boolean.FALSE;
            try {
                result = Api.deleteUser(user);
            } catch (IOException e) {
                GeneralUtils.logError(clazz, "IOException: " + e.toString());
            } catch (JSONException e) {
                GeneralUtils.logError(clazz, "JSONException: " + e.toString());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            GeneralUtils.logDebug(clazz, "User deleted: " + result);
        }
    }

}
