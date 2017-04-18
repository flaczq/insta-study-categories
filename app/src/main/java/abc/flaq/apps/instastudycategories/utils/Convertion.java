package abc.flaq.apps.instastudycategories.utils;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;
import abc.flaq.apps.instastudycategories.pojo.User;
import abc.flaq.apps.instastudycategories.pojo.instagram.InstagramUser;

import static abc.flaq.apps.instastudycategories.utils.Constants.API_ALL_CATEGORY_NAME;

public class Convertion {

    public static User instagramUserToUser(InstagramUser.InstagramUserData instagramUser) throws IOException, JSONException {
        User user = new User();
        user.setForeignId(Utils.doForeignId(instagramUser.getId()));    // Update with _id after creating
        user.setInstagramId(instagramUser.getId());
        user.setFullname(instagramUser.getFullname());
        user.setUsername(instagramUser.getUsername());
        user.setBio(instagramUser.getBio());
        user.setProfilePicUrl(instagramUser.getProfilePicUrl());
        user.setFollowers((Utils.isEmpty(instagramUser.getCounts()) ? 0 : instagramUser.getCounts().getFollowers()));
        user.setMedia((Utils.isEmpty(instagramUser.getCounts()) ? 0 : instagramUser.getCounts().getMedia()));
        // Adding user to "all" category by default
        List<String> categories = new ArrayList<>();
        Category allCategory = Api.getCategoryByName(API_ALL_CATEGORY_NAME);
        if (Utils.isNotEmpty(allCategory)) {
            categories.add(allCategory.getForeignId());
        }
        user.setCategories(categories);
        user.setCategoriesSize(categories.size());
        user.setSubcategories(new ArrayList<String>());
        user.setSubcategoriesSize(0);
        user.setActive(Boolean.TRUE);
        return user;
    }

    public static Subcategory subcategoryFromName(String name) {
        Subcategory subcategory = new Subcategory();
        subcategory.setForeignId(Utils.doForeignId(name + new Date()));    // Update with _id after creating
        subcategory.setCategoriesSize(1);
        subcategory.setActive(Boolean.FALSE);
        // TODO: ...
        return subcategory;
    }

}