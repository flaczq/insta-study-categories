package abc.flaq.apps.instastudycategories.pojo;

import android.support.annotation.NonNull;

import java.util.ArrayList;

import abc.flaq.apps.instastudycategories.pojo.instagram.InstagramUser;
import abc.flaq.apps.instastudycategories.utils.Utils;

public class User extends EveObject implements Comparable<User> {

    private String instagramId = "";
    private String fullname = "";
    private String username = "";
    private String bio = "";
    private String profilePicUrl = "";
    private Integer followers;
    private Integer media;
    private ArrayList<String> categories;
    private Integer categoriesSize;
    private ArrayList<String> subcategories;
    private Integer subcategoriesSize;
    private Boolean active;

    public String getInstagramId() {
        return instagramId;
    }
    public void setInstagramId(String instagramId) {
        this.instagramId = instagramId;
    }

    public String getFullname() {
        return fullname;
    }
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }
    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public Integer getFollowers() {
        return followers;
    }
    public void setFollowers(Integer followers) {
        this.followers = followers;
    }

    public Integer getMedia() {
        return media;
    }
    public void setMedia(Integer media) {
        this.media = media;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }
    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public Integer getCategoriesSize() {
        return categoriesSize;
    }
    public void setCategoriesSize(Integer categoriesSize) {
        this.categoriesSize = categoriesSize;
    }

    public ArrayList<String> getSubcategories() {
        return subcategories;
    }
    public void setSubcategories(ArrayList<String> subcategories) {
        this.subcategories = subcategories;
    }

    public Integer getSubcategoriesSize() {
        return subcategoriesSize;
    }
    public void setSubcategoriesSize(Integer subcategoriesSize) {
        this.subcategoriesSize = subcategoriesSize;
    }

    public Boolean isActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        String string = "User[";
        string += "instagramId: " + instagramId + ", ";
        string += "fullname: " + fullname + ", ";
        string += "username: " + username + ", ";
        string += "bio: " + bio + ", ";
        string += "profilePicUrl: " + profilePicUrl + ", ";
        string += "followers: " + followers + ", ";
        string += "media: " + media + ", ";
        string += "categories: " + categories + ", ";
        string += "categoriesSize: " + categoriesSize + ", ";
        string += "subcategories: " + subcategories + ", ";
        string += "subcategoriesSize: " + subcategoriesSize + ", ";
        string += "active: " + active;
        string += "]";
        return string;
    }

    @Override
    public int compareTo(@NonNull User user) {
        return (user.getFollowers() - followers);
    }

    public String toPostJson() {
        String json = "{";
        json += "\"foreignId\":\"" + getForeignId() + "\",";
        json += "\"instagramId\":\"" + instagramId + "\",";
        json += "\"fullname\":\"" + fullname + "\",";
        json += "\"username\":\"" + username + "\",";
        json += "\"bio\":\"" + bio + "\",";
        json += "\"profilePicUrl\":\"" + profilePicUrl + "\",";
        json += "\"followers\":" + followers + ",";
        json += "\"media\":" + media + ",";
        json += "\"categories\":" + Utils.listToString(categories) + ",";
        json += "\"categoriesSize\":" + categoriesSize + ",";
        json += "\"subcategories\":" + Utils.listToString(subcategories) + ",";
        json += "\"subcategoriesSize\":" + subcategoriesSize + ",";
        json += "\"active\":" + active;
        json += "}";
        return json;
    }

    public void updateFromInstagramUser(InstagramUser.InstagramUserData instagramUser) {
        instagramId = instagramUser.getId();
        fullname = instagramUser.getFullname();
        username = instagramUser.getUsername();
        bio = instagramUser.getBio();
        profilePicUrl = instagramUser.getProfilePicUrl();
        if (Utils.isEmpty(instagramUser.getCounts())) {
            followers = 0;
            media = 0;
        } else {
            followers = instagramUser.getCounts().getFollowers();
            media = instagramUser.getCounts().getMedia();
        }
        active = Boolean.TRUE;
    }

}
