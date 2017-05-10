package abc.flaq.apps.instastudycategories.general;

import android.content.SharedPreferences;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import abc.flaq.apps.instastudycategories.helper.Utils;
import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;
import abc.flaq.apps.instastudycategories.pojo.User;

public class Session {

    private static Session instance;
    private SharedPreferences settings;
    private User user;
    private ImageView userProfilePic;
    private ArrayList<Category> categories;
    private Map<String, List<Subcategory>> subcategoriesMap = new HashMap<>();
    private Boolean isCategoryChanged = false;
    private Boolean isSubcategoryChanged = false;

    private Session() {
    }

    public static Session getInstance() {
        if (Utils.isEmpty(instance)) {
            instance = new Session();
        }
        return instance;
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

    public ArrayList<Category> getCategories() {
        return categories;
    }
    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }

    public List<Subcategory> getSubcategories(String categoryForeignId) {
        return subcategoriesMap.get(categoryForeignId);
    }
    public void setSubcategories(String categoryForeignId, List<Subcategory> subcategories) {
        this.subcategoriesMap.put(categoryForeignId, subcategories);
    }

    public Boolean isCategoryChanged() {
        return isCategoryChanged;
    }
    public void setCategoryChanged(Boolean categoryChanged) {
        isCategoryChanged = categoryChanged;
    }

    public Boolean isSubcategoryChanged() {
        return isSubcategoryChanged;
    }
    public void setSubcategoryChanged(Boolean subcategoryChanged) {
        isSubcategoryChanged = subcategoryChanged;
    }

}
