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
import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.utils.Utils;

import static abc.flaq.apps.instastudycategories.utils.Constants.STRINGS_CATEGORY_PREFIX;

public class CategoryAdapter extends BaseAdapter {

    private final Context context;
    private final List<Category> categories;
    private int maxSize;

    public CategoryAdapter(Context context, List<Category> categories) {
        this.context = context;
        this.categories = categories;
        // Assuming categories are sorted by size desc
        this.maxSize = (categories.size() > 0 ? categories.get(0).getUsersSize() : -1);
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

    public String getForeignId(int position) {
        if (position < categories.size()) {
            return categories.get(position).getForeignId();
        }
        return null;
    }

    public void addItem(Category category) {
        categories.add(category);
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final Category category = categories.get(position);

        if (Utils.isEmpty(view)) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.activity_category_item, viewGroup, false);

            final RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.category_item_layout);
            final ImageView image = (ImageView) view.findViewById(R.id.category_item_image);
            final TextView name = (TextView) view.findViewById(R.id.category_item_name);
            final ViewHolder viewHolder = new ViewHolder(layout, image, name);
            view.setTag(viewHolder);
        }

        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        Utils.setSubcategoryGridDesign(category.getUsersSize(), maxSize, viewHolder.image);
        String categoryName = Utils.getStringByName(context, STRINGS_CATEGORY_PREFIX + category.getName());
        viewHolder.name.setText(categoryName);

        int drawable = Utils.getCategoryDrawable(context, category.getName());
        if (Utils.isEmpty(category.getImageUrl())) {
            if (drawable == 0) {
                viewHolder.image.setImageResource(R.drawable.placeholder_category);
            } else {
                viewHolder.image.setImageResource(drawable);
            }
        } else {
            UrlImageViewHelper.setUrlDrawable(viewHolder.image, category.getImageUrl(), R.drawable.placeholder_category);   // FIXME: images on server
        }

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
