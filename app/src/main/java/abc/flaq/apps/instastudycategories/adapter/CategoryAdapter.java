package abc.flaq.apps.instastudycategories.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.mikepenz.iconics.view.IconicsTextView;

import java.util.ArrayList;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.design.Decorator;
import abc.flaq.apps.instastudycategories.helper.Utils;
import abc.flaq.apps.instastudycategories.pojo.Category;

public class CategoryAdapter extends BaseAdapter {

    private final Context context;
    private final LayoutInflater inflater;

    private final ArrayList<Category> categories;
    private final Typeface font;

    public CategoryAdapter(Context context, ArrayList<Category> categories, Typeface font) {
        this.context = context;
        this.categories = categories;
        this.inflater = LayoutInflater.from(context);
        this.font = font;
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
        private final IconicsTextView subcategories;
        private final ImageView image;
        private final TextView name;

        private CategoryViewHolder(View view) {
            subcategories = (IconicsTextView) view.findViewById(R.id.category_item_subcategories);
            image = (ImageView) view.findViewById(R.id.category_item_image);
            name = (TextView) view.findViewById(R.id.category_item_name);

            if (Build.VERSION.SDK_INT >= 21) {
                subcategories.setPadding(0, 5, 0, 0);
                name.setPadding(0, 5, 0, 0);
            } else {
                subcategories.setPadding(0, 3, 0, 0);
                name.setPadding(0, 0, 0, 10);
            }
            subcategories.setTypeface(font);
            name.setTypeface(font);
            name.setLineSpacing(0, 0.7f);
        }

        private void bind(Category model) {
            if (model.isAsSubcategory()) {
                subcategories.setText("{faw-smile-o} " + model.getUsersSize());
            } else {
                subcategories.setText("{faw-th-large} " + model.getSubcategoriesSize());
            }

            String categoryName = Utils.getStringByCategoryName(context, model.getName());
            name.setText(Utils.simplifyCharacters(categoryName));
            Decorator.fitFont(name);

            if (Utils.isEmpty(model.getImageUrl())) {
                Drawable drawable = Utils.getCategoryDrawable(context, model.getName());
                if (Utils.isEmpty(drawable)) {
                    image.setImageResource(R.drawable.placeholder_category);
                } else {
                    image.setImageDrawable(drawable);
                }
            } else {
                UrlImageViewHelper.setUrlDrawable(image, model.getImageUrl(), R.drawable.placeholder_category);
            }

            // Calculate height for all except "all" category
            if (!"all".equals(model.getName())) {
                Decorator.setGridHeight(model.getSubcategoriesSize(), image);
            }
        }
    }

}
