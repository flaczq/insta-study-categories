package abc.flaq.apps.instastudycategories.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.crystal.crystalpreloaders.widgets.CrystalPreloader;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.adapter.MenuActivity;
import abc.flaq.apps.instastudycategories.adapter.UserAdapter;
import abc.flaq.apps.instastudycategories.pojo.User;
import abc.flaq.apps.instastudycategories.utils.Api;
import abc.flaq.apps.instastudycategories.utils.GeneralUtils;

import static abc.flaq.apps.instastudycategories.utils.Constants.INSTAGRAM_URL;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_ID;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_SUBCATEGORY_ID;
import static abc.flaq.apps.instastudycategories.utils.Constants.PACKAGE_INSTAGRAM;

public class UserActivity extends MenuActivity {

    private final Activity clazz = this;
    private UserAdapter userAdapter;
    private Intent intent;

    private ListView listView;
    private CrystalPreloader preloader;

    private Boolean isCategory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user);
        listView = (ListView) findViewById(R.id.user_list);
        preloader = (CrystalPreloader) findViewById(R.id.user_preloader);

        intent = getIntent();
        // Check if passed id is from category or subcategory
        String categoryId = intent.getStringExtra(INTENT_CATEGORY_ID);
        String id = null;
        if (GeneralUtils.isNotEmpty(categoryId)) {
            isCategory = true;
            id = categoryId;
        } else {
            String subcategoryId = intent.getStringExtra(INTENT_SUBCATEGORY_ID);
            if (GeneralUtils.isNotEmpty(subcategoryId)) {
                id = subcategoryId;
            }
        }

        if (GeneralUtils.isEmpty(id)) {
            GeneralUtils.afterError(clazz, "(sub)categoryId is empty");
            finish();
        } else {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                    String selected = userAdapter.getUsername(position);
                    GeneralUtils.logDebug(clazz, "Selected position: " + position);
                    Uri instagramUri = Uri.parse(INSTAGRAM_URL + "_u/" + selected);
                    Intent nextIntent = new Intent(Intent.ACTION_VIEW, instagramUri);
                    nextIntent.setPackage(PACKAGE_INSTAGRAM);

                    if (GeneralUtils.isIntentAvailable(clazz, nextIntent)) {
                        GeneralUtils.logDebug(clazz, "Instagram intent is available");
                        clazz.startActivity(nextIntent);
                    } else {
                        GeneralUtils.logDebug(clazz, "Instagram intent is NOT available");
                        clazz.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL + selected)));
                    }
                }
            });

            new ProcessUsers().execute(id);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    private class ProcessUsers extends AsyncTask<String, Void, List<User>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            preloader.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<User> doInBackground(String... params) {
            String id = params[0];
            List<User> users = new ArrayList<>();

            try {
                Thread.sleep(1000); // FIXME: showing preloader, REMOVE!
                if (isCategory) {
                    users = Api.getUsersByCategoryId(id);
                } else {
                    users = Api.getUsersBySubcategoryId(id);
                }
                users = Api.getAllUsers(false);
                for (User user : users) {
                    GeneralUtils.logInfo(clazz, user.toString());
                }
            } catch (InterruptedException e) {
                GeneralUtils.logError(clazz, "InterruptedException: " + e.toString());
            } catch (JSONException e) {
                GeneralUtils.logError(clazz, "JSONException: " + e.toString());
            } catch (IOException e) {
                GeneralUtils.logError(clazz, "IOException: " + e.toString());
            }

            return users;
        }

        @Override
        protected void onPostExecute(List<User> result) {
            super.onPostExecute(result);

            preloader.setVisibility(View.GONE);
            userAdapter = new UserAdapter(clazz, result);
            listView.setAdapter(userAdapter);
        }
    }

}
