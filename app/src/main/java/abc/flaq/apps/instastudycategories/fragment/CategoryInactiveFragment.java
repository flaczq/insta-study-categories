package abc.flaq.apps.instastudycategories.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crystal.crystalpreloaders.widgets.CrystalPreloader;
import com.etsy.android.grid.StaggeredGridView;

import java.util.ArrayList;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.adapter.CategoryAdapter;
import abc.flaq.apps.instastudycategories.pojo.Category;
import abc.flaq.apps.instastudycategories.utils.Utils;

import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_INACTIVE;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_CATEGORY_INACTIVE_ADD_NEW;

public class CategoryInactiveFragment extends Fragment {

    private CategoryAdapter categoryAdapter;
    private StaggeredGridView gridView;
    private CrystalPreloader preloader;

    private int tabNo;
    private ArrayList<Category> inactiveCategories = new ArrayList<>();

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
        setBroadcastReceivers();
        Bundle args = getArguments();
        //tabNo = args.getInt(BUNDLE_CATEGORY_TAB_NO);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_category, container, false);
        preloader = (CrystalPreloader) rootView.findViewById(R.id.category_preloader);
        preloader.setVisibility(View.VISIBLE);

        categoryAdapter = new CategoryAdapter(getActivity(), inactiveCategories);
        gridView = (StaggeredGridView) rootView.findViewById(R.id.category_grid);
        gridView.setAdapter(categoryAdapter);

        return rootView;
    }

    private void setBroadcastReceivers() {
        InactiveCategoriesBroadcastReceiver receiver = new InactiveCategoriesBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_CATEGORY);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }

    private class InactiveCategoriesBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(INTENT_CATEGORY_INACTIVE)) {
                if (preloader.isShown()) {
                    preloader.setVisibility(View.GONE);
                }

                ArrayList<Category> newCategories = intent.getParcelableArrayListExtra(INTENT_CATEGORY_INACTIVE);
                if (Utils.isNotEmpty(newCategories)) {
                    inactiveCategories.clear();
                    inactiveCategories.addAll(newCategories);
                    categoryAdapter.notifyDataSetChanged();
                }
            }
            if (intent.hasExtra(INTENT_CATEGORY_INACTIVE_ADD_NEW)) {
                // FIXME: NOT WORKING - Scroll to bottom to see new category -
                //gridView.setSelection(categoryAdapter.getCount() - 1);
            }
        }
    }

}
