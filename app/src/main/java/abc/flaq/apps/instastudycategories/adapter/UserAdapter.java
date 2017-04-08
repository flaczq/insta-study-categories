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
import abc.flaq.apps.instastudycategories.utils.GeneralUtils;

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

    public String getUsername(int position) {
        if (position < users.size()) {
            return users.get(position).getUsername();
        }
        return null;
    }

    public void addItem(User user) {
        users.add(0, user);
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final User user = users.get(position);

        if (GeneralUtils.isEmpty(view)) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.activity_user_item, viewGroup, false);

            final RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.user_item_layout);
            final TextView username = (TextView) view.findViewById(R.id.user_item_username);
            final ImageView profilePic = (ImageView) view.findViewById(R.id.user_item_profile_pic);
            final UserAdapter.ViewHolder viewHolder = new UserAdapter.ViewHolder(layout, username, profilePic);
            view.setTag(viewHolder);
        }

        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.username.setText(user.getUsername());
        String profilePicUrl = user.getProfilePicUrl();
        if (GeneralUtils.isEmpty(profilePicUrl)) {
            viewHolder.profilePic.setImageResource(R.drawable.splash_image);
        } else {
            UrlImageViewHelper.setUrlDrawable(viewHolder.profilePic, profilePicUrl, R.drawable.splash_image);
        }

        return view;
    }

    private class ViewHolder {
        private final RelativeLayout layout;
        private final TextView username;
        private final ImageView profilePic;

        public ViewHolder(RelativeLayout layout, TextView username, ImageView profilePic) {
            this.layout = layout;
            this.username = username;
            this.profilePic = profilePic;
        }
    }

}
