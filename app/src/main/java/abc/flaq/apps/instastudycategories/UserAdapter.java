package abc.flaq.apps.instastudycategories;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

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
    public Object getItem(int position) {
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

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final User user = users.get(position);

        if (Utils.isEmpty(view)) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.activity_user_item, viewGroup, false);

            final RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.user_item_layout);
            final TextView textView = (TextView) view.findViewById(R.id.user_item_textview);
            final UserAdapter.ViewHolder viewHolder = new UserAdapter.ViewHolder(layout, textView);
            view.setTag(viewHolder);
        }

        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.textView.setText(user.getUsername());

        return view;
    }

    private class ViewHolder {
        private final RelativeLayout layout;
        private final TextView textView;

        public ViewHolder(RelativeLayout layout, TextView textView) {
            this.layout = layout;
            this.textView = textView;
        }
    }

}
