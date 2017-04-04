package abc.flaq.apps.instastudycategories.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

import static abc.flaq.apps.instastudycategories.utils.Constants.DATE_TIMEZONE;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EveObject {

    @JsonProperty("_id")
    private String id;
    @JsonProperty("_etag")
    private String etag;
    @JsonProperty("_created")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIMEZONE, timezone = "Poland")
    private Date created;
    @JsonProperty("_updated")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIMEZONE, timezone = "Poland")
    private Date updated;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getEtag() {
        return etag;
    }
    public void setEtag(String etag) {
        this.etag = etag;
    }

    public Date getCreated() {
        return created;
    }
    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }
    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public void update(EveObject object) {
        etag = object.getEtag();
        updated = object.getUpdated();
    }

}
