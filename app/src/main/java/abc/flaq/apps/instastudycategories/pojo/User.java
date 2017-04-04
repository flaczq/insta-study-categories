package abc.flaq.apps.instastudycategories.pojo;

import java.util.List;

public class User extends EveObject implements Comparable<User> {

    private String instagramId = "";
    private String fullname = "";
    private String username = "";
    private String bio = "";
    private String profilePicUrl = "";
    private Integer followers;
    private Integer media;
    private List<String> categories;
    private Integer categoriesSize;
    private List<String> subcategories;
    private Integer subcategoriesSize;
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

    public List<String> getSubcategories() {
        return subcategories;
    }
    public void setSubcategories(List<String> subcategories) {
        this.subcategories = subcategories;
    }

    public Integer getSubcategoriesSize() {
        return subcategoriesSize;
    }
    public void setSubcategoriesSize(Integer subcategoriesSize) {
        this.subcategoriesSize = subcategoriesSize;
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
        string += "active: " + active;
        string += "]";
        return string;
    }

    @Override
    public int compareTo(User user) {
        return (user.getFollowers() - followers);
    }

}
