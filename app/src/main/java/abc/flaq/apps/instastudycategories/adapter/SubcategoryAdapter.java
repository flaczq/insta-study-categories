package abc.flaq.apps.instastudycategories.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;
import abc.flaq.apps.instastudycategories.utils.GeneralUtils;

public class SubcategoryAdapter extends BaseAdapter {

    private final Context context;
    private final List<Subcategory> subcategories;
    private int maxSize;

    public SubcategoryAdapter(Context context, List<Subcategory> subcategories) {
        this.context = context;
        this.subcategories = subcategories;
        // Assuming subcategories are sorted by size desc
        this.maxSize = (subcategories.size() > 0 ? subcategories.get(0).getUsersSize() : 10);
    }

    @Override
    public int getCount() {
        return subcategories.size();
    }

    @Override
    public Subcategory getItem(int position) {
        if (position < subcategories.size()) {
            return subcategories.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public String getItemRealId(int position) {
        if (position < subcategories.size()) {
            return subcategories.get(position).getId();
        }
        return null;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final Subcategory subcategory = subcategories.get(position);

        if (GeneralUtils.isEmpty(view)) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.activity_subcategory_item, viewGroup, false);

            final RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.subcategory_item_layout);
            final TextView textView = (TextView) view.findViewById(R.id.subcategory_item_name);
            final SubcategoryAdapter.ViewHolder viewHolder = new ViewHolder(layout, textView);
            view.setTag(viewHolder);
        }

        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        GeneralUtils.setGridDesign(position, subcategory.getUsersSize(), maxSize, viewHolder.layout);
        viewHolder.textView.setText(subcategory.getName());

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
