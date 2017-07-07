package abc.flaq.apps.instastudycategories.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.crystal.crystalpreloaders.widgets.CrystalPreloader;
import com.etsy.android.grid.StaggeredGridView;

import java.util.ArrayList;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.activity.SubcategoryActivity;
import abc.flaq.apps.instastudycategories.activity.UserActivity;
import abc.flaq.apps.instastudycategories.adapter.CategoryAdapter;
import abc.flaq.apps.instastudycategories.general.Session;
import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.helper.Utils;
import abc.flaq.apps.instastudycategories.pojo.EveObject;
import abc.flaq.apps.instastudycategories.pojo.Info;

import static abc.flaq.apps.instastudycategories.helper.Constants.FONT_CATEGORY_NAME;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_ACTIVE_INFO;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_INACTIVE_END;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_FOREIGN_ID;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_INACTIVE;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_NAME;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_INACTIVE_START;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_CATEGORY_USERS_SIZE;

public class CategoryInactiveFragment extends Fragment {

    private CategoryAdapter categoryAdapter;
    private StaggeredGridView gridView;
    private CrystalPreloader preloader;

    private int tabNo;
    private ArrayList<EveObject> inactiveCategories = new ArrayList<>();

    public static CategoryInactiveFragment newInstance(int tabNo) {
        Bundle args = new Bundle();
        //args.putInt(BUNDLE_CATEGORY_TAB_NO, tabNo);
        CategoryInactiveFragment fragment = new CategoryInactiveFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*Bundle args = getArguments();
        tabNo = args.getInt(BUNDLE_CATEGORY_TAB_NO);*/
    }
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.activity_category, container, false);

        preloader = (CrystalPreloader) parentView.findViewById(R.id.category_preloader);
        preloader.setVisibility(View.VISIBLE);

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), FONT_CATEGORY_NAME);
        categoryAdapter = new CategoryAdapter(getActivity(), inactiveCategories, font);

        gridView = (StaggeredGridView) parentView.findViewById(R.id.category_grid);
        gridView.setAdapter(categoryAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                if (categoryAdapter.getItem(position) instanceof Category) {
                    Category selected = (Category) categoryAdapter.getItem(position);
                    Intent nextIntent;
                    if (selected.isAsSubcategory()) {
                        nextIntent = new Intent(getActivity(), UserActivity.class);
                    } else {
                        nextIntent = new Intent(getActivity(), SubcategoryActivity.class);
                    }
                    nextIntent.putExtra(INTENT_CATEGORY_FOREIGN_ID, selected.getForeignId());
                    nextIntent.putExtra(INTENT_CATEGORY_NAME, selected.getName());
                    nextIntent.putExtra(INTENT_CATEGORY_USERS_SIZE, selected.getUsersSize());
                    getActivity().startActivity(nextIntent);
                } else {
                    Utils.showInfo(container, "Tutaj będą prezentowane nowości i ważne informacje o aplikacji");
                }
            }
        });

        return parentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        setBroadcastReceivers();
    }
    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        super.onPause();
    }

    private void setBroadcastReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_CATEGORY);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(INTENT_CATEGORY_INACTIVE_START)) {
                gridView.setVisibility(View.INVISIBLE);
                preloader.setVisibility(View.VISIBLE);
            }
            if (intent.hasExtra(INTENT_CATEGORY_INACTIVE_END)) {
                preloader.setVisibility(View.INVISIBLE);
                gridView.setVisibility(View.VISIBLE);
            }
            if (intent.hasExtra(INTENT_CATEGORY_INACTIVE)) {
                ArrayList<Category> newCategories = intent.getParcelableArrayListExtra(INTENT_CATEGORY_INACTIVE);
                if (Utils.isNotEmpty(newCategories)) {
                    inactiveCategories.clear();
                    inactiveCategories.addAll(newCategories);
                    if (inactiveCategories.size() > 1) {
                        Info newestInfo = (Utils.isEmpty(Session.getInstance().getNewestInfo()) ? new Info() : Session.getInstance().getNewestInfo());
                        if (inactiveCategories.get(1) instanceof Info) {
                            inactiveCategories.set(1, newestInfo);
                        } else {
                            inactiveCategories.add(1, newestInfo);
                        }
                    }
                    categoryAdapter.notifyDataSetChanged();
                }
            }
            if (intent.hasExtra(INTENT_CATEGORY_ACTIVE_INFO) && inactiveCategories.size() > 1) {
                Info newInfo = intent.getParcelableExtra(INTENT_CATEGORY_ACTIVE_INFO);
                if (Utils.isNotEmpty(newInfo)) {
                    if (inactiveCategories.get(1) instanceof Info) {
                        inactiveCategories.set(1, newInfo);
                    } else {
                        inactiveCategories.add(1, newInfo);
                    }
                    categoryAdapter.notifyDataSetChanged();
                }
            }
        }
    };

}
