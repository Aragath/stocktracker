package com.example.csci571.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.csci571.fragments.HistoryFragment;
import com.example.csci571.fragments.HourlyFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private String ticker;
    private String color;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, String ticker, String color){
        super(fragmentActivity);
        this.ticker = ticker;
        this.color = color;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return HourlyFragment.newInstance(ticker, color);
            case 1:
                return HistoryFragment.newInstance(ticker);
            default:
                return new Fragment(); // default fragment
        }
    }
    @Override
    public int getItemCount() {
        return 2; // # of tabs
    }
}
