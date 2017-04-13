package abc.flaq.apps.instastudycategories.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.crystal.crystalpreloaders.widgets.CrystalPreloader;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.adapter.UserAdapter;
import abc.flaq.apps.instastudycategories.pojo.User;
import abc.flaq.apps.instastudycategories.utils.Api;
import abc.flaq.apps.instastudycategories.utils.Utils;
import abc.flaq.apps.instastudycategories.utils.Session;

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

    private Menu mainMenu;
    private String selectedId;
    private Boolean isCategory = false;
    private Boolean hasJoined = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        listView = (ListView) findViewById(R.id.user_list);
        preloader = (CrystalPreloader) findViewById(R.id.user_preloader);

        intent = getIntent();
        // Check if passed selectedId is from category or subcategory
        String categoryId = intent.getStringExtra(INTENT_CATEGORY_ID);
        selectedId = null;
        if (Utils.isNotEmpty(categoryId)) {
            isCategory = true;
            selectedId = categoryId;
        } else {
            String subcategoryId = intent.getStringExtra(INTENT_SUBCATEGORY_ID);
            if (Utils.isNotEmpty(subcategoryId)) {
                selectedId = subcategoryId;
            }
        }

        if (Utils.isEmpty(selectedId)) {
            Utils.afterError(clazz, "No category or subcategory id");
            finish();
        } else {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                    User selected = userAdapter.getItem(position);
                    Utils.logDebug(clazz, "Selected position: " + position);
                    Uri instagramUri = Uri.parse(INSTAGRAM_URL + "_u/" + selected.getUsername());
                    Intent nextIntent = new Intent(Intent.ACTION_VIEW, instagramUri);
                    nextIntent.setPackage(PACKAGE_INSTAGRAM);

                    if (Utils.isIntentAvailable(clazz, nextIntent)) {
                        Utils.logDebug(clazz, "Instagram intent is available");
                        clazz.startActivity(nextIntent);
                    } else {
                        Utils.logDebug(clazz, "Instagram intent is NOT available");
                        clazz.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL + selected.getUsername())));
                    }
                }
            });

            new ProcessUsers().execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        mainMenu = menu;
        menu.findItem(R.id.menu_add).setVisible(false);
        menu.findItem(R.id.menu_join).setVisible(!hasJoined);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (preloader.isShown()) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_add:
                // not available from here
                break;
            case R.id.menu_join:
                Utils.showMessage(clazz, "joining");
                new ProcessAddUserToCategory().execute();
                break;
            case R.id.menu_info:
                return super.onOptionsItemSelected(item);
            case R.id.menu_login:
                return super.onOptionsItemSelected(item);
            default:
                break;
        }
        return true;
    }

    private class ProcessUsers extends AsyncTask<Void, Void, Void> {
        private List<User> users;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            preloader.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (isCategory) {
                    users = Api.getUsersByCategoryId(selectedId);
                } else {
                    users = Api.getUsersBySubcategoryId(selectedId);
                }
                for (User user : users) {
                    Utils.logInfo(clazz, user.toString());
                    if (Utils.isNotEmpty(Session.getInstance().getUser()) &&
                            user.getId().equals(Session.getInstance().getUser().getId())) {
                        hasJoined = (Session.getInstance().getUser().getCategories().contains(selectedId) ||
                                Session.getInstance().getUser().getSubcategories().contains(selectedId));
                        invalidateOptionsMenu();
                    }
                }
            } catch (JSONException e) {
                Utils.logError(clazz, "JSONException: " + e.toString());
            } catch (IOException e) {
                Utils.logError(clazz, "IOException: " + e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            preloader.setVisibility(View.GONE);
            userAdapter = new UserAdapter(clazz, users);
            listView.setAdapter(userAdapter);
        }
    }

    private class ProcessAddUserToCategory extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean result = Boolean.FALSE;
            try {
                if (isCategory) {
                    result = Api.addUserToCategory(Session.getInstance().getUser(), selectedId);
                } else {
                    result = Api.addUserToSubcategory(Session.getInstance().getUser(), selectedId);
                }
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
                Utils.showMessage(clazz, "User successfully added to the category");
                userAdapter.addItem(Session.getInstance().getUser());
                userAdapter.notifyDataSetChanged();
                mainMenu.findItem(R.id.menu_join).setVisible(false);
            } else {
                Utils.afterError(clazz, "Can't add user to category");
            }
        }
    }

}
