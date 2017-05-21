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

import java.util.List;
import java.util.Locale;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;
import abc.flaq.apps.instastudycategories.helper.Utils;

import static android.util.TypedValue.COMPLEX_UNIT_SP;

public class SubcategoryAdapter extends BaseAdapter {

    private final Context context;
    private final LayoutInflater inflater;

    private final List<Subcategory> subcategories;

    public SubcategoryAdapter(Context context, List<Subcategory> subcategories) {
        this.context = context;
        this.subcategories = subcategories;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (Utils.isEmpty(subcategories)) {
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

        subcategoryViewHolder.bind(subcategory);

        return view;
    }

    private class SubcategoryViewHolder {
        private final TextView users;
        private final ImageView image;
        private final TextView name;

        private SubcategoryViewHolder(View view) {
            users = (TextView) view.findViewById(R.id.subcategory_item_users);
            image = (ImageView) view.findViewById(R.id.subcategory_item_image);
            name = (TextView) view.findViewById(R.id.subcategory_item_name);

            if (Build.VERSION.SDK_INT >= 21) {
                users.setPadding(0, 3, 0, 0);
                name.setPadding(0, 5, 0, 0);
            } else {
                users.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
                name.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            }
        }

        private void bind(Subcategory model) {
            // TODO: turn this on maybe?
            //Utils.setSubcategoryGridDesign(model.getUsersSize(), subcategoryViewHolder.image);
            if (Build.VERSION.SDK_INT >= 21) {
                users.setText(String.format(Locale.ENGLISH, "☻ %d", model.getUsersSize()));
            } else {
                users.setText(String.format(Locale.ENGLISH, "☺ %d", model.getUsersSize()));
            }
            String subcategoryName = Utils.getStringBySubcategoryName(context, model.getName());
            name.setText(Utils.simplifyCharacters(subcategoryName));
            if (name.getText().length() >= 10) {
                name.setTextSize(COMPLEX_UNIT_SP, 22);
            }

            if (Utils.isEmpty(model.getImageUrl())) {
                Drawable drawable = Utils.getSubcategoryDrawable(context, model.getName());
                if (Utils.isEmpty(drawable)) {
                    image.setImageResource(R.drawable.placeholder_category);
                } else {
                    image.setImageDrawable(drawable);
                }
            } else {
                // FIXME!!: images on server
                UrlImageViewHelper.setUrlDrawable(image, model.getImageUrl(), R.drawable.placeholder_category);
            }
        }
    }

}
