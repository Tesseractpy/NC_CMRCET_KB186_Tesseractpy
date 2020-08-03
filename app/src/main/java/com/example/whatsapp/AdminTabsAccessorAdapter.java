package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class AdminTabsAccessorAdapter extends FragmentPagerAdapter
{

    public AdminTabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int i)
    {
        switch(i)
        {
            case 0:
                BlockedMessagesFragment blockedMessagesFragment = new BlockedMessagesFragment();
                return blockedMessagesFragment;

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 1;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {


        switch(position)
        {
            case 0:
                return "Blocked And Reported Messages";


            default:
                return null;
        }

    }
}
