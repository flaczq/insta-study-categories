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
import java.util.List;
import java.util.Map;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.design.Decorator;
import abc.flaq.apps.instastudycategories.helper.Utils;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;

public class SubcategoryAdapter extends BaseAdapter {

    private final Context context;
    private final LayoutInflater inflater;

    private final ArrayList<Subcategory> subcategories;
    private final Typeface font;
    private Map<String, List<Object>> subcategoriesResources;

    public SubcategoryAdapter(Context context, ArrayList<Subcategory> subcategories, Typeface font) {
        this.context = context;
        this.subcategories = subcategories;
        this.inflater = LayoutInflater.from(context);
        this.font = font;
        this.subcategoriesResources = Utils.getSubcategoriesResources(context, subcategories);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        subcategoriesResources = Utils.getSubcategoriesResources(context, subcategories);
    }

    @Override
    public int getCount() {
        if (subcategories == null) {
            return 0;
        }
        return subcategories.size();
    }

    @Override
    public Subcategory getItem(int position) {
        return subcategories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final SubcategoryViewHolder subcategoryViewHolder;
        final Subcategory subcategory = subcategories.get(position);

        if (Utils.isEmpty(view)) {
            view = inflater.inflate(R.layout.activity_subcategory_item, viewGroup, false);

            subcategoryViewHolder = new SubcategoryViewHolder(view);
            view.setTag(subcategoryViewHolder);
        } else {
            subcategoryViewHolder = (SubcategoryViewHolder) view.getTag();
        }

        String nameResource = (String) subcategoriesResources.get("names").get(position);
        Drawable photoResource = (Drawable) subcategoriesResources.get("photos").get(position);
        subcategoryViewHolder.bind(subcategory, nameResource, photoResource);

        return view;
    }

    private class SubcategoryViewHolder {
        private final IconicsTextView users;
        private final ImageView photo;
        private final TextView name;

        private SubcategoryViewHolder(View view) {
            users = (IconicsTextView) view.findViewById(R.id.subcategory_item_users);
            photo = (ImageView) view.findViewById(R.id.subcategory_item_image);
            name = (TextView) view.findViewById(R.id.subcategory_item_name);

            if (Build.VERSION.SDK_INT >= 21) {
                users.setPadding(0, 5, 0, 0);
                name.setPadding(0, 5, 0, 0);
            } else {
                users.setPadding(0, 3, 0, 0);
                name.setPadding(0, 0, 0, 10);
            }
            users.setTypeface(font);
            name.setTypeface(font);
            name.setLineSpacing(0, 0.7f);
        }

        private void bind(Subcategory model, String nameResource, Drawable photoResource) {
            users.setText("{faw-smile-o} " + model.getUsersSize());

            name.setText(nameResource);
            Decorator.fitFont(name);

            if (Utils.isEmpty(model.getImageUrl())) {
                photo.setImageDrawable(photoResource);
            } else {
                UrlImageViewHelper.setUrlDrawable(photo, model.getImageUrl(), R.drawable.placeholder_category);
            }

            // Calculate height for all except "all" category
            //Decorator.setGridHeight(model.getUsersSize(), photo);
        }
    }

}
