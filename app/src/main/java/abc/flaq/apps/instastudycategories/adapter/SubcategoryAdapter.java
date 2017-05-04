package abc.flaq.apps.instastudycategories.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.List;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;
import abc.flaq.apps.instastudycategories.utils.Utils;

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
        final ViewHolder viewHolder;
        final Subcategory subcategory = subcategories.get(position);

        if (Utils.isEmpty(view)) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.activity_subcategory_item, viewGroup, false);

            final ImageView image = (ImageView) view.findViewById(R.id.subcategory_item_image);
            final TextView name = (TextView) view.findViewById(R.id.subcategory_item_name);
            viewHolder = new ViewHolder(image, name);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Utils.setSubcategoryGridDesign(subcategory.getUsersSize(), maxSize, viewHolder.image);
        String subcategoryName = Utils.getStringBySubcategoryName(context, subcategory.getName());
        viewHolder.name.setText(subcategoryName);

        if (Utils.isEmpty(subcategory.getImageUrl())) {
            Drawable drawable = Utils.getSubcategoryDrawable(context, subcategory.getName());
            if (Utils.isEmpty(drawable)) {
                viewHolder.image.setImageResource(R.drawable.placeholder_category);
            } else {
                viewHolder.image.setImageDrawable(drawable);
            }
        } else {
            UrlImageViewHelper.setUrlDrawable(viewHolder.image, subcategory.getImageUrl(), R.drawable.placeholder_category);   // FIXME: images on server
        }

        return view;
    }

    private class ViewHolder {
        private final ImageView image;
        private final TextView name;

        private ViewHolder(ImageView image, TextView name) {
            this.image = image;
            this.name = name;
        }
    }

}
