package abc.flaq.apps.instastudycategories.pojo;

import android.support.annotation.NonNull;

public class Info extends EveObject implements Comparable<Info> {

    private String type;
    private String title;
    private String message;
    private String language;

    public Info() {
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
        return (info.getCreated().compareTo(this.getCreated()));
    }

}
