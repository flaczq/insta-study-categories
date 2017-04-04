package abc.flaq.apps.instastudycategories.adapter;

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
import android.widget.Toast;

import com.crystal.crystalpreloaders.widgets.CrystalPreloader;

import org.json.JSONException;

import java.io.IOException;
import java.net.URISyntaxException;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.pojo.InstagramOAuthResponse;
import abc.flaq.apps.instastudycategories.pojo.InstagramUser;
import abc.flaq.apps.instastudycategories.pojo.User;
import abc.flaq.apps.instastudycategories.utils.Api;
import abc.flaq.apps.instastudycategories.utils.Constants;
import abc.flaq.apps.instastudycategories.utils.GeneralUtils;
import abc.flaq.apps.instastudycategories.utils.InstagramUtils;

import static abc.flaq.apps.instastudycategories.utils.Constants.INSTAGRAM_REDIRECT_URL;

public class MenuActivity extends AppCompatActivity {

    private final Activity clazz = this;

    private Dialog instagramDialog;
    private InstagramUser instagramUser;
    private Boolean isAuthenticated;
    private Menu mainMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instagramDialog = new Dialog(clazz);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        mainMenu = menu;
        setAuthenticated(false);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                Toast.makeText(clazz, "adding", Toast.LENGTH_LONG).show();
                break;
            case R.id.menu_join:
                Toast.makeText(clazz, "joining", Toast.LENGTH_LONG).show();
                break;
            case R.id.menu_info:
                if (GeneralUtils.isNotEmpty(instagramUser)) {
                    Toast.makeText(clazz, instagramUser.toString(), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.menu_login:
                Toast.makeText(clazz, "Login in...", Toast.LENGTH_SHORT).show();
                try {
                    String instagramAuthUrl = InstagramUtils.getAuthUrl(Constants.INSTAGRAM_SCOPES.public_content);
                    createInstagramDialog(instagramAuthUrl);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void createInstagramDialog(String url) {
        // fixme: better preloader
        final CrystalPreloader preloader = new CrystalPreloader(clazz);
        WebView webView = new WebView(clazz);
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (GeneralUtils.isNotEmpty(url) && !url.startsWith(INSTAGRAM_REDIRECT_URL)) {
                    view.loadUrl(url);
                    preloader.setVisibility(View.GONE);
                } else {
                    String code = InstagramUtils.getCodeFromUrl(clazz, url);
                    if (GeneralUtils.isEmpty(code)) {
                        GeneralUtils.afterError(clazz, "Instagram code is empty");
                        instagramDialog.dismiss();
                    } else {
                        GeneralUtils.logDebug(clazz, "Collected instagram code: " + code);
                        preloader.setVisibility(View.VISIBLE);
                        new ProcessHandleCode().execute(code);
                    }
                }
                return true;
            }
        });

        instagramDialog.setContentView(webView);
        instagramDialog.addContentView(preloader, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        instagramDialog.show();
    }

    private void setAuthenticated(Boolean authenticated) {
        isAuthenticated = authenticated;
        if (authenticated) {
            mainMenu.findItem(R.id.menu_login).setVisible(false);
            mainMenu.findItem(R.id.menu_add).setVisible(true);
            mainMenu.findItem(R.id.menu_join).setVisible(true);
            mainMenu.findItem(R.id.menu_info).setVisible(true);
        } else {
            mainMenu.findItem(R.id.menu_login).setVisible(true);
            mainMenu.findItem(R.id.menu_add).setVisible(false);
            mainMenu.findItem(R.id.menu_join).setVisible(false);
            mainMenu.findItem(R.id.menu_info).setVisible(false);
        }
    }

    private class ProcessHandleCode extends AsyncTask<String, Void, InstagramOAuthResponse> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //preloader.setVisibility(View.VISIBLE);
        }

        @Override
        protected InstagramOAuthResponse doInBackground(String... params) {
            String code = params[0];
            InstagramOAuthResponse instagramToken = null;
            try {
                instagramToken = InstagramUtils.getAccessTokenByCode(code);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return instagramToken;
        }

        @Override
        protected void onPostExecute(InstagramOAuthResponse result) {
            super.onPostExecute(result);

            //preloader.setVisibility(View.GONE);
            if (GeneralUtils.isNotEmpty(result)) {
                if (result.isError()) {
                    GeneralUtils.afterError(clazz, result.toString());
                    instagramUser = null;
                    setAuthenticated(false);
                } else {
                    GeneralUtils.logDebug(clazz, "Collected instagram access token: " + result.getAccessToken());
                    GeneralUtils.showMessage(clazz, "zalogowany");
                    instagramUser = result.getUser();
                    setAuthenticated(true);
                }
                instagramDialog.dismiss();
            }
        }
    }

    private class ProcessDeleteUser extends AsyncTask<User, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //preloader.setVisibility(View.VISIBLE);
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

            //preloader.setVisibility(View.GONE);
            GeneralUtils.logDebug(clazz, "User deleted: " + result);
        }
    }

}
