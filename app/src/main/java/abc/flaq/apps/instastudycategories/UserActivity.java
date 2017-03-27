package abc.flaq.apps.instastudycategories;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.crystal.crystalpreloaders.widgets.CrystalPreloader;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static abc.flaq.apps.instastudycategories.Constants.INSTAGRAM_URL;
import static abc.flaq.apps.instastudycategories.Constants.INTENT_SUBCATEGORY_ID;
import static abc.flaq.apps.instastudycategories.Constants.PACKAGE_INSTAGRAM;

public class UserActivity extends AppCompatActivity {

    private final Activity clazz = this;
    private UserAdapter userAdapter;
    private Intent intent;

    private ListView listView;
    private CrystalPreloader preloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user);
        listView = (ListView) findViewById(R.id.user_listview);
        preloader = (CrystalPreloader) findViewById(R.id.user_preloader);

        intent = getIntent();
        String subcategoryId = intent.getStringExtra(INTENT_SUBCATEGORY_ID);

        if (Utils.isEmpty(subcategoryId)) {
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
                        Utils.log(Utils.LOG_DEBUG, clazz, "Intent is available");
                        clazz.startActivity(nextIntent);
                    } else {
                        Utils.log(Utils.LOG_DEBUG, clazz, "Intent is NOT available");
                        clazz.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL + selected)));
                    }
                }
            });

            new ProcessUsers().execute(subcategoryId);
        }
    }

    private class ProcessUsers extends AsyncTask<String, Void, List<User>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            preloader.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<User> doInBackground(String... params) {
            String subcategoryId = params[0];
            List<User> users = new ArrayList<>();

            try {
                Thread.sleep(1000); // FIXME: showing preloader, REMOVE
                users = Api.getUsersBySubcategoryId(subcategoryId);
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

}
