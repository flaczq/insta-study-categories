package abc.flaq.apps.instastudycategories.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;
import abc.flaq.apps.instastudycategories.utils.Utils;

import static abc.flaq.apps.instastudycategories.utils.Constants.STRINGS_SUBCATEGORY_PREFIX;

public class SubcategoryAdapter extends BaseAdapter {

    private final Context context;
    private final List<Subcategory> subcategories;
    private int maxSize;

    public SubcategoryAdapter(Context context, List<Subcategory> subcategories) {
        this.context = context;
        this.subcategories = subcategories;
        // Assuming subcategories are sorted by size desc
        this.maxSize = (subcategories.size() > 0 ? subcategories.get(0).getUsersSize() : -1);
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

    public String getForeignId(int position) {
        if (position < subcategories.size()) {
            return subcategories.get(position).getForeignId();
        }
        return null;
    }

    public void addItem(Subcategory subcategory) {
        subcategories.add(subcategory);
    }

    private Integer findItemById(Subcategory subcategory) {
        for (int i = 0; i < subcategories.size(); i++) {
            if (subcategory.getId().equals(subcategories.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }
    public void updateItem(Subcategory subcategory) {
        int index = findItemById(subcategory);
        if (index >= 0) {
            subcategories.add(index, subcategory);
        }
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final Subcategory subcategory = subcategories.get(position);

        if (Utils.isEmpty(view)) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.activity_subcategory_item, viewGroup, false);

            final RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.subcategory_item_layout);
            final ImageView image = (ImageView) view.findViewById(R.id.subcategory_item_image);
            final TextView name = (TextView) view.findViewById(R.id.subcategory_item_name);
            final SubcategoryAdapter.ViewHolder viewHolder = new ViewHolder(layout, image, name);
            view.setTag(viewHolder);
        }

        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        Utils.setSubcategoryGridDesign(position, subcategory.getUsersSize(), maxSize, viewHolder.layout);
        String subcategoryName = Utils.getStringByName(context, STRINGS_SUBCATEGORY_PREFIX + subcategory.getName());
        viewHolder.name.setText(subcategoryName);

        return view;
    }

    private class ViewHolder {
        private final RelativeLayout layout;
        private final ImageView image;
        private final TextView name;

        private ViewHolder(RelativeLayout layout, ImageView image, TextView name) {
            this.layout = layout;
            this.image = image;
            this.name = name;
        }
    }

}
