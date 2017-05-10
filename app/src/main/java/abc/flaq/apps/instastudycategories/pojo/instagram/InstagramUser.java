package abc.flaq.apps.instastudycategories.pojo.instagram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import abc.flaq.apps.instastudycategories.helper.Utils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstagramUser {

    private InstagramUserData data;
    private InstagramMeta meta;

    public InstagramUserData getData() {
        return data;
    }
    public void setData(InstagramUserData data) {
        this.data = data;
    }

    public InstagramMeta getMeta() {
        return meta;
    }
    public void setMeta(InstagramMeta meta) {
        this.meta = meta;
    }

    @Override
    public String toString() {
        String string = "InstagramUser[";
        string += "data: " + (Utils.isEmpty(data) ? null : data.toString()) + ", ";
        string += "meta: " + (Utils.isEmpty(meta) ? null : meta.toString());
        string += "]";
        return string;
    }

    static public class InstagramUserData extends InstagramOAuthUser {
        private InstagramCounts counts;

        public InstagramCounts getCounts() {
            return counts;
        }
        public void setCounts(InstagramCounts counts) {
            this.counts = counts;
        }

        @Override
        public String toString() {
            String string = "InstagramUserData[";
            string += "counts: " + (Utils.isEmpty(counts) ? null : counts.toString()) + ", ";
            string += "id: " + getId() + ", ";
            string += "username: " + getUsername() + ", ";
            string += "fullname: " + getFullname() + ", ";
            string += "profilePicUrl: " + getProfilePicUrl() + ", ";
            string += "bio: " + getBio() + ", ";
            string += "website: " + getWebsite() + ", ";
            string += "]";
            return string;
        }
    }

}
