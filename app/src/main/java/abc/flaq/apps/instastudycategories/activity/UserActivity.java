package abc.flaq.apps.instastudycategories.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.crystal.crystalpreloaders.widgets.CrystalPreloader;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.adapter.ChatAdapter;
import abc.flaq.apps.instastudycategories.adapter.UserAdapter;
import abc.flaq.apps.instastudycategories.api.Api;
import abc.flaq.apps.instastudycategories.general.Session;
import abc.flaq.apps.instastudycategories.general.WebSocketClientSide;
import abc.flaq.apps.instastudycategories.helper.Utils;
import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;
import abc.flaq.apps.instastudycategories.pojo.User;
import abc.flaq.apps.instastudycategories.pojo.WebSocketMessage;

import static abc.flaq.apps.instastudycategories.helper.Constants.INSTAGRAM_URL;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_FOREIGN_ID;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_NAME;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_FOREIGN_ID;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_NAME;

public class UserActivity extends SessionActivity {

    private final AppCompatActivity clazz = this;
    private CoordinatorLayout layout;
    private ListView listView;
    private UserAdapter userAdapter;
    private CrystalPreloader preloader;

    private List<User> users = new ArrayList<>();
    private Menu mainMenu;
    private String parentForeignId;
    private Boolean isCategory = false;
    private Boolean hasJoined;
    private Boolean isApiWorking = false;
    private WebSocketClientSide webSocket;
    private Dialog chatDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        layout = (CoordinatorLayout) findViewById(R.id.user_layout);
        listView = (ListView) findViewById(R.id.user_list);
        preloader = (CrystalPreloader) findViewById(R.id.user_preloader);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.user_fab);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View chatView = inflater.inflate(R.layout.activity_user_chat, null);
        ListView chatList = (ListView) chatView.findViewById(R.id.user_chat_list);
        final TextView chatInput = (TextView) chatView.findViewById(R.id.user_chat_input);
        ImageButton sendButton = (ImageButton) chatView.findViewById(R.id.user_chat_input_send);

        ChatAdapter chatAdapter = new ChatAdapter(clazz, new ArrayList<WebSocketMessage>());
        chatList.setAdapter(chatAdapter);

        chatDialog = new Dialog(clazz, R.style.DialogTheme);
        chatDialog.setContentView(chatView);
        Window chatWindow = chatDialog.getWindow();
        if (Utils.isNotEmpty(chatWindow)) {
            // Scroll list when keyboard is shown
            chatWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        Intent intent = getIntent();
        String categoryForeignId = intent.getStringExtra(INTENT_CATEGORY_FOREIGN_ID);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                User selected = userAdapter.getItem(position);
                //Utils.showQuickInfo(layout, getString(R.string.ig_profile_open) + selected.getUsername() + "...");
                Intent nextIntent = Utils.getInstagramIntent(selected.getUsername());

                if (Utils.isIntentAvailable(clazz, nextIntent)) {
                    Utils.logDebug(clazz, "Instagram intent is available");
                    clazz.startActivity(nextIntent);
                } else {
                    Utils.logDebug(clazz, "Instagram intent is NOT available");
                    clazz.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL + selected.getUsername())));
                }
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.isEmpty(Session.getInstance().getUser())) {
                    Utils.showLoginError(layout, getString(R.string.error_chat_login));
                } else {
                    showChatDialog();
                }
            }
        });
        chatDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (chatInput.hasFocus()) {
                    chatInput.clearFocus();
                }
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.isNotEmpty(chatInput.getText())) {
                    webSocket.sendMessage(chatInput.getText().toString());
                    chatInput.setText(null);
                }
            }
        });

        webSocket = WebSocketClientSide.createWebSocketClientSide(clazz, layout, chatAdapter);

        // Check if parentForeignId is from newCategory or subcategory
        if (Utils.isNotEmpty(categoryForeignId)) {
            isCategory = true;
            parentForeignId = categoryForeignId;
            String categoryParentName = Utils.getStringByCategoryName(clazz, intent.getStringExtra(INTENT_CATEGORY_NAME));
            Utils.setActionBarTitle(clazz, categoryParentName, null);
        } else {
            String subcategoryForeignId = intent.getStringExtra(INTENT_SUBCATEGORY_FOREIGN_ID);
            if (Utils.isNotEmpty(subcategoryForeignId)) {
                parentForeignId = subcategoryForeignId;
                String categoryParentName = Utils.getStringByCategoryName(clazz, intent.getStringExtra(INTENT_CATEGORY_NAME));
                String subcategoryParentName = Utils.getStringBySubcategoryName(clazz, intent.getStringExtra(INTENT_SUBCATEGORY_NAME));
                Utils.setActionBarTitle(clazz, subcategoryParentName, categoryParentName);
            }
        }

        if (Utils.isEmpty(parentForeignId)) {
            Utils.showConnectionError(layout, "Empty 'categoryId' and 'subcategoryId'");
        } else {
            new ProcessUsers().execute();
        }
    }

    @Override
    protected void onDestroy() {
        webSocket.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        setMainMenuVisibility(menu);
        menu.findItem(R.id.menu_suggest).setVisible(false);
        menu.findItem(R.id.menu_sort).setVisible(users.size() > 1);
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
        if (Utils.isNotEmpty(mainMenu)) {
            if (!isCategory && Utils.isNotEmpty(Session.getInstance().getUser())) {
                mainMenu.findItem(R.id.menu_join).setVisible(!joined);
                mainMenu.findItem(R.id.menu_leave).setVisible(joined);
            }
            mainMenu.findItem(R.id.menu_sort).setVisible(users.size() > 1);
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
                new ProcessAddUserToSubcategory().execute();
                break;
            case R.id.menu_leave:
                new ProcessRemoveUserFromSubcategory().execute();
                break;
            case R.id.menu_sort:
                PopupMenu popup = new PopupMenu(clazz, findViewById(R.id.menu_sort));
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.sort_menu_joined_date:
                                Utils.sortByJoinedDate(users, false);
                                break;
                            case R.id.sort_menu_joined_date_rev:
                                Utils.sortByJoinedDate(users, true);
                                break;
                            case R.id.sort_menu_followers:
                                Utils.sortByFollowers(users, false);
                                break;
                            case R.id.sort_menu_followers_rev:
                                Utils.sortByFollowers(users, true);
                                break;
                            case R.id.sort_menu_alphabetically:
                                Utils.sortAlphabetically(users, false);
                                break;
                            case R.id.sort_menu_alphabetically_rev:
                                Utils.sortAlphabetically(users, true);
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

    private void showChatDialog() {
        chatDialog.show();
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
                    Utils.logDebug(clazz, user.toString());
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
                Utils.showConnectionError(layout, getString(R.string.error_users_load));
            } catch (IOException e) {
                Utils.logError(clazz, "IOException: " + e.getMessage());
                Utils.showConnectionError(layout, getString(R.string.error_users_load));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            isApiWorking = false;
            preloader.setVisibility(View.INVISIBLE);

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
            listView.setVisibility(View.INVISIBLE);
            preloader.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean result = Boolean.FALSE;
            try {
                if (isCategory) {
                    Category category = Api.getCategoryById(Utils.undoForeignId(parentForeignId));
                    result = Api.addUserToCategory(Session.getInstance().getUser(), category);
                } else {
                    Subcategory subcategory = Api.getSubcategoryById(Utils.undoForeignId(parentForeignId));
                    result = Api.addUserToSubcategory(Session.getInstance().getUser(), subcategory);
                }
            } catch (JSONException e) {
                Utils.logError(clazz, "JSONException: " + e.getMessage());
                Utils.showConnectionError(layout, getString(R.string.error_user_subcategory_add));
            } catch (IOException e) {
                Utils.logError(clazz, "IOException: " + e.getMessage());
                Utils.showConnectionError(layout, getString(R.string.error_user_subcategory_add));
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            isApiWorking = false;
            preloader.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);

            if (result) {
                Utils.showInfo(layout,
                        getString(R.string.user_subcategory_add_success)
                );

                users.add(0, Session.getInstance().getUser());
                userAdapter.notifyDataSetChanged();
                hasJoined = true;
                setCategoryMenuVisibility(true);

                Session.getInstance().setSubcategoryChanged(true);
            } else {
                Utils.showConnectionError(layout, getString(R.string.error_user_subcategory_add));
            }
        }
    }

    private class ProcessRemoveUserFromSubcategory extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isApiWorking = true;
            listView.setVisibility(View.INVISIBLE);
            preloader.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean result = Boolean.FALSE;
            try {
                if (isCategory) {
                    Category category = Api.getCategoryById(Utils.undoForeignId(parentForeignId));
                    result = Api.removeUserFromCategory(Session.getInstance().getUser(), category);
                } else {
                    Subcategory subcategory = Api.getSubcategoryById(Utils.undoForeignId(parentForeignId));
                    result = Api.removeUserFromSubcategory(Session.getInstance().getUser(), subcategory);
                }
            } catch (JSONException e) {
                Utils.logError(clazz, "JSONException: " + e.getMessage());
                Utils.showConnectionError(layout, getString(R.string.error_user_subcategory_remove));
            } catch (IOException e) {
                Utils.logError(clazz, "IOException: " + e.getMessage());
                Utils.showConnectionError(layout, getString(R.string.error_user_subcategory_remove));
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            isApiWorking = false;
            preloader.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);

            if (result) {
                Utils.showInfo(layout,
                        getString(R.string.user_subcategory_remove_success)
                );

                Session.getInstance().getUser().removeFromList(users);
                userAdapter.notifyDataSetChanged();
                hasJoined = false;
                setCategoryMenuVisibility(false);

                Session.getInstance().setSubcategoryChanged(true);
            } else {
                Utils.showConnectionError(layout, getString(R.string.error_user_subcategory_remove));
            }
        }
    }

}
