package com.example.mahmoudfcih.simpleblogapp;



import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


/**
 * Created by mahmoud on 4/20/2017.
 */

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when
   // int icons[]={R.mipmap.ic_home_white_24dp,R.mipmap.ic_notifications_white_24dp,R.mipmap.ic_add_a_photo_white_24dp};

    public ViewPagerAdapter(FragmentManager fm,CharSequence mTitles[], int mNumbOfTabsumb) {
        super(fm);
        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;

    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0) // if the position is 0 we are returning the First tab
        {
            Tab1 tab1 = new Tab1();
            return tab1;
            } else if(position == 1) {
            Tab2 tab2 = new Tab2();
            return tab2;
            }else {
            Tab3 tab3=new Tab3();
            return tab3;
        }

    }
// This method return the titles for the Tabs in the Tab Strip
    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
/*
        Drawable drawable= ResourcesCompat.getDrawable(getResources(),icons[position],null);
        drawable.setBounds(0,0,60,60);
        ImageSpan imageSpan=new ImageSpan(drawable);
        SpannableString spannableString =new SpannableString(" ");
        spannableString.setSpan(imageSpan,0,spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;*/

        }
    @Override
    public int getCount() {
        return NumbOfTabs;
    }
}
