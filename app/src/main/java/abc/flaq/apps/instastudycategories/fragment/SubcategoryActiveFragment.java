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
import abc.flaq.apps.instastudycategories.helper.Utils;

import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_ACTIVE;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_ACTIVE_END;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_FOREIGN_ID;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_NAME;
import static abc.flaq.apps.instastudycategories.helper.Constants.INTENT_SUBCATEGORY_ACTIVE_START;

public class SubcategoryActiveFragment extends Fragment {

    private SubcategoryAdapter subcategoryAdapter;
    private StaggeredGridView gridView;
    private CrystalPreloader preloader;

    private int tabNo;
    private ArrayList<Subcategory> activeSubcategories = new ArrayList<>();

    public static SubcategoryActiveFragment newInstance(int tabNo) {
        Bundle args = new Bundle();
        //args.putInt(BUNDLE_SUBCATEGORY_TAB_NO, tabNo);
        SubcategoryActiveFragment fragment = new SubcategoryActiveFragment();
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
        View parentView = inflater.inflate(R.layout.activity_subcategory, container, false);

        preloader = (CrystalPreloader) parentView.findViewById(R.id.subcategory_preloader);
        //preloader.setVisibility(View.VISIBLE);

        subcategoryAdapter = new SubcategoryAdapter(getActivity(), activeSubcategories);

        gridView = (StaggeredGridView) parentView.findViewById(R.id.subcategory_grid);
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
        filter.addAction(INTENT_SUBCATEGORY);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(INTENT_SUBCATEGORY_ACTIVE_START)) {
                gridView.setVisibility(View.INVISIBLE);
                preloader.setVisibility(View.VISIBLE);
            }
            if (intent.hasExtra(INTENT_SUBCATEGORY_ACTIVE_END)) {
                preloader.setVisibility(View.INVISIBLE);
                gridView.setVisibility(View.VISIBLE);
            }
            if (intent.hasExtra(INTENT_SUBCATEGORY_ACTIVE)) {
                ArrayList<Subcategory> newSubcategories = intent.getParcelableArrayListExtra(INTENT_SUBCATEGORY_ACTIVE);
                if (Utils.isNotEmpty(newSubcategories)) {
                    activeSubcategories.clear();
                    activeSubcategories.addAll(newSubcategories);
                    subcategoryAdapter.notifyDataSetChanged();
                }
            }
        }
    };

}
