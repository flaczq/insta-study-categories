package abc.flaq.apps.instastudycategories.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.List;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.helper.Utils;
import abc.flaq.apps.instastudycategories.pojo.User;

import static abc.flaq.apps.instastudycategories.helper.Constants.INSTAGRAM_URL;
import static abc.flaq.apps.instastudycategories.helper.Constants.USER_FULL_DATE_FORMAT;

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

        private UserViewHolder(View view) {
            profilePic = (ImageView) view.findViewById(R.id.user_item_profile_pic);
            username = (TextView) view.findViewById(R.id.user_item_username);
            joined = (TextView) view.findViewById(R.id.user_item_joined);
            followers = (TextView) view.findViewById(R.id.user_item_followers);
            media = (TextView) view.findViewById(R.id.user_item_media);
        }

        private void bind(final User model) {
            if (Utils.isNotEmpty(model)) {
                String profilePicUrl = model.getProfilePicUrl();
                if (Utils.isEmpty(profilePicUrl)) {
                    profilePic.setImageResource(R.drawable.placeholder_profile_pic_72);
                } else {
                    UrlImageViewHelper.setUrlDrawable(profilePic, profilePicUrl, R.drawable.placeholder_profile_pic_72);
                }
                profilePic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent nextIntent = Utils.getInstagramIntent(model.getUsername());

                        if (Utils.isIntentAvailable(context, nextIntent)) {
                            Utils.logDebug(context, "Instagram intent is available");
                            context.startActivity(nextIntent);
                        } else {
                            Utils.logDebug(context, "Instagram intent is NOT available");
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL + model.getUsername())));
                        }
                    }
                });

                username.setText(model.getUsername());
                if (Utils.isNotEmpty(model.getJoinDate())) {
                    joined.setText(Utils.formatDate(model.getJoinDate(), USER_FULL_DATE_FORMAT));
                }
                followers.setText("\uD83D\uDE03 " + (Utils.isEmpty(model.getFollowers()) ? "0" : Utils.formatNumber(model.getFollowers())));
                media.setText("\uD83D\uDCF7 " + (Utils.isEmpty(model.getMedia()) ? "0" : model.getMedia().toString()));
            }
        }
    }

}
