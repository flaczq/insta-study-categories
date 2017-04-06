package abc.flaq.apps.instastudycategories.pojo.instagram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstagramCounts {

    private Integer media;
    private Integer follows;
    @JsonProperty("followed_by")
    private Integer followers;

    public Integer getMedia() {
        return media;
    }
    public void setMedia(Integer media) {
        this.media = media;
    }

    public Integer getFollows() {
        return follows;
    }
    public void setFollows(Integer follows) {
        this.follows = follows;
    }

    public Integer getFollowers() {
        return followers;
    }
    public void setFollowers(Integer followers) {
        this.followers = followers;
    }

    @Override
    public String toString() {
        String string = "InstagramCounts[";
        string += "media: " + media + ", ";
        string += "follows: " + follows + ", ";
        string += "followers: " + followers;
        string += "]";
        return string;
    }

}
