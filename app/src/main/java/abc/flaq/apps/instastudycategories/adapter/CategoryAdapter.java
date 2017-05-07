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
import java.util.Locale;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.utils.Utils;

public class CategoryAdapter extends BaseAdapter {

    private final Context context;
    private final LayoutInflater inflater;

    private final ArrayList<Category> categories;

    public CategoryAdapter(Context context, ArrayList<Category> categories) {
        this.context = context;
        this.categories = categories;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (Utils.isEmpty(categories)) {
            return 0;
        }
        return categories.size();
    }

    @Override
    public Category getItem(int position) {
        return categories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final CategoryViewHolder categoryViewHolder;
        final Category category = getItem(position);

        if (Utils.isEmpty(view)) {
            view = inflater.inflate(R.layout.activity_category_item, viewGroup, false);

            categoryViewHolder = new CategoryViewHolder(view);
            view.setTag(categoryViewHolder);
        } else {
            categoryViewHolder = (CategoryViewHolder) view.getTag();
        }

        categoryViewHolder.bind(category);

        return view;
    }

    private class CategoryViewHolder {
        private final TextView users;
        private final ImageView image;
        private final TextView name;

        private CategoryViewHolder(View view) {
            users = (TextView) view.findViewById(R.id.category_item_subcategories);
            image = (ImageView) view.findViewById(R.id.category_item_image);
            name = (TextView) view.findViewById(R.id.category_item_name);
        }

        private void bind(Category model) {
            //Utils.setSubcategoryGridDesign(model.getUsersSize(), image);
            if (model.isAsSubcategory()) {
                users.setText(String.format(Locale.ENGLISH, "☻ %d", model.getUsersSize()));
            } else {
                users.setText(String.format(Locale.ENGLISH, "▣ %d", model.getSubcategoriesSize()));
            }
            String categoryName = Utils.getStringByCategoryName(context, model.getName());
            name.setText(Utils.simplifyCharacters(categoryName));

            if (Utils.isEmpty(model.getImageUrl())) {
                Drawable drawable = Utils.getCategoryDrawable(context, model.getName());
                if (Utils.isEmpty(drawable)) {
                    image.setImageResource(R.drawable.placeholder_category);
                } else {
                    image.setImageDrawable(drawable);
                }
            } else {
                // FIXME: images on server
                UrlImageViewHelper.setUrlDrawable(image, model.getImageUrl(), R.drawable.placeholder_category);
            }
        }
    }

}
