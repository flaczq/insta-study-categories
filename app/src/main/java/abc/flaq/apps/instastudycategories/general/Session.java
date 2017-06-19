package abc.flaq.apps.instastudycategories.general;

import android.content.SharedPreferences;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import abc.flaq.apps.instastudycategories.helper.Utils;
import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.pojo.Info;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;
import abc.flaq.apps.instastudycategories.pojo.User;

public class Session {

    private static Session instance;
    private SharedPreferences settings;
    private User user;
    private ImageView userProfilePic;
    private Info newestInfo;
    private ArrayList<Category> categories;
    private Map<String, List<Subcategory>> subcategoriesMap = new HashMap<>();
    // For refreshing grids when something's changed
    private Boolean isCategoryChanged = false;
    private Boolean isSubcategoryChanged = false;
    // --- HELPING
    // For displaying subtitle in action bar on users list
    private String categoryName;
    // For calculating grid height
    private int maxGridSize;

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

    public Info getNewestInfo() {
        return newestInfo;
    }
    public void setNewestInfo(Info newestInfo) {
        this.newestInfo = newestInfo;
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

    public String getCategoryName() {
        return categoryName;
    }
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getMaxGridSize() {
        return maxGridSize;
    }
    public void setMaxGridSize(int maxGridSize) {
        this.maxGridSize = maxGridSize;
    }

}
