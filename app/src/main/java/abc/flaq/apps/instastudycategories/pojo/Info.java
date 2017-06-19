package abc.flaq.apps.instastudycategories.pojo;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import static abc.flaq.apps.instastudycategories.helper.Constants.DB_INFO_TYPE_NEWS;

public class Info extends EveObject implements Comparable<Info>, Parcelable {

    private String type;
    private String title;
    private String message;
    private String language;

    public Info() {
        type = DB_INFO_TYPE_NEWS;
        title = "";
        message = "";
        language = "pl";
    }
    private Info(Parcel parcel) {
        type = parcel.readString();
        title = parcel.readString();
        message = parcel.readString();
        language = parcel.readString();
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        String string = "Info[";
        string += "type: " + type + ", ";
        string += "title: " + title + ", ";
        string += "message: " + message + ", ";
        string += "language: " + language;
        string += "]";
        return string;
    }

    @Override
    public int compareTo(@NonNull Info info) {
        return (info.getCreated().compareTo(getCreated()));
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(type);
        parcel.writeString(title);
        parcel.writeString(message);
        parcel.writeString(language);
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Info createFromParcel(Parcel parcel) {
            return new Info(parcel);
        }
        public Info[] newArray(int size) {
            return new Info[size];
        }
    };

}
