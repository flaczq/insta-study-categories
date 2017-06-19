package abc.flaq.apps.instastudycategories.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.mikepenz.iconics.view.IconicsTextView;

import java.util.ArrayList;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.design.Decorator;
import abc.flaq.apps.instastudycategories.design.TypeWriter;
import abc.flaq.apps.instastudycategories.helper.Utils;
import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.pojo.EveObject;
import abc.flaq.apps.instastudycategories.pojo.Info;

import static abc.flaq.apps.instastudycategories.helper.Constants.DB_INFO_TYPE_ERROR;
import static abc.flaq.apps.instastudycategories.helper.Constants.DB_INFO_TYPE_NEWS;
import static abc.flaq.apps.instastudycategories.helper.Constants.DB_INFO_TYPE_WARN;

public class CategoryAdapter extends BaseAdapter {

    private final static int ITEM_TYPE_CATEGORY = 0;
    private final static int ITEM_TYPE_INFO = 1;

    private final Context context;
    private final LayoutInflater inflater;

    private final ArrayList<EveObject> categories;
    private final Typeface font;
    private Boolean firstTime;

    public CategoryAdapter(Context context, ArrayList<EveObject> categories, Typeface font) {
        this.context = context;
        this.categories = categories;
        this.inflater = LayoutInflater.from(context);
        this.font = font;
        this.firstTime = true;
    }

    @Override
    public int getCount() {
        if (Utils.isEmpty(categories)) {
            return 0;
        }
        return categories.size();
    }

    @Override
    public EveObject getItem(int position) {
        return categories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < 0 || position >= categories.size()) {
            return ITEM_TYPE_CATEGORY;
        }
        if (categories.get(position) instanceof Category) {
            return ITEM_TYPE_CATEGORY;
        }
        return ITEM_TYPE_INFO;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final CategoryViewHolder categoryViewHolder;
        final InfoViewHolder infoViewHolder;
        int type = getItemViewType(position);

        if (type == ITEM_TYPE_CATEGORY) {
            final Category category = (Category) getItem(position);

            if (Utils.isEmpty(view)) {
                view = inflater.inflate(R.layout.activity_category_item, viewGroup, false);

                categoryViewHolder = new CategoryViewHolder(view);
                view.setTag(categoryViewHolder);
            } else {
                categoryViewHolder = (CategoryViewHolder) view.getTag();
            }

            categoryViewHolder.bind(category);
        } else {
            // Info grid
            final Info info = (Info) getItem(position);

            if (Utils.isEmpty(view)) {
                view = inflater.inflate(R.layout.activity_category_info_item, viewGroup, false);

                infoViewHolder = new InfoViewHolder(view);
                view.setTag(infoViewHolder);
            } else {
                infoViewHolder = (InfoViewHolder) view.getTag();
            }

            infoViewHolder.bind(info);
        }

        return view;
    }

    private class CategoryViewHolder {
        private final IconicsTextView subcategories;
        private final ImageView photo;
        private final TextView name;

        private CategoryViewHolder(View view) {
            subcategories = (IconicsTextView) view.findViewById(R.id.category_item_subcategories);
            photo = (ImageView) view.findViewById(R.id.category_item_image);
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
                    photo.setImageResource(R.drawable.placeholder_category);
                } else {
                    photo.setImageDrawable(drawable);
                }
            } else {
                UrlImageViewHelper.setUrlDrawable(photo, model.getImageUrl(), R.drawable.placeholder_category);
            }

            // Calculate height for all except "all" category
            /*if (!"all".equals(model.getName())) {
                Decorator.setGridHeight(model.getSubcategoriesSize(), photo);
            }*/
        }
    }
    private class InfoViewHolder {
        private final RelativeLayout layout;
        private final TextView title;
        private final TypeWriter message;

        private InfoViewHolder(View view) {
            layout = (RelativeLayout) view.findViewById(R.id.category_item_info_layout);
            title = (TextView) view.findViewById(R.id.category_item_info_title);
            message = (TypeWriter) view.findViewById(R.id.category_item_info_message);

            if (Build.VERSION.SDK_INT >= 21) {
                title.setPadding(0, 5, 0, 0);
            } else {
                title.setPadding(0, 3, 0, 0);
            }
            title.setTypeface(font);
            message.setTypeface(font);
            message.setLineSpacing(0, 0.7f);
            message.setCharacterDelay(100);
        }

        private void bind(Info model) {
            if (Utils.isEmpty(model) || (Utils.isEmpty(model.getTitle()) && Utils.isEmpty(model.getMessage()))) {
                title.setText("INFORMACJE");
                message.setText("Wczytywanie...");
            } else {
                switch (model.getType()) {
                    case DB_INFO_TYPE_ERROR:
                        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorCategoryRed));
                        break;
                    case DB_INFO_TYPE_WARN:
                        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorCategoryPurple));
                        break;
                    case DB_INFO_TYPE_NEWS:
                    default:
                        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorCategoryBlueLight));
                        break;
                }

                title.setText(model.getTitle());
                if (firstTime) {
                    message.animateText(model.getMessage());
                    message.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            firstTime = false;
                        }
                    }, 1000);
                } else if (!message.isAnimating()) {
                    message.setText(model.getMessage());
                }
            }
        }
    }

}
