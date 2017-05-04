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

import java.util.ArrayList;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.utils.Utils;

public class CategoryAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<Category> categories;
    private int maxSize;

    public CategoryAdapter(Context context, ArrayList<Category> categories) {
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
        final ViewHolder viewHolder;
        final Category category = categories.get(position);

        if (Utils.isEmpty(view)) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.activity_category_item, viewGroup, false);

            final ImageView image = (ImageView) view.findViewById(R.id.category_item_image);
            final TextView name = (TextView) view.findViewById(R.id.category_item_name);
            viewHolder = new ViewHolder(image, name);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Utils.setSubcategoryGridDesign(category.getUsersSize(), maxSize, viewHolder.image);
        String categoryName = Utils.getStringByCategoryName(context, category.getName());
        viewHolder.name.setText(Utils.changeExtraCharacters(categoryName));

        if (Utils.isEmpty(category.getImageUrl())) {
            Drawable drawable = Utils.getCategoryDrawable(context, category.getName());
            if (Utils.isEmpty(drawable)) {
                viewHolder.image.setImageResource(R.drawable.placeholder_category);
            } else {
                viewHolder.image.setImageDrawable(drawable);
            }
        } else {
            UrlImageViewHelper.setUrlDrawable(viewHolder.image, category.getImageUrl(), R.drawable.placeholder_category);   // FIXME: images on server
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
