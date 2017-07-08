package abc.flaq.apps.instastudycategories.activity;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crystal.crystalpreloaders.widgets.CrystalPreloader;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import abc.flaq.apps.instastudycategories.BuildConfig;
import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.adapter.ChatAdapter;
import abc.flaq.apps.instastudycategories.adapter.UserAdapter;
import abc.flaq.apps.instastudycategories.api.Api;
import abc.flaq.apps.instastudycategories.design.Decorator;
import abc.flaq.apps.instastudycategories.general.Session;
import abc.flaq.apps.instastudycategories.general.WebSocketClientSide;
import abc.flaq.apps.instastudycategories.helper.Utils;
import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;
import abc.flaq.apps.instastudycategories.pojo.User;
import abc.flaq.apps.instastudycategories.pojo.WebSocketMessage;

import static abc.flaq.apps.instastudycategories.R.id.menu_sort_alphabet;
import static abc.flaq.apps.instastudycategories.R.id.menu_sort_followers;
import static abc.flaq.apps.instastudycategories.helper.Constants.ADMOB_TEST_DEVICE_ID;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_FOREIGN_ID;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_NAME;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_USERS_SIZE;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_FOREIGN_ID;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_NAME;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_USERS_SIZE;
import static abc.flaq.apps.instastudycategories.helper.Constants.WEB_SOCKET_MAX_MESSAGES;
import static android.view.inputmethod.EditorInfo.IME_ACTION_SEND;

public class UserActivity extends SessionActivity {

    private final AppCompatActivity clazz = this;
    private CoordinatorLayout layout;
    private ListView listView;
    private UserAdapter userAdapter;
    private CrystalPreloader preloader;
    private AdView adView;

    private List<User> users = new ArrayList<>();
    private Menu mainMenu;
    private String parentForeignId;
    private Integer parentUsersSize;
    private String subcategoryParentName;
    private Boolean isCategory = false;
    private Boolean hasJoined;
    private Boolean isApiWorking = false;
    private WebSocketClientSide webSocket;
    private Integer messageCount = WEB_SOCKET_MAX_MESSAGES;
    private Dialog chatDialog;
    private Boolean sortDate = false;
    private Boolean sortFollowers = true;
    private Boolean sortAlphabet = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Toolbar toolbar = (Toolbar) findViewById(R.id.user_toolbar);
        setSupportActionBar(toolbar);

        layout = (CoordinatorLayout) findViewById(R.id.user_layout);
        preloader = (CrystalPreloader) findViewById(R.id.user_preloader);

        listView = (ListView) findViewById(R.id.user_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                User selected = userAdapter.getItem(position);
                showOtherUserInfoDialog(clazz, selected);
            }
        });

        adView = (AdView) findViewById(R.id.user_adView);
        AdRequest adRequest = (
                BuildConfig.IS_DEBUG ?
                        new AdRequest.Builder().addTestDevice(ADMOB_TEST_DEVICE_ID).build() :
                        new AdRequest.Builder().build()
        );
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (listView.getPaddingBottom() == 0) {
                    // Move up to show adView
                    if (adView.getHeight() == 0) {
                        listView.setPadding(0, 0, 0, Utils.getDpFromPx(clazz, 50));
                    } else {
                        listView.setPadding(0, 0, 0, adView.getHeight());
                    }
                }
            }
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                if (listView.getPaddingBottom() == 0) {
                    // Move up to show adView
                    if (adView.getHeight() == 0) {
                        listView.setPadding(0, 0, 0, Utils.getDpFromPx(clazz, 50));
                    } else {
                        listView.setPadding(0, 0, 0, adView.getHeight());
                    }
                }
            }
        });

        Intent intent = getIntent();
        String categoryForeignId = intent.getStringExtra(INTENT_CATEGORY_FOREIGN_ID);
        // Check if parentForeignId is from newCategory or subcategory
        if (Utils.isNotEmpty(categoryForeignId)) {
            isCategory = true;
            parentForeignId = categoryForeignId;
            parentUsersSize = intent.getIntExtra(INTENT_CATEGORY_USERS_SIZE, -1);
            String categoryParentName = Utils.getCategoryString(clazz, intent.getStringExtra(INTENT_CATEGORY_NAME));
            Decorator.setActionBarTitle(clazz, categoryParentName, null);
        } else {
            String subcategoryForeignId = intent.getStringExtra(INTENT_SUBCATEGORY_FOREIGN_ID);
            if (Utils.isNotEmpty(subcategoryForeignId)) {
                parentForeignId = subcategoryForeignId;
                parentUsersSize = intent.getIntExtra(INTENT_SUBCATEGORY_USERS_SIZE, -1);
                String categoryParentName = Utils.getCategoryString(clazz, Session.getInstance().getCategoryName());
                subcategoryParentName = Utils.getSubcategoryString(clazz, intent.getStringExtra(INTENT_SUBCATEGORY_NAME));
                Decorator.setActionBarTitle(clazz, subcategoryParentName, categoryParentName);
            }
        }

        if (Utils.isEmpty(parentForeignId)) {
            handleConnectionError("Empty 'categoryId' and 'subcategoryId'", getString(R.string.error_users_load));
        } else {
            new ProcessUsers().execute();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        adView.resume();
    }
    @Override
    protected void onDestroy() {
        if (Utils.isNotEmpty(webSocket)) {
            webSocket.close();
        }
        super.onDestroy();
    }
    @Override
    protected void onPause() {
        if (Utils.isNotEmpty(chatDialog)) {
            chatDialog.dismiss();
        }
        adView.pause();
        super.onPause();
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
                Snackbar.make(Utils.findSnackbarView(layout), "Czy na pewno chcesz opuścić kategorię \"" + subcategoryParentName + "\"?", Snackbar.LENGTH_LONG)
                        .setActionTextColor(ContextCompat.getColor(clazz, R.color.colorError))
                        .setAction("OPUŚĆ", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new ProcessRemoveUserFromSubcategory().execute();
                            }
                        }).show();
                break;
            case R.id.menu_sort:
                // submenu only
                break;
            case R.id.menu_sort_joined_date:
                if (sortDate) {
                    item.setTitle("Od najstarszego");
                } else {
                    item.setTitle("Od najnowszego");
                }
                sortDate = !sortDate;
                Utils.sortByJoinedDate(users, sortDate);
                userAdapter.notifyDataSetChanged();
                break;
            case menu_sort_followers:
                if (sortFollowers) {
                    item.setTitle("Najmniej followersów");
                } else {
                    item.setTitle("Najwięcej followersów");
                }
                sortFollowers = !sortFollowers;
                Utils.sortByFollowers(users, sortFollowers);
                userAdapter.notifyDataSetChanged();
                break;
            case menu_sort_alphabet:
                if (sortAlphabet) {
                    item.setTitle("Alfabetycznie (Z - A)");
                } else {
                    item.setTitle("Alfabetycznie (A - Z)");
                }
                sortAlphabet = !sortAlphabet;
                Utils.sortAlphabetically(users, sortAlphabet);
                userAdapter.notifyDataSetChanged();
                break;
            case R.id.menu_login:
                return super.onOptionsItemSelected(item);
            default:
                break;
        }
        return true;
    }

    public CoordinatorLayout getLayout() {
        return layout;
    }

    private void initWebSocket() {
        final ArrayList<WebSocketMessage> messages = new ArrayList<>();
        final ChatAdapter chatAdapter = new ChatAdapter(clazz, messages);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View chatView = inflater.inflate(R.layout.activity_user_chat, null);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.user_fab);
        final TextView chatHeader = (TextView) chatView.findViewById(R.id.user_chat_header);
        final ListView chatList = (ListView) chatView.findViewById(R.id.user_chat_list);
        final EditText chatInput = (EditText) chatView.findViewById(R.id.user_chat_input);
        final ImageButton sendButton = (ImageButton) chatView.findViewById(R.id.user_chat_input_send);

        chatDialog = new Dialog(clazz, R.style.DialogTheme);
        chatDialog.setContentView(chatView);
        Window chatWindow = chatDialog.getWindow();
        if (Utils.isNotEmpty(chatWindow)) {
            // Scroll list when keyboard is shown
            chatWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

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
                fab.setImageResource(R.drawable.ic_chat_bubble_white_24dp);
            }
        });
        chatInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == IME_ACTION_SEND) {
                    sendButton.performClick();
                    return true;
                }
                return false;
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.isNotEmpty(chatInput.getText())) {
                    messageCount = chatAdapter.getCount();
                    webSocket.sendMessage(chatInput.getText().toString());
                    chatInput.setText(null);
                }
            }
        });

        chatList.setAdapter(chatAdapter);
        chatList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                WebSocketMessage selected = chatAdapter.getItem(position);
                String text = ("@" + selected.getName() + " ");
                if (Utils.isNotEmpty(chatInput.getText())) {
                    chatInput.setText(chatInput.getText().toString().trim() + " " + text);
                } else {
                    chatInput.setText(text);
                }
                chatInput.setSelection(chatInput.getText().length());
            }
        });
        chatList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                WebSocketMessage selected = chatAdapter.getItem(position);
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("text", selected.getMessage());
                clipboard.setPrimaryClip(clipData);
                Toast.makeText(clazz, getString(R.string.message_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        webSocket = WebSocketClientSide.createWebSocketClientSide(clazz);
        webSocket.setWebSocketListener(new WebSocketClientSide.WebSocketListener() {
            @Override
            public void onConnectionOpen() {
                fab.setClickable(true);
            }

            @Override
            public void onConnectionError(String logMessage) {
                fab.setClickable(false);
                chatDialog.dismiss();
                handleConnectionError(logMessage, getString(R.string.error_chat_connection));
            }

            @Override
            public void onMessageTypeMsg(final WebSocketMessage message) {
                messages.add(message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatAdapter.notifyDataSetChanged();
                    }
                });
                if (chatAdapter.getCount() > messageCount) {
                    fab.setImageResource(R.drawable.ic_chat_white_24dp);
                }
            }

            @Override
            public void onMessageTypeNum(final Integer totalUsers) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatHeader.setText(getString(R.string.chat_total_users, totalUsers));
                    }
                });
            }
        });
        webSocket.connect();
    }

    private void showChatDialog() {
        chatDialog.show();
    }

    private void handleConnectionError(String logMessage, String showMessage) {
        //isApiWorking = true;
        Utils.showConnectionError(layout, logMessage, showMessage);
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
                Api.getAllUsers(true);
                if (isCategory) {
                    users = Api.getUsersByCategoryForeignId(parentForeignId);
                } else {
                    users = Api.getUsersBySubcategoryForeignId(parentForeignId);
                }
                hasJoined = false;
                for (User user : users) {
                    if (Utils.isNotEmpty(Session.getInstance().getUser()) &&
                            user.getId().equals(Session.getInstance().getUser().getId())) {
                        if (isCategory) {
                            hasJoined = Session.getInstance().getUser().getCategories().contains(parentForeignId);
                        } else {
                            hasJoined = Session.getInstance().getUser().getSubcategories().contains(parentForeignId);
                        }
                    }
                    user.calculateJoinDate(parentForeignId);
                }
                Collections.sort(users);
            } catch (JSONException e) {
                handleConnectionError("JSONException: " + e.getMessage(), getString(R.string.error_users_load));
            } catch (IOException e) {
                handleConnectionError("IOException: " + e.getMessage(), getString(R.string.error_users_load));
            } catch (ParseException e) {
                handleConnectionError("ParseException: " + e.getMessage(), getString(R.string.error_users_load));
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

            if (parentUsersSize >= 0 && users.size() != parentUsersSize) {
                Session.getInstance().setSubcategoryChanged(true);
            }

            initWebSocket();
        }
    }

    private class ProcessAddUserToSubcategory extends AsyncTask<Void, Void, Boolean> {
        int usersSize;

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
                    usersSize = category.getUsersSize();
                    result = Api.addUserToCategory(Session.getInstance().getUser(), category);
                } else {
                    Subcategory subcategory = Api.getSubcategoryById(Utils.undoForeignId(parentForeignId));
                    usersSize = subcategory.getUsersSize();
                    result = Api.addUserToSubcategory(Session.getInstance().getUser(), subcategory);
                }

                Session.getInstance().getUser().calculateJoinDate(parentForeignId);
            } catch (JSONException e) {
                handleConnectionError("JSONException: " + e.getMessage(), getString(R.string.error_user_subcategory_add));
            } catch (IOException e) {
                handleConnectionError("IOException: " + e.getMessage(), getString(R.string.error_user_subcategory_add));
            } catch (ParseException e) {
                handleConnectionError("ParseException: " + e.getMessage(), getString(R.string.error_user_subcategory_add));
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
                if (usersSize < 10) {
                    Utils.showInfo(layout,
                            getString(R.string.user_subcategory_add_success_short)
                    );
                } else {
                    Utils.showInfo(layout,
                            getString(R.string.user_subcategory_add_success_short_more)
                    );
                }

                users.add(0, Session.getInstance().getUser());
                userAdapter.notifyDataSetChanged();
                hasJoined = true;
                setCategoryMenuVisibility(true);

                Session.getInstance().setSubcategoryChanged(true);
            } else {
                handleConnectionError("ProcessAddUserToSubcategory - empty result", getString(R.string.error_user_subcategory_add));
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
                handleConnectionError("JSONException: " + e.getMessage(), getString(R.string.error_user_subcategory_remove));
            } catch (IOException e) {
                handleConnectionError("IOException: " + e.getMessage(), getString(R.string.error_user_subcategory_remove));
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
                /*Utils.showInfo(layout,
                        getString(R.string.user_subcategory_remove_success)
                );*/

                Session.getInstance().getUser().removeFromList(users);
                userAdapter.notifyDataSetChanged();
                hasJoined = false;
                setCategoryMenuVisibility(false);

                Session.getInstance().setSubcategoryChanged(true);
            } else {
                handleConnectionError("ProcessRemoveUserFromSubcategory - empty result", getString(R.string.error_user_subcategory_remove));
            }
        }
    }

}
