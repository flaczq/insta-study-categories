package abc.flaq.apps.instastudycategories.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import abc.flaq.apps.instastudycategories.utils.GeneralUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstagramOAuthResponse {

    @JsonProperty("access_token")
    private String accessToken;
    private InstagramUser user;
    private Integer code;
    @JsonProperty("error_type")
    private String type;
    @JsonProperty("error_message")
    private String message;

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

    public Integer getCode() {
        return code;
    }
    public void setCode(Integer code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean isOk() {
        if (GeneralUtils.isEmpty(code)) {
            return true;
        }
        return (code == 200);
    }
    public Boolean isError() {
        if (GeneralUtils.isEmpty(code)) {
            return false;
        }
        return (code != 200);
    }

    @Override
    public String toString() {
        String string = "InstagramOAuthResponse[";
        string += "accessToken: " + accessToken + ", ";
        string += "user: " + (GeneralUtils.isEmpty(user) ? null : user.toString()) + ", ";
        string += "code: " + code + ", ";
        string += "type: " + type + ", ";
        string += "message: " + message;
        string += "]";
        return string;
    }

}
