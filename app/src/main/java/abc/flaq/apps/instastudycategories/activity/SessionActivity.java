package abc.flaq.apps.instastudycategories.activity;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import org.json.JSONException;

import java.io.IOException;
import java.net.URISyntaxException;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.api.Api;
import abc.flaq.apps.instastudycategories.api.InstagramApi;
import abc.flaq.apps.instastudycategories.design.Decorator;
import abc.flaq.apps.instastudycategories.general.Session;
import abc.flaq.apps.instastudycategories.helper.Factory;
import abc.flaq.apps.instastudycategories.helper.Utils;
import abc.flaq.apps.instastudycategories.pojo.User;
import abc.flaq.apps.instastudycategories.pojo.instagram.InstagramAccessToken;
import abc.flaq.apps.instastudycategories.pojo.instagram.InstagramUser;

import static abc.flaq.apps.instastudycategories.helper.Constants.INSTAGRAM_ENDPOINT_USER_SELF;
import static abc.flaq.apps.instastudycategories.helper.Constants.INSTAGRAM_REMOTE_REDIRECT_URL;
import static abc.flaq.apps.instastudycategories.helper.Constants.INSTAGRAM_URL;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SESSION;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SESSION_LOGIN;
import static abc.flaq.apps.instastudycategories.helper.Constants.SETTINGS_ACCESS_TOKEN;

public class SessionActivity extends AppCompatActivity {

    private final AppCompatActivity clazz = this;
    private View rootView;

    private Menu mainMenu;
    private User user;
    private String accessToken;
    private Dialog instagramDialog;
    private Boolean isApiWorking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Decorator.removeActionBarShadow(clazz);

        rootView = findViewById(android.R.id.content);
        accessToken = Session.getInstance().getSettings().getString(SETTINGS_ACCESS_TOKEN, null);
        if (Utils.isEmpty(Session.getInstance().getUser())) {
            if (Utils.isNotEmpty(accessToken)) {
                new ProcessGetUser().execute();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.isEmpty(Session.getInstance().getUser())) {
            setMainMenuVisibility(mainMenu);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        mainMenu = menu;
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isApiWorking) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_suggest:
                return super.onOptionsItemSelected(item);
            case R.id.menu_join:
                return super.onOptionsItemSelected(item);
            case R.id.menu_leave:
                return super.onOptionsItemSelected(item);
            case R.id.menu_sort:
                return super.onOptionsItemSelected(item);
            case R.id.menu_login:
                if (Utils.isEmpty(Session.getInstance().getUser())) {
                    showLoginDialog();
                } else {
                    Utils.logDebug(clazz, "User is not empty, but login icon is available");
                    setMainMenuVisibility(mainMenu);
                    invalidateOptionsMenu();
                }
                break;
            default:
                break;
        }
        return true;
    }

    public void showLoginDialog() {
        initInstagramDialog();
        new MaterialDialog.Builder(clazz)
                .title(R.string.login)
                .content(R.string.login_info)
                .positiveText(R.string.log_in)
                .neutralText(R.string.back)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        instagramDialog.show();
                        dialog.dismiss();
                    }
                })
                .show();
    }
    private void initInstagramDialog() {
        String instagramAuthUrl = "";
        try {
            instagramAuthUrl = InstagramApi.getRemoteAuthUrl();
        } catch (URISyntaxException e) {
            handleConnectionError(getString(R.string.error_user_login));
            Utils.logError(clazz, "URISyntaxException: " + e.getMessage());
        }
        WebView loginWebView = new WebView(clazz);
        loginWebView.loadUrl(instagramAuthUrl);
        loginWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith(INSTAGRAM_REMOTE_REDIRECT_URL) && url.length() > INSTAGRAM_REMOTE_REDIRECT_URL.length()) {
                    if (url.contains("user_denied")) {
                        instagramDialog.dismiss();
                    } else {
                        accessToken = InstagramApi.getAccessTokenFromUrl(url);
                        if (Utils.isEmpty(accessToken)) {
                            handleConnectionError(getString(R.string.error_ig_login));
                            Utils.logError(clazz, "Empty Instagram accessToken");
                            instagramDialog.dismiss();
                        } else if (instagramDialog.isShowing()) {
                            Utils.logDebug(clazz, "Collected instagram access token: " + accessToken);
                            new ProcessGetUser().execute();
                        }
                    }
                } else {
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                handleConnectionError(getString(R.string.error_ig_login));
                instagramDialog.dismiss();
                logOut();
            }
        });

        instagramDialog = new Dialog(clazz);
        instagramDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        instagramDialog.setContentView(loginWebView);
    }

    public void setMainMenuVisibility(Menu menu) {
        if (Utils.isNotEmpty(menu)) {
            Boolean isAuthenticated = Utils.isNotEmpty(Session.getInstance().getUser());
            menu.findItem(R.id.menu_suggest).setVisible(isAuthenticated);
            menu.findItem(R.id.menu_join).setVisible(isAuthenticated);
            menu.findItem(R.id.menu_leave).setVisible(isAuthenticated);
            menu.findItem(R.id.menu_sort).setVisible(isAuthenticated);
            menu.findItem(R.id.menu_login).setVisible(!isAuthenticated);
        }
    }

    public void showOtherUserInfoDialog(final AppCompatActivity activity, final User user) {
        MaterialDialog.Builder infoDialogBuilder = new MaterialDialog.Builder(activity)
                .title(user.getUsername())
                .content(user.getInfoContent(activity))
                .positiveText(R.string.back);

        if (Utils.isEmpty(user.getProfilePicUrl())) {
            infoDialogBuilder.iconRes(R.drawable.placeholder_profile_pic_72);
        } else {
            ImageView profilePic = new ImageView(activity);
            UrlImageViewHelper.setUrlDrawable(profilePic, user.getProfilePicUrl(), R.drawable.placeholder_profile_pic_72);
            infoDialogBuilder.icon(profilePic.getDrawable());
        }

        final MaterialDialog infoDialog = infoDialogBuilder.build();
        infoDialog.getIconView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent nextIntent = Utils.getInstagramIntent(user.getUsername());

                if (Utils.isIntentAvailable(activity, nextIntent)) {
                    Utils.logDebug(activity, "Instagram intent is available");
                    activity.startActivity(nextIntent);
                } else {
                    Utils.logDebug(activity, "Instagram intent is NOT available");
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL + user.getUsername())));
                }

                infoDialog.hide();
            }
        });
        infoDialog.getTitleView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent nextIntent = Utils.getInstagramIntent(user.getUsername());

                if (Utils.isIntentAvailable(activity, nextIntent)) {
                    Utils.logDebug(activity, "Instagram intent is available");
                    activity.startActivity(nextIntent);
                } else {
                    Utils.logDebug(activity, "Instagram intent is NOT available");
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL + user.getUsername())));
                }

                infoDialog.hide();
            }
        });

        infoDialog.show();
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

    public void logOut() {
        user = null;
        Session.getInstance().setUser(null);
        accessToken = null;
        removeAccessToken();
        setMainMenuVisibility(mainMenu);
        invalidateOptionsMenu();

        if (Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().removeAllCookies(null);
        } else {
            CookieManager.getInstance().removeAllCookie();
        }
    }

    private void handleConnectionError(String message) {
        isApiWorking = true;
        Utils.showConnectionError(rootView, message);
    }

    private class ProcessGetAccessToken extends AsyncTask<String, Void, InstagramAccessToken> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isApiWorking = true;
        }

        @Override
        protected InstagramAccessToken doInBackground(String... params) {
            String code = params[0];
            InstagramAccessToken instagramToken = null;
            try {
                instagramToken = InstagramApi.getAccessTokenByCode(code);
            } catch (IOException e) {
                handleConnectionError(getString(R.string.error_user_login));
                Utils.logError(clazz, "IOException: " + e.getMessage());
            }
            return instagramToken;
        }

        @Override
        protected void onPostExecute(InstagramAccessToken result) {
            super.onPostExecute(result);
            isApiWorking = false;

            if (Utils.isNotEmpty(result)) {
                if (result.isError() || Utils.isEmpty(result.getAccessToken())) {
                    Utils.logError(rootView.getContext(), result.toString());
                    Utils.showConnectionError(rootView, getString(R.string.error_user_login));
                    logOut();
                } else {
                    Utils.logDebug(clazz, "Collected instagram access token: " + result.getAccessToken());
                    accessToken = result.getAccessToken();
                    new ProcessGetUser().execute();
                }
            } else {
                Utils.showConnectionError(rootView, getString(R.string.error_user_login));
                logOut();
            }
            if (Utils.isNotEmpty(instagramDialog)) {
                instagramDialog.dismiss();
            }
        }
    }

    public class ProcessGetUser extends AsyncTask<Void, Void, InstagramUser> {
        private Boolean isNewUser = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isApiWorking = true;
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
                        Utils.logDebug(clazz, "User already exists: " + user);
                        user.updateFromInstagramUser(instagramUser.getData());
                        Api.updateUser(user);
                    }
                }
            } catch (IOException e) {
                handleConnectionError(getString(R.string.error_user_login));
                Utils.logError(clazz, "IOException: " + e.getMessage());
            } catch (JSONException e) {
                handleConnectionError(getString(R.string.error_user_login));
                Utils.logError(clazz, "JSONException: " + e.getMessage());
            }
            return instagramUser;
        }

        @Override
        protected void onPostExecute(InstagramUser result) {
            super.onPostExecute(result);
            isApiWorking = false;

            if (Utils.isNotEmpty(user) &&
                    Utils.isNotEmpty(result) &&
                    Utils.isNotEmpty(result.getMeta()) &&
                    Utils.isNotEmpty(result.getData())) {
                if (result.getMeta().isError()) {
                    Utils.logError(rootView.getContext(), result.toString());
                    Utils.showConnectionError(rootView, getString(R.string.error_user_login));
                    logOut();
                } else {
                    Utils.logDebug(clazz, "Collected instagram user data: " + result.toString());
                    if (isNewUser) {
                        new ProcessAddUser().execute();
                    } else {
                        Utils.showQuickInfo(rootView, getString(R.string.logged_in));
                        Session.getInstance().setUser(user);
                        saveAccessToken(accessToken);

                        ImageView profilePic = new ImageView(clazz);
                        UrlImageViewHelper.setUrlDrawable(profilePic, Session.getInstance().getUser().getProfilePicUrl(), R.drawable.placeholder_profile_pic_72);
                        Session.getInstance().setUserProfilePic(profilePic);

                        setMainMenuVisibility(mainMenu);
                        invalidateOptionsMenu();

                        Intent intent = new Intent(INTENT_SESSION);
                        intent.putExtra(INTENT_SESSION_LOGIN, true);
                        LocalBroadcastManager.getInstance(clazz).sendBroadcast(intent);
                    }
                }
            } else {
                Utils.showConnectionError(rootView, getString(R.string.error_user_login));
                logOut();
            }
            if (Utils.isNotEmpty(instagramDialog)) {
                instagramDialog.dismiss();
            }
        }
    }

    private class ProcessAddUser extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isApiWorking = true;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean result = Boolean.FALSE;
            try {
                result = Api.addUser(user);
            } catch (JSONException e) {
                handleConnectionError(getString(R.string.error_user_login));
                Utils.logError(clazz, "JSONException: " + e.getMessage());
            } catch (IOException e) {
                handleConnectionError(getString(R.string.error_user_login));
                Utils.logError(clazz, "IOException: " + e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            isApiWorking = false;

            if (result) {
                Utils.showQuickInfo(rootView, getString(R.string.logged_in));
                Session.getInstance().setUser(user);
                saveAccessToken(accessToken);

                ImageView profilePic = new ImageView(clazz);
                UrlImageViewHelper.setUrlDrawable(profilePic, Session.getInstance().getUser().getProfilePicUrl(), R.drawable.placeholder_profile_pic_72);
                Session.getInstance().setUserProfilePic(profilePic);

                setMainMenuVisibility(mainMenu);
                invalidateOptionsMenu();

                Intent intent = new Intent(INTENT_SESSION);
                intent.putExtra(INTENT_SESSION_LOGIN, true);
                LocalBroadcastManager.getInstance(clazz).sendBroadcast(intent);
            } else {
                Utils.showConnectionError(rootView, getString(R.string.error_user_login));
                logOut();
            }
        }
    }

}
