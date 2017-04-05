package abc.flaq.apps.instastudycategories.pojo.instagram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import abc.flaq.apps.instastudycategories.utils.GeneralUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstagramAccessToken extends InstagramResponse {

    @JsonProperty("access_token")
    private String accessToken;
    private InstagramUser user;

    public String getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public InstagramUser getUser() {
        return user;
    }
    public void setUser(InstagramUser user) {
        this.user = user;
    }

    @Override
    public String toString() {
        String string = "InstagramAccessToken[";
        string += "accessToken: " + accessToken + ", ";
        string += "user: " + (GeneralUtils.isEmpty(user) ? null : user.toString());
        string += "]";
        return string;
    }

}
