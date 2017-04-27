package abc.flaq.apps.instastudycategories.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Response extends EveObject {

    private enum STATUS_CODE { OK, ERR }

    @JsonProperty("_status")
    private String status;

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean isOk() {
        return STATUS_CODE.OK.name().equals(status);
    }
    public Boolean isError() {
        return STATUS_CODE.ERR.name().equals(status);
    }

    @Override
    public String toString() {
        String string = "Response[";
        string += "status: " + status;
        string += "]";
        return string;
    }

}
