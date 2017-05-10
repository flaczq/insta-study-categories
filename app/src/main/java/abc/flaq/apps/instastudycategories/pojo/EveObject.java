package abc.flaq.apps.instastudycategories.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import abc.flaq.apps.instastudycategories.helper.Utils;

import static abc.flaq.apps.instastudycategories.helper.Constants.FULL_DATE_FORMAT;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EveObject {

    private String foreignId;
    @JsonProperty("_id")
    private String id;
    @JsonProperty("_etag")
    private String etag;
    @JsonProperty("_created")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FULL_DATE_FORMAT, timezone = "Poland")
    private Date created;
    @JsonProperty("_updated")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FULL_DATE_FORMAT, timezone = "Poland")
    private Date updated;

    public String getForeignId() {
        return foreignId;
    }
    public void setForeignId(String foreignId) {
        this.foreignId = foreignId;
    }

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

    public void updateFromResponse(EveObject object) {
        id = object.getId();
        foreignId = Utils.doForeignId(object.getId());
        etag = object.getEtag();
        created = object.getCreated();
        updated = object.getUpdated();
    }

    public void removeFromList(List list) {
        Iterator<EveObject> it = list.listIterator();
        while (it.hasNext()) {
            EveObject eveObject = it.next();
            if (id.equals(eveObject.getId())) {
                it.remove();
                break;
            }
        }
    }

}
