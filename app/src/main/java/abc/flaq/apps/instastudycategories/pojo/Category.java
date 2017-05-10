package abc.flaq.apps.instastudycategories.pojo;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import abc.flaq.apps.instastudycategories.helper.Utils;

public class Category extends EveObject implements Comparable<Category>, Parcelable {

    private String name = "";
    private Integer usersSize;
    private Integer subcategoriesSize;
    private ArrayList<String> hashtags;
    private Boolean asSubcategory;
    private String imageUrl = "";
    private Boolean active;

    public Category() {
    }
    private Category(Parcel parcel) {
        name = parcel.readString();
        usersSize = parcel.readInt();
        subcategoriesSize = parcel.readInt();
        parcel.readStringList(hashtags);
        asSubcategory = (parcel.readInt() == 1);
        imageUrl = parcel.readString();
        active = (parcel.readInt() == 1);
    }

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

    public ArrayList<String> getHashtags() {
        return hashtags;
    }
    public void setHashtags(ArrayList<String> hashtags) {
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

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getForeignId());
        parcel.writeString(name);
        parcel.writeInt(usersSize);
        parcel.writeInt(subcategoriesSize);
        parcel.writeStringList(hashtags);
        parcel.writeInt(asSubcategory ? 1 : 0);
        parcel.writeString(imageUrl);
        parcel.writeInt(active ? 1 : 0);
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Category createFromParcel(Parcel parcel) {
            return new Category(parcel);
        }
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

}
