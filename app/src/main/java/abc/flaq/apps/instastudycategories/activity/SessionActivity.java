package abc.flaq.apps.instastudycategories.activity;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import abc.flaq.apps.instastudycategories.general.Session;
import abc.flaq.apps.instastudycategories.helper.Constants;
import abc.flaq.apps.instastudycategories.helper.Factory;
import abc.flaq.apps.instastudycategories.helper.Utils;
import abc.flaq.apps.instastudycategories.pojo.User;
import abc.flaq.apps.instastudycategories.pojo.instagram.InstagramAccessToken;
import abc.flaq.apps.instastudycategories.pojo.instagram.InstagramUser;

import static abc.flaq.apps.instastudycategories.helper.Constants.INSTAGRAM_ENDPOINT_USER_SELF;
import static abc.flaq.apps.instastudycategories.helper.Constants.INSTAGRAM_REDIRECT_URL;
import static abc.flaq.apps.instastudycategories.helper.Constants.INSTAGRAM_URL;
import static abc.flaq.apps.instastudycategories.helper.Constants.SETTINGS_ACCESS_TOKEN;

public class SessionActivity extends AppCompatActivity {

    private final AppCompatActivity clazz = this;
    private View rootView;

    private Menu mainMenu;
    private User user;
    private String accessToken;
    private Dialog instagramDialog;
    private Boolean isApiWorking = false;

    // TODO: menu z lewej
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.removeActionBarShadow(clazz);

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
            case R.id.menu_info:
                if (Utils.isEmpty(Session.getInstance().getUser())) {
                    Utils.logDebug(clazz, "User is empty, but info icon is available");
                    new ProcessGetUser().execute();
                } else {
                    showInfoDialog();
                }
                break;
            case R.id.menu_login:
                if (Utils.isNotEmpty(Session.getInstance().getUser())) {
                    Utils.logDebug(clazz, "User is not empty, but login icon is available");
                    setMainMenuVisibility(mainMenu);
                    invalidateOptionsMenu();
                } else {
                    try {
                        String instagramAuthUrl = InstagramApi.getAuthUrl(Constants.INSTAGRAM_SCOPES.public_content);
                        showInstagramDialog(instagramAuthUrl);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                        Utils.showConnectionError(rootView, getString(R.string.error_user_login));
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void showInstagramDialog(String url) {
        instagramDialog = new Dialog(clazz);
        if (Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().removeAllCookies(null);
        } else {
            CookieManager.getInstance().removeAllCookie();
        }
        WebView webView = new WebView(clazz);
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith(INSTAGRAM_REDIRECT_URL)) {
                    String code = InstagramApi.getCodeFromUrl(rootView, url);
                    if (Utils.isEmpty(code)) {
                        Utils.logError(clazz, "Empty Instagram code");
                        Utils.showConnectionError(rootView, getString(R.string.error_ig_login));
                        instagramDialog.dismiss();
                    } else {
                        new ProcessGetAccessToken().execute(code);
                    }
                } else {
                    view.loadUrl(url);
                }
                return true;
            }
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Utils.showConnectionError(rootView, getString(R.string.error_ig_login));
                instagramDialog.dismiss();
            }
        });
        instagramDialog.setContentView(webView);
        instagramDialog.show();
    }

    private void showInfoDialog() {
        MaterialDialog.Builder infoDialogBuilder = new MaterialDialog.Builder(clazz)
                .title(Session.getInstance().getUser().getUsername())
                .content(Session.getInstance().getUser().getInfoContent())
                .contentColorRes(android.R.color.primary_text_light)
                .positiveText(R.string.back)
                .negativeText(R.string.logout)
                .neutralText(R.string.delete_account)
                .neutralColorRes(R.color.colorAdditionalAction)
                .titleColorRes(R.color.colorPrimaryDark)
                .backgroundColorRes(R.color.colorBackgroundLight)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Utils.showQuickInfo(rootView, getString(R.string.logged_out));
                        logOut();
                        dialog.dismiss();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Snackbar.make(Utils.findSnackbarView(rootView), R.string.remove_account_info, Snackbar.LENGTH_LONG)
                                .setActionTextColor(ContextCompat.getColor(clazz, R.color.colorAccent))
                                .setAction(R.string.remove, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        new ProcessDeleteUser().execute();
                                    }
                                }).show();
                        dialog.dismiss();
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
                Utils.showQuickInfo(rootView, getString(R.string.ig_profile_open) + Session.getInstance().getUser().getUsername() + "&#8230;");
                Intent nextIntent = Utils.getInstagramIntent(Session.getInstance().getUser().getUsername());

                if (Utils.isIntentAvailable(clazz, nextIntent)) {
                    Utils.logDebug(clazz, "Instagram intent is available");
                    clazz.startActivity(nextIntent);
                } else {
                    Utils.logDebug(clazz, "Instagram intent is NOT available");
                    clazz.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL + Session.getInstance().getUser().getUsername())));
                }

                infoDialog.dismiss();
            }
        });
        infoDialog.getTitleView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.showQuickInfo(rootView, getString(R.string.ig_profile_open) + Session.getInstance().getUser().getUsername() + "&#8230;");
                Intent nextIntent = Utils.getInstagramIntent(Session.getInstance().getUser().getUsername());

                if (Utils.isIntentAvailable(clazz, nextIntent)) {
                    Utils.logDebug(clazz, "Instagram intent is available");
                    clazz.startActivity(nextIntent);
                } else {
                    Utils.logDebug(clazz, "Instagram intent is NOT available");
                    clazz.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL + Session.getInstance().getUser().getUsername())));
                }

                infoDialog.dismiss();
            }
        });
        infoDialog.show();
    }

    public void setMainMenuVisibility(Menu menu) {
        if (Utils.isNotEmpty(menu)) {
            Boolean isAuthenticated = Utils.isNotEmpty(Session.getInstance().getUser());
            menu.findItem(R.id.menu_suggest).setVisible(isAuthenticated);
            menu.findItem(R.id.menu_join).setVisible(isAuthenticated);
            menu.findItem(R.id.menu_leave).setVisible(isAuthenticated);
            menu.findItem(R.id.menu_sort).setVisible(isAuthenticated);
            menu.findItem(R.id.menu_info).setVisible(isAuthenticated);
            menu.findItem(R.id.menu_login).setVisible(!isAuthenticated);
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

    public void logOut() {
        user = null;
        Session.getInstance().setUser(null);
        accessToken = null;
        removeAccessToken();
        setMainMenuVisibility(mainMenu);
        invalidateOptionsMenu();
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
                Utils.logError(clazz, "IOException: " + e.getMessage());
                Utils.showConnectionError(rootView, getString(R.string.error_user_login));
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
                    Session.getInstance().setUser(null);
                    accessToken = null;
                    setMainMenuVisibility(mainMenu);
                    invalidateOptionsMenu();
                } else {
                    Utils.logDebug(clazz, "Collected instagram access token: " + result.getAccessToken());
                    accessToken = result.getAccessToken();
                    new ProcessGetUser().execute();
                }
            } else {
                Utils.showConnectionError(rootView, getString(R.string.error_user_login));
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
                Utils.logError(clazz, "IOException: " + e.getMessage());
                Utils.showConnectionError(rootView, getString(R.string.error_user_login));
            } catch (JSONException e) {
                Utils.logError(clazz, "JSONException: " + e.getMessage());
                Utils.showConnectionError(rootView, getString(R.string.error_user_login));
            }
            return instagramUser;
        }

        @Override
        protected void onPostExecute(InstagramUser result) {
            super.onPostExecute(result);
            isApiWorking = false;

            if (Utils.isNotEmpty(result) &&
                    Utils.isNotEmpty(result.getMeta()) &&
                    Utils.isNotEmpty(result.getData())) {
                if (result.getMeta().isError()) {
                    Utils.logError(rootView.getContext(), result.toString());
                    Utils.showConnectionError(rootView, getString(R.string.error_user_login));
                } else {
                    Utils.logDebug(clazz, "Collected instagram user data: " + result.toString());
                    if (isNewUser) {
                        new ProcessAddUser().execute();
                    } else {
                        Utils.showQuickInfo(rootView, "Zalogowano");
                        Session.getInstance().setUser(user);
                        saveAccessToken(accessToken);

                        ImageView profilePic = new ImageView(clazz);
                        UrlImageViewHelper.setUrlDrawable(profilePic, Session.getInstance().getUser().getProfilePicUrl(), R.drawable.placeholder_profile_pic_72);
                        Session.getInstance().setUserProfilePic(profilePic);

                        setMainMenuVisibility(mainMenu);
                        invalidateOptionsMenu();
                    }
                }
            } else {
                Utils.showConnectionError(rootView, getString(R.string.error_user_login));
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
                Utils.logError(clazz, "JSONException: " + e.getMessage());
                Utils.showConnectionError(rootView, getString(R.string.error_user_login));
            } catch (IOException e) {
                Utils.logError(clazz, "IOException: " + e.getMessage());
                Utils.showConnectionError(rootView, getString(R.string.error_user_login));
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
            } else {
                Utils.showConnectionError(rootView, getString(R.string.error_user_login));
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
                Utils.logError(clazz, "IOException: " + e.getMessage());
                Utils.showConnectionError(rootView, getString(R.string.error_account_remove));
            } catch (JSONException e) {
                Utils.logError(clazz, "JSONException: " + e.getMessage());
                Utils.showConnectionError(rootView, getString(R.string.error_account_remove));
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            isApiWorking = false;

            if (result) {
                Utils.showQuickInfo(rootView, getString(R.string.remove_account_success));
                logOut();
            }
        }
    }

}
