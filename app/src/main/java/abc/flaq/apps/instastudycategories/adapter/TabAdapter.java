package abc.flaq.apps.instastudycategories.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import abc.flaq.apps.instastudycategories.fragment.CategoryFragment;

public class TabAdapter extends FragmentPagerAdapter {

    public TabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
            default:
                return CategoryFragment.newInstance(position);  // FIXME: CategoryActiveFragment
            case 1:
                return CategoryFragment.newInstance(position);  // FIXME: CategoryInactiveFragment
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
                return "AKTYWNE";
            case 1:
                return "NIEAKTYWNE";
        }
    }
}
