package abc.flaq.apps.instastudycategories.pojo;

import android.support.annotation.NonNull;

import java.util.List;

import abc.flaq.apps.instastudycategories.utils.Utils;

public class Category extends EveObject implements Comparable<Category> {

    private String name = "";
    private Integer usersSize;
    private Integer subcategoriesSize;
    private List<String> hashtags;
    private Boolean asSubcategory;
    private String imageUrl = "";
    private Boolean active;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Integer getUsersSize() {
        return usersSize;
    }
    public void setUsersSize(Integer usersSize) {
        this.usersSize = usersSize;
    }

    public Integer getSubcategoriesSize() {
        return subcategoriesSize;
    }
    public void setSubcategoriesSize(Integer subcategoriesSize) {
        this.subcategoriesSize = subcategoriesSize;
    }

    public List<String> getHashtags() {
        return hashtags;
    }
    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public Boolean isAsSubcategory() {
        return asSubcategory;
    }
    public void setAsSubcategory(Boolean asSubcategory) {
        this.asSubcategory = asSubcategory;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean isActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        String string = "Category[";
        string += "name: " + name + ", ";
        string += "usersSize: " + usersSize + ", ";
        string += "subcategoriesSize: " + subcategoriesSize + ", ";
        string += "hashtags: " + hashtags + ", ";
        string += "asSubcategory: " + asSubcategory + ", ";
        string += "imageUrl: " + imageUrl + ", ";
        string += "active: " + active;
        string += "]";
        return string;
    }

    @Override
    public int compareTo(@NonNull Category category) {
        return (category.getUsersSize() - this.usersSize);
    }

    public String toPostJson() {
        String json = "{";
        json += "\"foreignId\":\"" + getForeignId() + "\",";
        json += "\"name\":\"" + name + "\",";
        json += "\"usersSize\":" + usersSize + ",";
        json += "\"subcategoriesSize\":" + subcategoriesSize + ",";
        json += "\"hashtags\":" + Utils.listToString(hashtags) + ",";
        json += "\"asSubcategory\":" + asSubcategory + ",";
        json += "\"imageUrl\":\"" + imageUrl + "\",";
        json += "\"active\":" + active;
        json += "}";
        return json;
    }

}
