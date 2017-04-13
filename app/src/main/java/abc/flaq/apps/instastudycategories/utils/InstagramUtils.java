package abc.flaq.apps.instastudycategories.utils;

import java.util.ArrayList;

import abc.flaq.apps.instastudycategories.pojo.User;
import abc.flaq.apps.instastudycategories.pojo.instagram.InstagramUser;

public class InstagramUtils {

    public static User instagramUserToUser(InstagramUser.InstagramUserData instagramUser) {
        User user = new User();
        user.setInstagramId(instagramUser.getId());
        user.setFullname(instagramUser.getFullname());
        user.setUsername(instagramUser.getUsername());
        user.setBio(instagramUser.getBio());
        user.setProfilePicUrl(instagramUser.getProfilePicUrl());
        user.setFollowers((Utils.isEmpty(instagramUser.getCounts()) ? 0 : instagramUser.getCounts().getFollowers()));
        user.setMedia((Utils.isEmpty(instagramUser.getCounts()) ? 0 : instagramUser.getCounts().getMedia()));
        user.setCategories(new ArrayList<String>());
        user.setCategoriesSize(0);
        user.setSubcategories(new ArrayList<String>());
        user.setSubcategoriesSize(0);
        user.setActive(Boolean.TRUE);
        return user;
    }

}