package abc.flaq.apps.instastudycategories.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstagramUser {

    private String id = "";
    private String username = "";
    @JsonProperty("full_name")
    private String fullname = "";
    @JsonProperty("profile_picture")
    private String profilePicUrl = "";
    private String bio = "";
    private String website = "";

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }
    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getBio() {
        return bio;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getWebsite() {
        return website;
    }
    public void setWebsite(String website) {
        this.website = website;
    }

    @Override
    public String toString() {
        String string = "InstagramUser[";
        string += "id: " + id + ", ";
        string += "username: " + username + ", ";
        string += "fullname: " + fullname + ", ";
        string += "profilePicUrl: " + profilePicUrl + ", ";
        string += "bio: " + bio + ", ";
        string += "website: " + website;
        string += "]";
        return string;
    }

}
