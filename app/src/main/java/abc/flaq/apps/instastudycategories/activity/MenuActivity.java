package abc.flaq.apps.instastudycategories.activity;

import android.app.Activity;
import android.app.Dialog;
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
import abc.flaq.apps.instastudycategories.utils.Factory;
import abc.flaq.apps.instastudycategories.utils.InstagramApi;
import abc.flaq.apps.instastudycategories.utils.Session;
import abc.flaq.apps.instastudycategories.utils.Utils;

import static abc.flaq.apps.instastudycategories.utils.Constants.INSTAGRAM_ENDPOINT_USER_SELF;
import static abc.flaq.apps.instastudycategories.utils.Constants.INSTAGRAM_REDIRECT_URL;
import static abc.flaq.apps.instastudycategories.utils.Constants.SETTINGS_ACCESS_TOKEN;

public class MenuActivity extends AppCompatActivity {

    private final Activity clazz = this;
    private Dialog instagramDialog;
    private Menu mainMenu;
    private String accessToken;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accessToken = Session.getInstance().getSettings().getString(SETTINGS_ACCESS_TOKEN, null);
        Utils.logInfo(clazz, "Session access token: " + accessToken);
        if (Utils.isEmpty(Session.getInstance().getUser()) && Utils.isNotEmpty(accessToken)) {
            new ProcessGetUser().execute();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        mainMenu = menu;
        handleLogin();
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_suggest:
                // not available from here
                break;
            case R.id.menu_join:
                // not available from here
                break;
            case R.id.menu_leave:
                // not available from here
                break;
            case R.id.menu_info:
                if (Utils.isEmpty(Session.getInstance().getUser())) {
                    Utils.logDebug(clazz, "Instagram user data is empty");
                    new ProcessGetUser().execute();
                } else {
                    Utils.showMessage(clazz, Session.getInstance().getUser().toString());
                }
                break;
            case R.id.menu_login:
                if (Utils.isNotEmpty(Session.getInstance().getUser())) {
                    Utils.logDebug(clazz, "User is not empty, but login icon is available");
                    handleLogin();
                    invalidateOptionsMenu();
                } else {
                    Utils.showMessage(clazz, "Logging in...");
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
                if (Utils.isNotEmpty(url)) {
                    if (!url.startsWith(INSTAGRAM_REDIRECT_URL)) {
                        view.loadUrl(url);
                        preloader.setVisibility(View.GONE);
                    } else {
                        String code = InstagramApi.getCodeFromUrl(clazz, url);
                        if (Utils.isEmpty(code)) {
                            Utils.afterError(clazz, "Instagram code is empty");
                            instagramDialog.dismiss();
                        } else {
                            Utils.logInfo(clazz, "Collected instagram code: " + code);
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

    private void handleLogin() {
        if (Utils.isEmpty(mainMenu)) {
            // Call onCreateOptionsMenu
            invalidateOptionsMenu();
        } else {
            Boolean isAuthenticated = Utils.isNotEmpty(Session.getInstance().getUser());
            mainMenu.findItem(R.id.menu_suggest).setVisible(isAuthenticated);
            mainMenu.findItem(R.id.menu_join).setVisible(isAuthenticated);
            mainMenu.findItem(R.id.menu_leave).setVisible(isAuthenticated);
            mainMenu.findItem(R.id.menu_info).setVisible(isAuthenticated);
            mainMenu.findItem(R.id.menu_login).setVisible(!isAuthenticated);
        }
    }

    private void saveAccessToken(String accessToken) {
        Session.getInstance().getSettings().edit()
                .putString(SETTINGS_ACCESS_TOKEN, accessToken)
                .apply();
    }
    private void removeAccessToken() {
        Session.getInstance().getSettings().edit()
                .remove(SETTINGS_ACCESS_TOKEN)
                .apply();
    }

    public void resetAuthentication() {
        Utils.afterError(clazz, "Resetting authentication");
        Session.getInstance().setUser(null);
        user = null;
        accessToken = null;
        handleLogin();
        invalidateOptionsMenu();
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
                Utils.logError(clazz, "IOException: " + e.toString());
            }
            return instagramToken;
        }

        @Override
        protected void onPostExecute(InstagramAccessToken result) {
            super.onPostExecute(result);

            if (Utils.isNotEmpty(result)) {
                if (result.isError() || Utils.isEmpty(result.getAccessToken())) {
                    Utils.afterError(clazz, result.toString());
                    Session.getInstance().setUser(null);
                    accessToken = null;
                    handleLogin();
                    invalidateOptionsMenu();
                } else {
                    Utils.logInfo(clazz, "Collected instagram access token: " + result.getAccessToken());
                    accessToken = result.getAccessToken();
                    new ProcessGetUser().execute();
                }
                if (Utils.isNotEmpty(instagramDialog)) {
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
                if (Utils.isNotEmpty(instagramUser) &&
                        Utils.isNotEmpty(instagramUser.getData())) {
                    user = Api.getUserByInstagramId(instagramUser.getData().getId());
                    if (Utils.isEmpty(user)) {
                        isNewUser = true;
                        user = Factory.userFromInstagramUser(instagramUser.getData());
                    } else {
                        Utils.logInfo(clazz, "User already exists: " + user);
                    }
                }
            } catch (IOException e) {
                Utils.logError(clazz, "IOException: " + e.toString());
            } catch (JSONException e) {
                Utils.logError(clazz, "JSONException: " + e.toString());
            }
            return instagramUser;
        }

        @Override
        protected void onPostExecute(InstagramUser result) {
            super.onPostExecute(result);
            if (Utils.isNotEmpty(result) &&
                    Utils.isNotEmpty(result.getMeta()) &&
                    Utils.isNotEmpty(result.getData())) {
                if (result.getMeta().isError()) {
                    Utils.afterError(clazz, result.toString());
                } else {
                    Utils.logInfo(clazz, "Collected instagram user data: " + result.toString());
                    if (isNewUser) {
                        new ProcessAddUser().execute();
                    } else {
                        Session.getInstance().setUser(user);
                        handleLogin();
                        invalidateOptionsMenu();
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
                result = Api.addUser(user);
            } catch (JSONException e) {
                Utils.logError(clazz, "JSONException: " + e.toString());
            } catch (IOException e) {
                Utils.logError(clazz, "IOException: " + e.toString());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                Utils.showMessage(clazz, "User logged in");
                Session.getInstance().setUser(user);
                saveAccessToken(accessToken);
                handleLogin();
                invalidateOptionsMenu();
            }
        }
    }

    private class ProcessDeleteUser extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean result = Boolean.FALSE;
            try {
                result = Api.removeUser(user);
            } catch (IOException e) {
                Utils.logError(clazz, "IOException: " + e.toString());
            } catch (JSONException e) {
                Utils.logError(clazz, "JSONException: " + e.toString());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                Utils.logDebug(clazz, "User deleted");
                resetAuthentication();
                removeAccessToken();
            }
        }
    }

}
