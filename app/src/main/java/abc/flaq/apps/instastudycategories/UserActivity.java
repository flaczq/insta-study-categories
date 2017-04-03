package abc.flaq.apps.instastudycategories;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.crystal.crystalpreloaders.widgets.CrystalPreloader;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static abc.flaq.apps.instastudycategories.Constants.INSTAGRAM_URL;
import static abc.flaq.apps.instastudycategories.Constants.INTENT_CATEGORY_ID;
import static abc.flaq.apps.instastudycategories.Constants.INTENT_SUBCATEGORY_ID;
import static abc.flaq.apps.instastudycategories.Constants.PACKAGE_INSTAGRAM;

public class UserActivity extends AppCompatActivity {

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
        if (Utils.isNotEmpty(categoryId)) {
            isCategory = true;
            id = categoryId;
        } else {
            String subcategoryId = intent.getStringExtra(INTENT_SUBCATEGORY_ID);
            if (Utils.isNotEmpty(subcategoryId)) {
                id = subcategoryId;
            }
        }

        if (Utils.isEmpty(id)) {
            Utils.afterError(clazz);
            finish();
        } else {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                    String selected = userAdapter.getUsername(position);
                    Utils.log(Utils.LOG_DEBUG, clazz, "Selected position: " + position);
                    Uri instagramUri = Uri.parse(INSTAGRAM_URL + "_u/" + selected);
                    Intent nextIntent = new Intent(Intent.ACTION_VIEW, instagramUri);
                    nextIntent.setPackage(PACKAGE_INSTAGRAM);

                    if (Utils.isIntentAvailable(clazz, nextIntent)) {
                        Utils.log(Utils.LOG_DEBUG, clazz, "Instagram intent is available");
                        clazz.startActivity(nextIntent);
                    } else {
                        Utils.log(Utils.LOG_DEBUG, clazz, "Instagram intent is NOT available");
                        clazz.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL + selected)));
                    }
                }
            });

            new ProcessUsers().execute(id);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        menu.findItem(R.id.menu_add).setVisible(false);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                // HIDDEN
                break;
            case R.id.menu_join:
                Toast.makeText(clazz, "joining", Toast.LENGTH_LONG).show();
                break;
            case R.id.menu_info:
                Toast.makeText(clazz, "infoing", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
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
                    Utils.log(Utils.LOG_DEBUG, clazz, user.toString());
                }
            } catch (InterruptedException e) {
                Utils.log(Utils.LOG_ERROR, clazz, "InterruptedException: " + e.toString());
            } catch (JSONException e) {
                Utils.log(Utils.LOG_ERROR, clazz, "JSONException: " + e.toString());
            } catch (IOException e) {
                Utils.log(Utils.LOG_ERROR, clazz, "IOException: " + e.toString());
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

    private class ProcessDeleteUser extends AsyncTask<User, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            preloader.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(User... params) {
            User user = params[0];
            Boolean result = Boolean.FALSE;
            try {
                result = Api.deleteUser(user);
            } catch (IOException e) {
                Utils.log(Utils.LOG_ERROR, clazz, "IOException: " + e.toString());
            } catch (JSONException e) {
                Utils.log(Utils.LOG_ERROR, clazz, "JSONException: " + e.toString());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            preloader.setVisibility(View.GONE);
            Utils.log(Utils.LOG_INFO, clazz, "User deleted: " + result);
        }
    }

}
