package abc.flaq.apps.instastudycategories.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.Date;
import java.util.List;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.general.Session;
import abc.flaq.apps.instastudycategories.helper.Utils;
import abc.flaq.apps.instastudycategories.pojo.WebSocketMessage;

import static abc.flaq.apps.instastudycategories.helper.Constants.CHAT_DATE_FORMAT;
import static abc.flaq.apps.instastudycategories.helper.Constants.CHAT_EARLY_DATE_FORMAT;
import static abc.flaq.apps.instastudycategories.helper.Constants.CHAT_LATE_DATE_FORMAT;
import static abc.flaq.apps.instastudycategories.helper.Constants.INSTAGRAM_URL;

public class ChatAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;

    private List<WebSocketMessage> messages;
    private Date yesterday;
    private Date weekAgo;

    public ChatAdapter(Context context, List<WebSocketMessage> messages) {
        this.context = context;
        this.messages = messages;
        this.inflater = LayoutInflater.from(context);
        this.yesterday = Utils.moveDateByDays(new Date(), 0);
        this.weekAgo = Utils.moveDateByDays(new Date(), -5);
    }

    @Override
    public int getCount() {
        if (messages == null) {
            return 0;
        }
        return messages.size();
    }

    @Override
    public WebSocketMessage getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final ChatViewHolder chatViewHolder;
        final WebSocketMessage message = messages.get(position);

        if (Utils.isEmpty(view)) {
            view = inflater.inflate(R.layout.activity_user_chat_item, viewGroup, false);

            chatViewHolder = new ChatViewHolder(view);
            view.setTag(chatViewHolder);
        } else {
            chatViewHolder = (ChatViewHolder) view.getTag();
        }

        Boolean sameAsBefore = (position > 0 && message.getName().equals(messages.get(position - 1).getName()));
        chatViewHolder.bind(message, sameAsBefore);

        return view;
    }

    private class ChatViewHolder {
        private final RelativeLayout layout;
        private final ImageView profilePic;
        private final TextView date;
        private final TextView username;
        private final TextView text;

        private ChatViewHolder(View view) {
            profilePic = (ImageView) view.findViewById(R.id.user_chat_item_profile_pic);
            date = (TextView) view.findViewById(R.id.user_chat_item_date);
            username = (TextView) view.findViewById(R.id.user_chat_item_username);
            text = (TextView) view.findViewById(R.id.user_chat_item_text);
            layout = (RelativeLayout) view;
        }

        private void bind(final WebSocketMessage model, Boolean sameAsBefore) {
            if (Utils.isNotEmpty(model)) {
                if (model.getMessage().contains("@" + Session.getInstance().getUser().getUsername())) {
                    layout.setBackgroundResource(R.drawable.chat_gradient);
                } else {
                    layout.setBackgroundResource(R.color.colorBackgroundLight);
                }

                if (sameAsBefore) {
                    profilePic.setVisibility(View.INVISIBLE);
                } else {
                    profilePic.setVisibility(View.VISIBLE);
                    if (Utils.isEmpty(model.getProfilePic())) {
                        profilePic.setImageResource(R.drawable.placeholder_profile_pic_72);
                    } else {
                        UrlImageViewHelper.setUrlDrawable(profilePic, model.getProfilePic(), R.drawable.placeholder_profile_pic_72);
                    }
                }
                profilePic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Utils.showQuickInfo(view, context.getString(R.string.ig_profile_open) + model.getName() + "...");
                        Intent nextIntent = Utils.getInstagramIntent(model.getName());
                        if (Utils.isIntentAvailable(context, nextIntent)) {
                            Utils.logDebug(context, "Instagram intent is available");
                            context.startActivity(nextIntent);
                        } else {
                            Utils.logDebug(context, "Instagram intent is NOT available");
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL + model.getName())));
                        }
                    }
                });

                if (model.getDate().after(weekAgo)) {
                    if (model.getDate().after(yesterday)) {
                        date.setText(DateFormat.format(CHAT_EARLY_DATE_FORMAT, model.getDate()));
                    } else {
                        date.setText(DateFormat.format(CHAT_DATE_FORMAT, model.getDate()));
                    }
                } else {
                    date.setText(DateFormat.format(CHAT_LATE_DATE_FORMAT, model.getDate()));
                }
                username.setText(model.getName());
                text.setText(model.getMessage());
            }
        }
    }

}
