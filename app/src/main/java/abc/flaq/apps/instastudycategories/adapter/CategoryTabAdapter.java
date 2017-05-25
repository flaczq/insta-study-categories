package abc.flaq.apps.instastudycategories.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import abc.flaq.apps.instastudycategories.R;
import abc.flaq.apps.instastudycategories.fragment.CategoryActiveFragment;
import abc.flaq.apps.instastudycategories.fragment.CategoryInactiveFragment;

public class CategoryTabAdapter extends FragmentPagerAdapter {

    private Context context;

    public CategoryTabAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
            default:
                return CategoryActiveFragment.newInstance(position);
            case 1:
                return CategoryInactiveFragment.newInstance(position);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
            default:
                return context.getString(R.string.tab_active);
            case 1:
                return context.getString(R.string.tab_inactive);
        }
    }
}
