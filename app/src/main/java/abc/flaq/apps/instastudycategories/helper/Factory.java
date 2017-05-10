package abc.flaq.apps.instastudycategories.helper;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import abc.flaq.apps.instastudycategories.api.Api;
import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;
import abc.flaq.apps.instastudycategories.pojo.User;
import abc.flaq.apps.instastudycategories.pojo.instagram.InstagramUser;

import static abc.flaq.apps.instastudycategories.helper.Constants.API_ALL_CATEGORY_NAME;

public class Factory {

    public static User userFromInstagramUser(InstagramUser.InstagramUserData instagramUser) throws IOException, JSONException {
        User user = new User();
        user.setForeignId(Utils.doForeignId(instagramUser.getId()));    // Update with _id after creating
        user.setInstagramId(instagramUser.getId());
        user.setFullname(instagramUser.getFullname());
        user.setUsername(instagramUser.getUsername());
        user.setBio(instagramUser.getBio());
        user.setProfilePicUrl(instagramUser.getProfilePicUrl());
        if (Utils.isEmpty(instagramUser.getCounts())) {
            user.setFollowers(0);
            user.setMedia(0);
        } else {
            user.setFollowers(instagramUser.getCounts().getFollowers());
            user.setMedia(instagramUser.getCounts().getMedia());
        }
        // Adding user to "all" category by default
        ArrayList<String> categories = new ArrayList<>();
        Category categoryAll = Api.getCategoryByName(API_ALL_CATEGORY_NAME);
        if (Utils.isNotEmpty(categoryAll)) {
            categories.add(categoryAll.getForeignId());
        }
        user.setCategories(categories);
        user.setCategoriesSize(categories.size());
        user.setSubcategories(new ArrayList<String>());
        user.setSubcategoriesSize(0);
        user.setActive(Boolean.TRUE);
        return user;
    }

    public static Category categoryFromName(String categoryName) throws IOException, JSONException {
        Category category = new Category();
        category.setForeignId(Utils.doForeignId(categoryName + new Date().getTime()));    // Update with _id after creating
        category.setName(categoryName);
        category.setUsersSize(0);
        category.setSubcategoriesSize(0);
        category.setHashtags(new ArrayList<String>());
        category.setAsSubcategory(Boolean.FALSE);
        category.setImageUrl("");
        category.setActive(Boolean.FALSE);
        return category;
    }

    public static Subcategory subcategoryFromName(String subcategoryName, String parentCategoryForeignId) throws IOException, JSONException {
        Subcategory subcategory = new Subcategory();
        subcategory.setForeignId(Utils.doForeignId(subcategoryName + new Date().getTime()));    // Update with _id after creating
        subcategory.setName(subcategoryName);
        subcategory.setUsersSize(0);
        List<String> categories = new ArrayList<>();
        categories.add(parentCategoryForeignId);
        subcategory.setCategories(categories);
        subcategory.setCategoriesSize(1);
        subcategory.setHashtags(new ArrayList<String>());
        subcategory.setImageUrl("");
        subcategory.setActive(Boolean.FALSE);
        return subcategory;
    }

}