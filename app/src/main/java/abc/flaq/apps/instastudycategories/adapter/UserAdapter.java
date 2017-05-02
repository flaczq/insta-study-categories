package abc.flaq.apps.instastudycategories.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.List;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.pojo.User;
import abc.flaq.apps.instastudycategories.utils.Utils;

public class UserAdapter extends BaseAdapter {

    private final Context context;
    private final List<User> users;

    public UserAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public User getItem(int position) {
        if (position < users.size()) {
            return users.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public String getId(int position) {
        if (position < users.size()) {
            return users.get(position).getId();
        }
        return null;
    }

    public void addItem(User user) {
        users.add(0, user);
    }

    private Integer findItemById(User user) {
        for (int i = 0; i < users.size(); i++) {
            if (user.getId().equals(users.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }
    public void removeItem(User user) {
        int index = findItemById(user);
        if (index >= 0) {
            users.remove(index);
        }
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final User user = users.get(position);

        if (Utils.isEmpty(view)) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.activity_user_item, viewGroup, false);

            final RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.user_item_layout);
            final ImageView profilePic = (ImageView) view.findViewById(R.id.user_item_profile_pic);
            final TextView username = (TextView) view.findViewById(R.id.user_item_username);
            final TextView joined = (TextView) view.findViewById(R.id.user_item_joined);
            final TextView followers = (TextView) view.findViewById(R.id.user_item_followers);
            final TextView media = (TextView) view.findViewById(R.id.user_item_media);
            final UserAdapter.ViewHolder viewHolder = new UserAdapter.ViewHolder(layout, profilePic, username, joined, followers, media);
            view.setTag(viewHolder);
        }

        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (Utils.isNotEmpty(user)) {
            String profilePicUrl = user.getProfilePicUrl();
            if (Utils.isEmpty(profilePicUrl)) {
                viewHolder.profilePic.setImageResource(R.drawable.placeholder_profile_pic_72);
            } else {
                UrlImageViewHelper.setUrlDrawable(viewHolder.profilePic, profilePicUrl, R.drawable.placeholder_profile_pic_72);
            }
            viewHolder.username.setText(user.getUsername());    // TODO: maksymalna szerokość
            viewHolder.joined.setText(Utils.formatDate(user.getCreated()));
            viewHolder.followers.setText(Utils.isEmpty(user.getFollowers()) ? "0" : user.getFollowers().toString());
            viewHolder.media.setText(Utils.isEmpty(user.getMedia()) ? "0" : user.getMedia().toString());
        }

        return view;
    }

    private class ViewHolder {
        private final RelativeLayout layout;
        private final ImageView profilePic;
        private final TextView username;
        private final TextView joined;
        private final TextView followers;
        private final TextView media;

        public ViewHolder(RelativeLayout layout, ImageView profilePic, TextView username, TextView joined, TextView followers, TextView media) {
            this.layout = layout;
            this.profilePic = profilePic;
            this.username = username;
            this.joined = joined;
            this.followers = followers;
            this.media = media;
        }
    }

}
