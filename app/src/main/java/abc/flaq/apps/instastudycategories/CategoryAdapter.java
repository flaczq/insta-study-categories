package abc.flaq.apps.instastudycategories;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class CategoryAdapter extends BaseAdapter {

    private final Context context;
    private final List<Category> categories;

    public CategoryAdapter(Context context, List<Category> categories) {
        this.context = context;
        this.categories = categories;
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final Category category = categories.get(i);

        if (Utils.isEmpty(view)) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.activity_category_item, viewGroup, false);

            final RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.category_item_layout);
            final TextView textView = (TextView) view.findViewById(R.id.category_item_textview);
            final ViewHolder viewHolder = new ViewHolder(layout, textView);
            view.setTag(viewHolder);
        }

        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (i == 0) {
            viewHolder.layout.setMinimumHeight(222);
            viewHolder.layout.setBackgroundColor(Color.parseColor("#B176B1"));
        } else if (i == 1) {
            viewHolder.layout.setMinimumHeight(159);
            viewHolder.layout.setBackgroundColor(Color.parseColor("#968089"));
        } else if (i == 2) {
            viewHolder.layout.setBackgroundColor(Color.parseColor("#F19C7F"));
        } else {
            viewHolder.layout.setBackgroundColor(Color.parseColor("#98CC9E"));
        }
        viewHolder.textView.setText(category.getName());

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
