package abc.flaq.apps.instastudycategories.pojo;

import android.graphics.drawable.Drawable;

import java.util.Date;

public class WebSocketMessage {

    private String message;
    private String name;
    private Date date;
    private Drawable profilePic;

    public WebSocketMessage() {
    }
    public WebSocketMessage(String message, String name, Date date) {
        this.message = message;
        this.name = name;
        this.date = date;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }

    public Drawable getProfilePic() {
        return profilePic;
    }
    public void setProfilePic(Drawable profilePic) {
        this.profilePic = profilePic;
    }

    @Override
    public String toString() {
        String string = "WebSocketMessage[";
        string += "message: " + message + ", ";
        string += "name: " + name + ", ";
        string += "date: " + date;
        string += "]";
        return string;
    }

}
