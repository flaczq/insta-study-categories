package abc.flaq.apps.instastudycategories.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import abc.flaq.apps.instastudycategories.fragment.CategoryActiveFragment;
import abc.flaq.apps.instastudycategories.fragment.CategoryInactiveFragment;

public class CategoryTabAdapter extends FragmentPagerAdapter {

    public CategoryTabAdapter(FragmentManager fm) {
        super(fm);
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
                return "AKTYWNE";
            case 1:
                return "NIEAKTYWNE";
        }
    }
}
