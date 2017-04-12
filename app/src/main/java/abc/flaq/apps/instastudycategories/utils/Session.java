package abc.flaq.apps.instastudycategories.utils;

import android.content.SharedPreferences;

import abc.flaq.apps.instastudycategories.pojo.User;

public class Session {

    private static Session instance;
    private SharedPreferences settings;
    private User user;

    public static Session getInstance() {
        if (GeneralUtils.isEmpty(instance)) {
            instance = new Session();
        }
        return instance;
    }

    private Session() {
    }

    public SharedPreferences getSettings() {
        return settings;
    }
    public void setSettings(SharedPreferences settings) {
        this.settings = settings;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

}
