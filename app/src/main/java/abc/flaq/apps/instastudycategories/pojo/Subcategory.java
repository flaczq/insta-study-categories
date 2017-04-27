package abc.flaq.apps.instastudycategories.pojo;

import android.support.annotation.NonNull;

import java.util.List;

import abc.flaq.apps.instastudycategories.utils.Utils;

public class Subcategory extends EveObject implements Comparable<Subcategory> {

    private String name = "";
    private Integer usersSize;
    private List<String> categories;
    private Integer categoriesSize;
    private List<String> hashtags;
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

    public List<String> getCategories() {
        return categories;
    }
    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public Integer getCategoriesSize() {
        return categoriesSize;
    }
    public void setCategoriesSize(Integer categoriesSize) {
        this.categoriesSize = categoriesSize;
    }

    public List<String> getHashtags() {
        return hashtags;
    }
    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
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
        String string = "Subcategory[";
        string += "name=" + name + ", ";
        string += "usersSize=" + usersSize + ", ";
        string += "categories=" + categories + ", ";
        string += "categoriesSize=" + categoriesSize + ", ";
        string += "hashtags=" + hashtags + ", ";
        string += "imageUrl: " + imageUrl + ", ";
        string += "active=" + active;
        string += "]";
        return string;
    }

    @Override
    public int compareTo(@NonNull Subcategory subcategory) {
        return (subcategory.getUsersSize() - this.usersSize);
    }

    public String toPostJson() {
        String json = "{";
        json += "\"foreignId\":\"" + getForeignId() + "\",";
        json += "\"name\":\"" + name + "\",";
        json += "\"usersSize\":" + usersSize + ",";
        json += "\"categories\":" + Utils.listToString(categories) + ",";
        json += "\"categoriesSize\":" + categoriesSize + ",";
        json += "\"hashtags\":" + Utils.listToString(hashtags) + ",";
        json += "\"imageUrl\":\"" + imageUrl + "\",";
        json += "\"active\":" + active;
        json += "}";
        return json;
    }

}
