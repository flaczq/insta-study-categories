package abc.flaq.apps.instastudycategories;

import android.content.Context;
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
    private int maxSize;

    public CategoryAdapter(Context context, List<Category> categories) {
        this.context = context;
        this.categories = categories;
        // Assuming categories are sorted by size desc
        this.maxSize = (categories.size() > 0 ? categories.get(0).getUsersSize() : 10);
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Category getItem(int position) {
        if (position < categories.size()) {
            return categories.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public String getItemRealId(int position) {
        if (position < categories.size()) {
            return categories.get(position).getId();
        }
        return null;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final Category category = categories.get(position);

        if (Utils.isEmpty(view)) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.activity_category_item, viewGroup, false);

            final RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.category_item_layout);
            final TextView textView = (TextView) view.findViewById(R.id.category_item_name);
            final ViewHolder viewHolder = new ViewHolder(layout, textView);
            view.setTag(viewHolder);
        }

        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        Utils.setGridDesign(position, category.getUsersSize(), maxSize, viewHolder.layout);
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
