package abc.flaq.apps.instastudycategories.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.helper.Utils;
import abc.flaq.apps.instastudycategories.pojo.WebSocketMessage;

import static abc.flaq.apps.instastudycategories.helper.Constants.FULL_DATE_FORMAT;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private int itemLayout;
    private List<WebSocketMessage> messages;

    public ChatAdapter(List<WebSocketMessage> messages, int itemLayout) {
        this.messages = messages;
        this.itemLayout = itemLayout;
    }

    public void addItem(WebSocketMessage item) {
        messages.add(item);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (Utils.isEmpty(messages)) {
            return 0;
        }
        return messages.size();
    }

    @Override
    public ChatAdapter.ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChatAdapter.ChatViewHolder holder, int position) {
        WebSocketMessage message = messages.get(position);
        holder.bind(message);
        holder.itemView.setTag(message);
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        private final TextView date;
        private final TextView username;
        private final TextView text;

        public ChatViewHolder(View view) {
            super(view);
            date = (TextView) view.findViewById(R.id.user_chat_item_date);
            username = (TextView) view.findViewById(R.id.user_chat_item_username);
            text = (TextView) view.findViewById(R.id.user_chat_item_text);
        }

        private void bind(WebSocketMessage model) {
            if (Utils.isNotEmpty(model)) {
                date.setText(Utils.formatDate(model.getDate(), FULL_DATE_FORMAT));
                username.setText(model.getName());
                text.setText(model.getMessage());
            }
        }
    }

}
