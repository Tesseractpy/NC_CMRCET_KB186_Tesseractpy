package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ForwardTabsAccessorAdapter extends FragmentPagerAdapter
{

    public ForwardTabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int i)
    {
        switch(i)
        {
            case 0:
                ForwardFragment forwardFragment = new ForwardFragment();
                return forwardFragment;

            default:
                return null;
        }

    }

    @Override
    public int getCount()
    {
        return 1;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position)
    {
        switch(position)
        {
            case 0:
                return "Contacts";

            default:
                return null;
        }
    }
}
