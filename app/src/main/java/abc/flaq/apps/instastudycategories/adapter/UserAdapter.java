package abc.flaq.apps.instastudycategories.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.List;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.pojo.User;
import abc.flaq.apps.instastudycategories.utils.Utils;

public class UserAdapter extends BaseAdapter {

    private final Context context;
    private final LayoutInflater inflater;

    private final List<User> users;

    public UserAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (Utils.isEmpty(users)) {
            return 0;
        }
        return users.size();
    }

    @Override
    public User getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final UserViewHolder userViewHolder;
        final User user = users.get(position);

        if (Utils.isEmpty(view)) {
            view = inflater.inflate(R.layout.activity_user_item, viewGroup, false);

            userViewHolder = new UserViewHolder(view);
            view.setTag(userViewHolder);
        } else {
            userViewHolder = (UserViewHolder) view.getTag();
        }

        userViewHolder.bind(user);

        return view;
    }

    private class UserViewHolder {
        private final ImageView profilePic;
        private final TextView username;
        private final TextView joined;
        private final TextView followers;
        private final TextView media;

        public UserViewHolder(View view) {
            profilePic = (ImageView) view.findViewById(R.id.user_item_profile_pic);
            username = (TextView) view.findViewById(R.id.user_item_username);
            joined = (TextView) view.findViewById(R.id.user_item_joined);
            followers = (TextView) view.findViewById(R.id.user_item_followers);
            media = (TextView) view.findViewById(R.id.user_item_media);
        }

        private void bind(User model) {
            if (Utils.isNotEmpty(model)) {
                String profilePicUrl = model.getProfilePicUrl();
                if (Utils.isEmpty(profilePicUrl)) {
                    profilePic.setImageResource(R.drawable.placeholder_profile_pic_72);
                } else {
                    UrlImageViewHelper.setUrlDrawable(profilePic, profilePicUrl, R.drawable.placeholder_profile_pic_72);
                }

                username.setText(model.getUsername());    // TODO: maksymalna szerokość
                joined.setText(Utils.formatDate(model.getCreated()));
                followers.setText("\uD83D\uDC68 " + (Utils.isEmpty(model.getFollowers()) ? "0" : model.getFollowers().toString()));
                media.setText("\uD83D\uDCF7 " + (Utils.isEmpty(model.getMedia()) ? "0" : model.getMedia().toString()));
            }
        }
    }

}
