package abc.flaq.apps.instastudycategories.pojo.instagram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.HttpURLConnection;

import abc.flaq.apps.instastudycategories.utils.GeneralUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstagramResponse {

    private Integer code;
    @JsonProperty("error_type")
    private String type;
    @JsonProperty("error_message")
    private String message;

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
        return (code == HttpURLConnection.HTTP_OK);
    }
    public Boolean isError() {
        if (GeneralUtils.isEmpty(code)) {
            return false;
        }
        return (code != HttpURLConnection.HTTP_OK);
    }

    @Override
    public String toString() {
        String string = "InstagramResponse[";
        string += "code: " + code + ", ";
        string += "type: " + type + ", ";
        string += "message: " + message;
        string += "]";
        return string;
    }

}
