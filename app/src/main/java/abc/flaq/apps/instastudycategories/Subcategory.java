package abc.flaq.apps.instastudycategories;

import java.util.List;

public class Subcategory extends EveObject implements Comparable<Subcategory> {

    private String name;
    private List<String> users;
    private Integer usersSize;
    private List<String> hashtags;
    private Boolean active;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<String> getUsers() {
        return users;
    }
    public void setUsers(List<String> users) {
        this.users = users;
    }

    public Integer getUsersSize() {
        return usersSize;
    }
    public void setUsersSize(Integer usersSize) {
        this.usersSize = usersSize;
    }

    public List<String> getHashtags() {
        return hashtags;
    }
    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        String string = "Subcategory[";
        string += "name=" + name + ", ";
        string += "users=" + users + ", ";
        string += "usersSize=" + usersSize + ", ";
        string += "hashtags=" + hashtags + ", ";
        string += "active=" + active;
        string += "]";
        return string;
    }

    @Override
    public int compareTo(Subcategory subcategory) {
        return (subcategory.getUsersSize() - this.usersSize);
    }

}
