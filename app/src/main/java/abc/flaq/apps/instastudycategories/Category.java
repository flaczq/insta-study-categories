package abc.flaq.apps.instastudycategories;

import java.util.List;

public class Category extends EveObject implements Comparable<Category> {

    private String name;
    private Integer usersSize;
    private Integer subcategoriesSize;
    private List<String> hashtags;
    private Boolean asSubcategory;
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

    public Boolean isActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        String string = "Category[";
        string += "name=" + name + ", ";
        string += "usersSize=" + usersSize + ", ";
        string += "subcategoriesSize=" + subcategoriesSize + ", ";
        string += "hashtags=" + hashtags + ", ";
        string += "asSubcategory=" + asSubcategory + ", ";
        string += "active=" + active;
        string += "]";
        return string;
    }

    @Override
    public int compareTo(Category category) {
        return (category.getUsersSize() - this.usersSize);
    }

}
