package abc.flaq.apps.instastudycategories;

public class User extends EveObject {

    private String username;
    private String fullname;
    private String instagramId;
    private Integer categoriesSize;
    private Integer followers;
    private Integer media;
    private String bio;
    private String profilePicUrl;
    private Boolean active;

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

    public String getInstagramId() {
        return instagramId;
    }
    public void setInstagramId(String instagramId) {
        this.instagramId = instagramId;
    }

    public Integer getCategoriesSize() {
        return categoriesSize;
    }
    public void setCategoriesSize(Integer categoriesSize) {
        this.categoriesSize = categoriesSize;
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

    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        String string = "User[";
        string += "username=" + username + ", ";
        string += "fullname=" + fullname + ", ";
        string += "instagramId=" + instagramId + ", ";
        string += "categoriesSize=" + categoriesSize + ", ";
        string += "followers=" + followers + ", ";
        string += "media=" + media + ", ";
        string += "bio=" + bio + ", ";
        string += "profilePicUrl=" + profilePicUrl + ", ";
        string += "active=" + active;
        string += "]";
        return string;
    }

}
