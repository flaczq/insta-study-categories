package abc.flaq.apps.instastudycategories.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

import abc.flaq.apps.instastudycategories.utils.Utils;

public class Response extends EveObject {

    private enum STATUS_CODE { OK, ERR }

    @JsonProperty("_status")
    private String status;
    @JsonProperty("_error")
    private Map<String, String> error;
    @JsonProperty("_issues")
    private Map<String, String> issues;

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, String> getError() {
        return error;
    }
    public void setError(Map<String, String> error) {
        this.error = error;
    }

    public Map<String, String> getIssues() {
        return issues;
    }
    public void setIssues(Map<String, String> issues) {
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
        string += "status: " + status + ", ";
        string += "error: " + (Utils.isEmpty(error) ? null : error.toString()) + ", ";
        string += "issues: " + (Utils.isEmpty(issues) ? null : issues.toString());
        string += "]";
        return string;
    }

}
