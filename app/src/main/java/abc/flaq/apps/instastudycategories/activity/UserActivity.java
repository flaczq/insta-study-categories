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
import abc.flaq.apps.instastudycategories.utils.Session;
import abc.flaq.apps.instastudycategories.utils.Utils;

import static abc.flaq.apps.instastudycategories.utils.Constants.INSTAGRAM_URL;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_FOREIGN_ID;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_SUBCATEGORY_FOREIGN_ID;
import static abc.flaq.apps.instastudycategories.utils.Constants.PACKAGE_INSTAGRAM;

public class UserActivity extends MenuActivity {

    private final Activity clazz = this;
    private UserAdapter userAdapter;
    private Intent intent;
    private ListView listView;
    private CrystalPreloader preloader;

    private Menu mainMenu;
    private String selectedForeignId;
    private Boolean isCategory = false;
    private Boolean hasJoined = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        listView = (ListView) findViewById(R.id.user_list);
        preloader = (CrystalPreloader) findViewById(R.id.user_preloader);

        intent = getIntent();
        // Check if passed selectedForeignId is from category or subcategory
        String categoryForeignId = intent.getStringExtra(INTENT_CATEGORY_FOREIGN_ID);
        selectedForeignId = null;
        if (Utils.isNotEmpty(categoryForeignId)) {
            isCategory = true;
            selectedForeignId = categoryForeignId;
        } else {
            String subcategoryForeignId = intent.getStringExtra(INTENT_SUBCATEGORY_FOREIGN_ID);
            if (Utils.isNotEmpty(subcategoryForeignId)) {
                selectedForeignId = subcategoryForeignId;
            }
        }

        if (Utils.isEmpty(selectedForeignId)) {
            Utils.afterError(clazz, "No category or subcategory id");
            finish();
        } else {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                    User selected = userAdapter.getItem(position);
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
        menu.findItem(R.id.menu_suggest).setVisible(false);
        menu.findItem(R.id.menu_join).setVisible(!isCategory && !hasJoined);
        menu.findItem(R.id.menu_leave).setVisible(!isCategory && hasJoined);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (preloader.isShown()) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_suggest:
                // not available from here
                break;
            case R.id.menu_join:
                Utils.showMessage(clazz, "joining");
                new ProcessAddUserToSubcategory().execute();
                break;
            case R.id.menu_leave:
                Utils.showMessage(clazz, "leaving");
                new ProcessRemoveUserFromSubcategory().execute();
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
                    users = Api.getUsersByCategoryForeignId(selectedForeignId);
                } else {
                    users = Api.getUsersBySubcategoryForeignId(selectedForeignId);
                }
                for (User user : users) {
                    Utils.logInfo(clazz, user.toString());
                    if (Utils.isNotEmpty(Session.getInstance().getUser()) &&
                            user.getId().equals(Session.getInstance().getUser().getId())) {
                        hasJoined = (Session.getInstance().getUser().getCategories().contains(selectedForeignId) ||
                                Session.getInstance().getUser().getSubcategories().contains(selectedForeignId));
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

    private class ProcessAddUserToSubcategory extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean result = Boolean.FALSE;
            try {
                if (isCategory) {
                    result = Api.addUserToCategory(Session.getInstance().getUser(), selectedForeignId);
                } else {
                    result = Api.addUserToSubcategory(Session.getInstance().getUser(), selectedForeignId);
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
                Utils.showMessage(clazz, "User successfully added to the subcategory");
                if (!isCategory) {
                    mainMenu.findItem(R.id.menu_join).setVisible(false);
                    mainMenu.findItem(R.id.menu_leave).setVisible(true);
                }
                userAdapter.addItem(Session.getInstance().getUser());
                userAdapter.notifyDataSetChanged();
            } else {
                Utils.afterError(clazz, "Can't add user to subcategory");
            }
        }
    }

    private class ProcessRemoveUserFromSubcategory extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean result = Boolean.FALSE;
            try {
                if (isCategory) {
                    result = Api.removeUserFromCategory(Session.getInstance().getUser(), selectedForeignId);
                } else {
                    result = Api.removeUserFromSubcategory(Session.getInstance().getUser(), selectedForeignId);
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
                Utils.showMessage(clazz, "User successfully removed from the subcategory");
                if (!isCategory) {
                    mainMenu.findItem(R.id.menu_join).setVisible(true);
                    mainMenu.findItem(R.id.menu_leave).setVisible(false);
                }
                userAdapter.removeItem(Session.getInstance().getUser());
                userAdapter.notifyDataSetChanged();
            } else {
                Utils.afterError(clazz, "Can't remove user from subcategory");
            }
        }
    }

}
