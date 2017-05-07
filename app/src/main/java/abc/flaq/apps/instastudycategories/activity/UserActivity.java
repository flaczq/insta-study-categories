package abc.flaq.apps.instastudycategories.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.crystal.crystalpreloaders.widgets.CrystalPreloader;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.adapter.UserAdapter;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;
import abc.flaq.apps.instastudycategories.pojo.User;
import abc.flaq.apps.instastudycategories.utils.Api;
import abc.flaq.apps.instastudycategories.utils.Session;
import abc.flaq.apps.instastudycategories.utils.Utils;

import static abc.flaq.apps.instastudycategories.utils.Constants.INSTAGRAM_URL;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_FOREIGN_ID;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_NAME;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_SUBCATEGORY_FOREIGN_ID;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_SUBCATEGORY_NAME;
import static abc.flaq.apps.instastudycategories.utils.Constants.INSTAGRAM_PACKAGE;

public class UserActivity extends SessionActivity {

    private final AppCompatActivity clazz = this;
    private View rootView;
    private ListView listView;
    private UserAdapter userAdapter;
    private CrystalPreloader preloader;

    private List<User> users = new ArrayList<>();
    private Menu mainMenu;
    private String parentForeignId;
    private String parentName;
    private Boolean isCategory = false;
    private Boolean hasJoined;
    private Boolean isApiWorking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        rootView = findViewById(android.R.id.content);
        listView = (ListView) findViewById(R.id.user_list);
        preloader = (CrystalPreloader) findViewById(R.id.user_preloader);

        Intent intent = getIntent();
        // Check if parentForeignId is from newCategory or subcategory
        String categoryForeignId = intent.getStringExtra(INTENT_CATEGORY_FOREIGN_ID);
        parentForeignId = null;
        if (Utils.isNotEmpty(categoryForeignId)) {
            isCategory = true;
            parentForeignId = categoryForeignId;
            parentName = Utils.getStringByCategoryName(clazz, intent.getStringExtra(INTENT_CATEGORY_NAME));
        } else {
            String subcategoryForeignId = intent.getStringExtra(INTENT_SUBCATEGORY_FOREIGN_ID);
            if (Utils.isNotEmpty(subcategoryForeignId)) {
                parentForeignId = subcategoryForeignId;
                parentName = Utils.getStringBySubcategoryName(clazz, intent.getStringExtra(INTENT_SUBCATEGORY_NAME));
            }
        }

        if (Utils.isEmpty(parentForeignId)) {
            Utils.showError(rootView, "Brak id kategorii lub podkategorii");
        } else {
            Utils.setActionBarTitle(clazz, parentName);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                    Utils.showQuickInfo(rootView, "Otwieranie profilu Instagram...");
                    User selected = userAdapter.getItem(position);
                    Uri instagramUri = Uri.parse(INSTAGRAM_URL + "_u/" + selected.getUsername());
                    Intent nextIntent = new Intent(Intent.ACTION_VIEW, instagramUri);
                    nextIntent.setPackage(INSTAGRAM_PACKAGE);

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
        if (!isCategory && Utils.isNotEmpty(mainMenu) && Utils.isNotEmpty(Session.getInstance().getUser())) {
            mainMenu.findItem(R.id.menu_join).setVisible(!joined);
            mainMenu.findItem(R.id.menu_leave).setVisible(joined);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isApiWorking) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_suggest:
                // not available from here
                break;
            case R.id.menu_join:
                Utils.showQuickInfo(rootView, "Dodawanie do podkategorii...");
                new ProcessAddUserToSubcategory().execute();
                break;
            case R.id.menu_leave:
                Utils.showQuickInfo(rootView, "Usuwanie z podkategorii...");
                new ProcessRemoveUserFromSubcategory().execute();
                break;
            case R.id.menu_sort:
                PopupMenu popup = new PopupMenu(clazz, findViewById(R.id.menu_sort));
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            // TODO: sortowanie
                            case R.id.sort_menu_alphabetically:
                                Collections.reverse(users);
                                break;
                            case R.id.sort_menu_alphabetically_rev:
                                Collections.reverse(users);
                                break;
                            case R.id.sort_menu_date:
                                Collections.reverse(users);
                                break;
                            case R.id.sort_menu_date_rev:
                                Collections.reverse(users);
                                break;
                            default:
                                break;
                        }
                        userAdapter.notifyDataSetChanged();
                        return true;
                    }
                });
                popup.inflate(R.menu.sort_menu);
                popup.show();
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
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isApiWorking = true;
            preloader.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (isCategory) {
                    users = Api.getUsersByCategoryForeignId(parentForeignId);
                } else {
                    users = Api.getUsersBySubcategoryForeignId(parentForeignId);
                }
                hasJoined = false;
                for (User user : users) {
                    Utils.logInfo(clazz, user.toString());
                    if (Utils.isNotEmpty(Session.getInstance().getUser()) &&
                            user.getId().equals(Session.getInstance().getUser().getId())) {
                        if (isCategory) {
                            hasJoined = Session.getInstance().getUser().getCategories().contains(parentForeignId);
                        } else {
                            hasJoined = Session.getInstance().getUser().getSubcategories().contains(parentForeignId);
                        }
                    }
                }
            } catch (JSONException e) {
                Utils.logError(clazz, "JSONException: " + e.getMessage());
            } catch (IOException e) {
                Utils.logError(clazz, "IOException: " + e.getMessage());
                Utils.showConnectionError(rootView, "Błąd pobierania listy użytkowników");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            isApiWorking = false;
            preloader.setVisibility(View.GONE);

            userAdapter = new UserAdapter(clazz, users);
            listView.setAdapter(userAdapter);
            setCategoryMenuVisibility(hasJoined);
        }
    }

    private class ProcessAddUserToSubcategory extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isApiWorking = true;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean result = Boolean.FALSE;
            try {
                if (isCategory) {
                    result = Api.addUserToCategory(Session.getInstance().getUser(), parentForeignId);
                } else {
                    Subcategory subcategory = Api.getSubcategoryById(parentForeignId);
                    result = Api.addUserToSubcategory(Session.getInstance().getUser(), subcategory);
                }
            } catch (JSONException e) {
                Utils.logError(clazz, "JSONException: " + e.getMessage());
            } catch (IOException e) {
                Utils.logError(clazz, "IOException: " + e.getMessage());
                Utils.showConnectionError(rootView, "Błąd dodawania użytkownika do podkategorii");
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            isApiWorking = false;

            if (result) {
                //Utils.showInfo(rootView, "Dodano użytkownika do podkategorii");
                users.add(0, Session.getInstance().getUser());
                userAdapter.notifyDataSetChanged();
                hasJoined = true;
                setCategoryMenuVisibility(true);
                Session.getInstance().setSubcategoryChanged(true);
                Session.getInstance().setCategoryChanged(true);
            } else {
                Utils.showError(rootView, "Dodanie użytkownika do podkategorii zakończone niepowodzeniem");
            }
        }
    }

    private class ProcessRemoveUserFromSubcategory extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isApiWorking = true;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean result = Boolean.FALSE;
            try {
                if (isCategory) {
                    result = Api.removeUserFromCategory(Session.getInstance().getUser(), parentForeignId);
                } else {
                    result = Api.removeUserFromSubcategory(Session.getInstance().getUser(), parentForeignId);
                }
            } catch (JSONException e) {
                Utils.logError(clazz, "JSONException: " + e.getMessage());
            } catch (IOException e) {
                Utils.logError(clazz, "IOException: " + e.getMessage());
                Utils.showConnectionError(rootView, "Błąd dodawania użytkownika do kategorii");
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            isApiWorking = false;

            if (result) {
                //Utils.showInfo(rootView, "Usunięto użytkownika z podkategorii");
                Session.getInstance().getUser().removeFromList(users);
                userAdapter.notifyDataSetChanged();
                hasJoined = false;
                setCategoryMenuVisibility(false);
                Session.getInstance().setSubcategoryChanged(true);
                Session.getInstance().setCategoryChanged(true);
            } else {
                Utils.showError(rootView, "Usunięcie użytkownika z podkategorii zakończone niepowodzeniem");
            }
        }
    }

}
