package abc.flaq.apps.instastudycategories;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Toast;

import com.crystal.crystalpreloaders.widgets.CrystalPreloader;
import com.etsy.android.grid.StaggeredGridView;
import com.getinch.retrogram.Instagram;

import org.json.JSONException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static abc.flaq.apps.instastudycategories.Constants.INSTAGRAM_REDIRECT_URL;

public class CategoryActivity extends AppCompatActivity {

    private final Activity clazz = this;
    private CategoryAdapter categoryAdapter;

    private StaggeredGridView gridView;
    private CrystalPreloader preloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_category);
        gridView = (StaggeredGridView) findViewById(R.id.category_grid);
        preloader = (CrystalPreloader) findViewById(R.id.category_preloader);

        Utils.log(Utils.LOG_DEBUG, clazz, "Build data: " +
                BuildConfig.FLAVOR_FULLNAME +
                "/" + BuildConfig.BUILD_TYPE + " " +
                BuildConfig.VERSION_NAME
        );

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                /*Category selected = categoryAdapter.getItem(position);
                Utils.log(Utils.LOG_DEBUG, clazz, "Selected position: " + position);

                Intent nextIntent;
                if (selected.isAsSubcategory()) {
                    nextIntent = new Intent(clazz, UserActivity.class);
                } else {
                    nextIntent = new Intent(clazz, SubcategoryActivity.class);
                }
                nextIntent.putExtra(INTENT_CATEGORY_ID, selected.getId());
                clazz.startActivity(nextIntent);*/
                String url = null;
                try {
                    String instagramAuthUrl = Utils.getInstagramAuthUrl(Constants.INSTAGRAM_SCOPES.public_content);
                    //Uri instagramUri = Uri.parse(instagramAuthUrl);
                    createInstagramDialog(instagramAuthUrl);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });

        new ProcessCategories().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        menu.findItem(R.id.menu_info).setVisible(false);
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
                final Instagram ig = new Instagram("2307539515.7efc3e8.cefcf169c7e4438ba632cd1803edf7b5");
                com.getinch.retrogram.model.User user = ig.getUsersEndpoint().getUser("2307539515");
                String a = user.getFullName();
                break;
            case R.id.menu_info:
                // HIDDEN
                break;
            default:
                break;
        }
        return true;
    }

    private void createInstagramDialog(String url) {
        final Dialog dialog = new Dialog(clazz);

        WebView webView = new WebView(clazz);
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Utils.isNotEmpty(url) && !url.startsWith(INSTAGRAM_REDIRECT_URL)) {
                    view.loadUrl(url);
                } else {
                    String code = Utils.getInstagramCode(url);
                    if (Utils.isEmpty(code)) {
                        Utils.afterError(clazz);
                    } else {
                        Utils.showMessage(clazz, "zalogowany");
                    }
                    dialog.dismiss();
                }
                return true;
            }
        });

        dialog.setContentView(webView);
        dialog.show();
    }

    private class ProcessCategories extends AsyncTask<Void, Void, List<Category>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            preloader.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Category> doInBackground(Void... params) {
            List<Category> categories = new ArrayList<>();

            try {
                Thread.sleep(1000); // FIXME: showing preloader, REMOVE
                categories = Api.getAllCategories(false);
                for (Category category : categories) {
                    Utils.log(Utils.LOG_DEBUG, clazz, category.toString());
                }
                /*InputStream returna = Api.post(INSTAGRAM_REQUEST_ACCESS_TOKEN_URL, "client_id=7efc3e82d7b64440b74a092651eae4cd&client_secret=b7096eab9408483a9582263352696491&grant_type=authorization_code&redirect_uri=http://94.176.238.81&code=7841a47567d14b33b7b82d9df71e92d9");
                String a = Api.getStream(returna);*/
            } catch (InterruptedException e) {
                Utils.log(Utils.LOG_ERROR, clazz, "InterruptedException: " + e.toString());
            } catch (JSONException e) {
                Utils.log(Utils.LOG_ERROR, clazz, "JSONException: " + e.toString());
            } catch (IOException e) {
                Utils.log(Utils.LOG_ERROR, clazz, "IOException: " + e.toString());
            }

            return categories;
        }

        @Override
        protected void onPostExecute(List<Category> result) {
            super.onPostExecute(result);

            preloader.setVisibility(View.GONE);
            categoryAdapter = new CategoryAdapter(clazz, result);
            gridView.setAdapter(categoryAdapter);
        }
    }

}
