package abc.flaq.apps.instastudycategories.utils;

import android.content.SharedPreferences;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;
import abc.flaq.apps.instastudycategories.pojo.User;

public class Session {

    private static Session instance;
    private SharedPreferences settings;
    private User user;
    private ImageView userProfilePic;
    private List<Category> categories;
    private Map<String, List<Subcategory>> subcategoriesMap = new HashMap<>();

    public static Session getInstance() {
        if (Utils.isEmpty(instance)) {
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

    public ImageView getUserProfilePic() {
        return userProfilePic;
    }
    public void setUserProfilePic(ImageView userProfilePic) {
        this.userProfilePic = userProfilePic;
    }

    public List<Category> getCategories() {
        return categories;
    }
    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Subcategory> getSubcategories(String id) {
        return subcategoriesMap.get(id);
    }
    public void setSubcategories(String id, List<Subcategory> subcategories) {
        this.subcategoriesMap.put(id, subcategories);
    }

}
