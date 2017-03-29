package abc.flaq.apps.instastudycategories;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Response extends EveObject {

    private enum STATUS_CODE { OK, ERR };

    @JsonProperty("_status")
    private String status;
    @JsonProperty("_error")
    private String error;
    @JsonProperty("__issues")
    private String issues;

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }

    public String getIssues() {
        return issues;
    }
    public void setIssues(String issues) {
        this.issues = issues;
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
        string += "status=" + status;
        string += "error=" + error;
        string += "issues=" + issues;
        string += "]";
        return string;
    }

}
