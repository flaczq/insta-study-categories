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
import android.widget.AdapterView;

import com.crystal.crystalpreloaders.widgets.CrystalPreloader;
import com.etsy.android.grid.StaggeredGridView;

import java.util.ArrayList;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.activity.UserActivity;
import abc.flaq.apps.instastudycategories.adapter.SubcategoryAdapter;
import abc.flaq.apps.instastudycategories.pojo.Subcategory;
import abc.flaq.apps.instastudycategories.utils.Utils;

import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_SUBCATEGORY;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_SUBCATEGORY_FOREIGN_ID;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_SUBCATEGORY_INACTIVE;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_SUBCATEGORY_INACTIVE_END;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_SUBCATEGORY_INACTIVE_START;
import static abc.flaq.apps.instastudycategories.utils.Constants.INTENT_SUBCATEGORY_NAME;

public class SubcategoryInactiveFragment extends Fragment {

    private SubcategoryAdapter subcategoryAdapter;
    private StaggeredGridView gridView;
    private CrystalPreloader preloader;

    private int tabNo;
    private ArrayList<Subcategory> inactiveSubcategories = new ArrayList<>();

    public static SubcategoryInactiveFragment newInstance(int tabNo) {
        Bundle args = new Bundle();
        //args.putInt(BUNDLE_SUBCATEGORY_TAB_NO, tabNo);
        SubcategoryInactiveFragment fragment = new SubcategoryInactiveFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        //tabNo = args.getInt(BUNDLE_SUBCATEGORY_TAB_NO);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_subcategory, container, false);

        preloader = (CrystalPreloader) rootView.findViewById(R.id.subcategory_preloader);
        //preloader.setVisibility(View.VISIBLE);

        subcategoryAdapter = new SubcategoryAdapter(getActivity(), inactiveSubcategories);

        gridView = (StaggeredGridView) rootView.findViewById(R.id.subcategory_grid);
        gridView.setAdapter(subcategoryAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                Subcategory selected = subcategoryAdapter.getItem(position);
                Intent nextIntent = new Intent(getActivity(), UserActivity.class);
                nextIntent.putExtra(INTENT_SUBCATEGORY_FOREIGN_ID, selected.getForeignId());
                nextIntent.putExtra(INTENT_SUBCATEGORY_NAME, selected.getName());
                getActivity().startActivity(nextIntent);
            }
        });

        return rootView;
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
        filter.addAction(INTENT_SUBCATEGORY);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(INTENT_SUBCATEGORY_INACTIVE_START)) {
                gridView.setVisibility(View.INVISIBLE);
                preloader.setVisibility(View.VISIBLE);
            }
            if (intent.hasExtra(INTENT_SUBCATEGORY_INACTIVE_END)) {
                preloader.setVisibility(View.INVISIBLE);
                gridView.setVisibility(View.VISIBLE);
            }
            if (intent.hasExtra(INTENT_SUBCATEGORY_INACTIVE)) {
                ArrayList<Subcategory> newCategories = intent.getParcelableArrayListExtra(INTENT_SUBCATEGORY_INACTIVE);
                if (Utils.isNotEmpty(newCategories)) {
                    inactiveSubcategories.clear();
                    inactiveSubcategories.addAll(newCategories);
                    subcategoryAdapter.notifyDataSetChanged();
                }
            }
        }
    };

}
