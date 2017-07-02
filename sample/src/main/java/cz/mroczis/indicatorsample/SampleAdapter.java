package cz.mroczis.indicatorsample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Simple adapter for view pager
 * Created by Michal on 29.06.17.
 */

public class SampleAdapter extends FragmentStatePagerAdapter {

    private static final int PAGES_COUNT = 8;

    public SampleAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return SampleFragment.getInstance(i + 1);
    }

    @Override
    public int getCount() {
        return PAGES_COUNT;
    }
}
