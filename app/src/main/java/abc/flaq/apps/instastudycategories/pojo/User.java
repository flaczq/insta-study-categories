package abc.flaq.apps.instastudycategories.pojo;

import android.content.Context;
import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.helper.Utils;
import abc.flaq.apps.instastudycategories.pojo.instagram.InstagramUser;

import static abc.flaq.apps.instastudycategories.helper.Constants.DATE_FORMAT;
import static abc.flaq.apps.instastudycategories.helper.Constants.FULL_DATE_FORMAT;
import static abc.flaq.apps.instastudycategories.helper.Constants.HOUR_FORMAT;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FULL_DATE_FORMAT, timezone = "Poland")
    private ArrayList<String> subcategoriesDates = new ArrayList<>();
    private Date joinDate;
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

    public ArrayList<String> getSubcategoriesDates() {
        return subcategoriesDates;
    }
    public void setSubcategoriesDates(ArrayList<String> subcategoriesDates) {
        this.subcategoriesDates = subcategoriesDates;
    }

    public Date getJoinDate() {
        return joinDate;
    }
    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
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
        string += "subcategoriesDates: " + subcategoriesDates + ", ";
        string += "active: " + active;
        string += "]";
        return string;
    }

    @Override
    public int compareTo(@NonNull User user) {
        if (Utils.isNotEmpty(getJoinDate()) && Utils.isNotEmpty(user.getJoinDate())) {
            return (user.getJoinDate().compareTo(getJoinDate()));
        }
        return (user.getCreated().compareTo(getCreated()));
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
        json += "\"subcategoriesDates\":" + Utils.listToString(subcategoriesDates) + ",";
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

    public void calculateJoinDate(String subcategoryForeignId) throws ParseException {
        int dateIndex = subcategories.indexOf(subcategoryForeignId);
        if (dateIndex == -1) {
            joinDate = getCreated();
        } else {
            joinDate = Utils.parseStringDate(subcategoriesDates.get(dateIndex), FULL_DATE_FORMAT);
        }
    }

    public String getInfoContent(Context context) {
        String infoContent = "";
        if (Utils.isNotEmpty(fullname)) {
            infoContent += context.getString(R.string.user_info_1) + fullname + ". ";
        }
        if (media > 0 && media < 5) {
            infoContent += context.getString(R.string.user_info_2) + media + context.getString(R.string.user_info_3);
        } else {
            infoContent += context.getString(R.string.user_info_2) + media + context.getString(R.string.user_info_4);
        }
        if (followers == 1) {
            infoContent += context.getString(R.string.user_info_5) + followers + context.getString(R.string.user_info_6);
        } else if (followers > 0 && followers < 5) {
            infoContent += context.getString(R.string.user_info_5) + followers + context.getString(R.string.user_info_7);
        } else {
            infoContent += context.getString(R.string.user_info_5) + followers + context.getString(R.string.user_info_8);
        }
        String date = Utils.formatDate(getCreated(), DATE_FORMAT);
        String hour = Utils.formatDate(getCreated(), HOUR_FORMAT);
        if (subcategoriesSize == 0) {
            infoContent += context.getString(R.string.user_info_9) + date + context.getString(R.string.user_info_10) + hour + ". ";
        } else if (subcategoriesSize == 1) {
            infoContent += context.getString(R.string.user_info_11) + date + context.getString(R.string.user_info_10) + hour + ". ";
        } else {
            infoContent += context.getString(R.string.user_info_12) + subcategoriesSize + context.getString(R.string.user_info_13) + date + context.getString(R.string.user_info_10) + hour + ". ";
        }
        if (Utils.isNotEmpty(bio)) {
            infoContent += context.getString(R.string.user_info_14) + bio;
        }
        return infoContent;
    }

}
