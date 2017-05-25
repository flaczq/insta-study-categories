package abc.flaq.apps.instastudycategories.activity;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crystal.crystalpreloaders.widgets.CrystalPreloader;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
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

import static abc.flaq.apps.instastudycategories.R.id.menu_sort_alphabet;
import static abc.flaq.apps.instastudycategories.R.id.menu_sort_followers;
import static abc.flaq.apps.instastudycategories.helper.Constants.INSTAGRAM_URL;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_FOREIGN_ID;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_NAME;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_FOREIGN_ID;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_NAME;
import static android.view.inputmethod.EditorInfo.IME_ACTION_SEND;

// FIXME: zalogować się tu i ikonki menu są źle
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
    private Boolean sortDate = false;
    private Boolean sortFollowers = true;
    private Boolean sortAlphabet = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        layout = (CoordinatorLayout) findViewById(R.id.user_layout);
        preloader = (CrystalPreloader) findViewById(R.id.user_preloader);

        listView = (ListView) findViewById(R.id.user_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                User selected = userAdapter.getItem(position);
                showOtherUserInfoDialog(selected);
            }
        });

        Intent intent = getIntent();
        String categoryForeignId = intent.getStringExtra(INTENT_CATEGORY_FOREIGN_ID);
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
            handleConnectionError(getString(R.string.error_users_load));
            Utils.logError(clazz, "Empty 'categoryId' and 'subcategoryId'");
        } else {
            new ProcessUsers().execute();
        }
    }

    @Override
    protected void onDestroy() {
        if (Utils.isNotEmpty(webSocket)) {
            webSocket.close();
        }
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
    // TODO: maks szerokość nazwy żeby zmieściły się ikonki
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isApiWorking) {
            return true;
        }

        CharSequence title = item.getTitle();
        int length = title.length();

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
                // submenu only
                break;
            case R.id.menu_sort_joined_date:
                if (sortDate) {
                    item.setTitle("Od najstarszego");
                } else {
                    item.setTitle("Od najmłodszego");
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
            case R.id.menu_info:
                return super.onOptionsItemSelected(item);
            case R.id.menu_login:
                return super.onOptionsItemSelected(item);
            default:
                break;
        }
        return true;
    }

    private void initWebSocket() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View chatView = inflater.inflate(R.layout.activity_user_chat, null);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.user_fab);
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
                fab.setBackgroundTintList(ContextCompat.getColorStateList(clazz, R.color.colorAccent));
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
                    webSocket.sendMessage(chatInput.getText().toString());
                    chatInput.setText(null);
                }
            }
        });

        final ChatAdapter chatAdapter = new ChatAdapter(clazz, new ArrayList<WebSocketMessage>());
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

        webSocket = WebSocketClientSide.createWebSocketClientSide(clazz, layout, chatAdapter, chatView, chatDialog);
    }

    private void showChatDialog() {
        chatDialog.show();
    }

    private void showOtherUserInfoDialog(final User user) {
        MaterialDialog.Builder infoDialogBuilder = new MaterialDialog.Builder(clazz)
                .title(user.getUsername())
                .content(user.getInfoContent(clazz))
                .positiveText(R.string.back);

        if (Utils.isEmpty(user.getProfilePicUrl())) {
            infoDialogBuilder.iconRes(R.drawable.placeholder_profile_pic_72);
        } else {
            ImageView profilePic = new ImageView(clazz);
            UrlImageViewHelper.setUrlDrawable(profilePic, user.getProfilePicUrl(), R.drawable.placeholder_profile_pic_72);
            infoDialogBuilder.icon(profilePic.getDrawable());
        }

        final MaterialDialog infoDialog = infoDialogBuilder.build();
        infoDialog.getIconView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent nextIntent = Utils.getInstagramIntent(user.getUsername());

                if (Utils.isIntentAvailable(clazz, nextIntent)) {
                    Utils.logDebug(clazz, "Instagram intent is available");
                    clazz.startActivity(nextIntent);
                } else {
                    Utils.logDebug(clazz, "Instagram intent is NOT available");
                    clazz.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL + user.getUsername())));
                }

                infoDialog.hide();
            }
        });
        infoDialog.getTitleView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent nextIntent = Utils.getInstagramIntent(user.getUsername());

                if (Utils.isIntentAvailable(clazz, nextIntent)) {
                    Utils.logDebug(clazz, "Instagram intent is available");
                    clazz.startActivity(nextIntent);
                } else {
                    Utils.logDebug(clazz, "Instagram intent is NOT available");
                    clazz.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL + user.getUsername())));
                }

                infoDialog.hide();
            }
        });

        infoDialog.show();
    }

    private void handleConnectionError(String message) {
        isApiWorking = true;
        Utils.showConnectionError(layout, message);
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
            } catch (JSONException e) {
                handleConnectionError(getString(R.string.error_users_load));
                Utils.logError(clazz, "JSONException: " + e.getMessage());
            } catch (IOException e) {
                handleConnectionError(getString(R.string.error_users_load));
                Utils.logError(clazz, "IOException: " + e.getMessage());
            } catch (ParseException e) {
                handleConnectionError(getString(R.string.error_users_load));
                Utils.logError(clazz, "ParseException: " + e.getMessage());
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

            initWebSocket();
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

                Session.getInstance().getUser().calculateJoinDate(parentForeignId);
            } catch (JSONException e) {
                handleConnectionError(getString(R.string.error_user_subcategory_add));
                Utils.logError(clazz, "JSONException: " + e.getMessage());
            } catch (IOException e) {
                handleConnectionError(getString(R.string.error_user_subcategory_add));
                Utils.logError(clazz, "IOException: " + e.getMessage());
            } catch (ParseException e) {
                handleConnectionError(getString(R.string.error_user_subcategory_add));
                Utils.logError(clazz, "ParseException: " + e.getMessage());
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
                        getString(R.string.user_subcategory_add_success_short)
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
                handleConnectionError(getString(R.string.error_user_subcategory_remove));
                Utils.logError(clazz, "JSONException: " + e.getMessage());
            } catch (IOException e) {
                handleConnectionError(getString(R.string.error_user_subcategory_remove));
                Utils.logError(clazz, "IOException: " + e.getMessage());
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
                Utils.showConnectionError(layout, getString(R.string.error_user_subcategory_remove));
            }
        }
    }

}
