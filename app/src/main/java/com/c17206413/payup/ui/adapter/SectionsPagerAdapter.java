package com.c17206413.payup.ui.adapter;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.c17206413.payup.R;
import com.c17206413.payup.ui.main.DueFragment;
import com.c17206413.payup.ui.main.HistoryFragment;
import com.c17206413.payup.ui.main.IncomingFragment;

import org.jetbrains.annotations.NotNull;

//
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_due, R.string.tab_incoming, R.string.tab_history};
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @NotNull
    @Override
    //Set teh fragments for each tab
    public Fragment getItem(int pos) {
        switch (pos) {
            case 1:
                return IncomingFragment.newInstance();
            case 2:
                return HistoryFragment.newInstance();
            default:
                return DueFragment.newInstance();
        }
    }
    @Nullable
    @Override
    //title for each tab
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    //total tabs
    public int getCount() {
        return 3;
    }
}