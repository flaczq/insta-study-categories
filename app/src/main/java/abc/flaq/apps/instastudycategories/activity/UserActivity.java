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

public class UserActivity extends SessionActivity {

    private final Activity clazz = this;
    private View rootView;
    private ListView listView;
    private UserAdapter userAdapter;

    private Menu mainMenu;
    private String selectedForeignId;
    private Boolean isCategory = false;
    private Boolean hasJoined;
    private Boolean isSnackbarShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = findViewById(android.R.id.content);
        setContentView(R.layout.activity_user);
        listView = (ListView) findViewById(R.id.user_list);

        Intent intent = getIntent();
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
            Utils.showError(rootView, "No category or subcategory id");
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
        setMainMenuVisibility(menu);
        menu.findItem(R.id.menu_suggest).setVisible(false);
        if (Utils.isEmpty(hasJoined) || Utils.isEmpty(Session.getInstance().getUser())) {
            menu.findItem(R.id.menu_join).setVisible(false);
            menu.findItem(R.id.menu_leave).setVisible(false);
        } else {
            menu.findItem(R.id.menu_join).setVisible(!hasJoined && !isCategory);
            menu.findItem(R.id.menu_leave).setVisible(hasJoined && !isCategory);
        }
        mainMenu = menu;
        return true;
    }
    private void setCategoryMenuVisibility(Boolean joined) {
        if (Utils.isNotEmpty(mainMenu) && !isCategory && Utils.isNotEmpty(Session.getInstance().getUser())) {
            mainMenu.findItem(R.id.menu_join).setVisible(!joined);
            mainMenu.findItem(R.id.menu_leave).setVisible(joined);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isSnackbarShown) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_suggest:
                // not available from here
                break;
            case R.id.menu_join:
                Utils.showInfo(rootView, "Dodawanie do podkategorii...");
                new ProcessAddUserToSubcategory().execute();
                break;
            case R.id.menu_leave:
                Utils.showInfo(rootView, "Usuwanie z podkategorii...");
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
            isSnackbarShown = true;
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
                    }
                }
                if (Utils.isEmpty(hasJoined)) {
                    hasJoined = false;
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
            isSnackbarShown = false;

            userAdapter = new UserAdapter(clazz, users);
            listView.setAdapter(userAdapter);
            setCategoryMenuVisibility(hasJoined);
        }
    }

    private class ProcessAddUserToSubcategory extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isSnackbarShown = true;
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
            isSnackbarShown = false;

            if (result) {
                Utils.showInfo(rootView, "Dodano użytkownika do podkategorii");
                userAdapter.addItem(Session.getInstance().getUser());
                userAdapter.notifyDataSetChanged();
                hasJoined = true;
                setCategoryMenuVisibility(true);
            } else {
                Utils.showError(rootView, "Can't add user to subcategory");
            }
        }
    }

    private class ProcessRemoveUserFromSubcategory extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isSnackbarShown = true;
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
            isSnackbarShown = false;

            if (result) {
                Utils.showInfo(rootView, "Usunięto użytkownika z podkategorii");
                userAdapter.removeItem(Session.getInstance().getUser());
                userAdapter.notifyDataSetChanged();
                hasJoined = false;
                setCategoryMenuVisibility(false);
            } else {
                Utils.showError(rootView, "Can't remove user from subcategory");
            }
        }
    }

}
