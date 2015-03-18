package com.tonyandr.caminoguide.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.tonyandr.caminoguide.R;
import com.tonyandr.caminoguide.constants.AppConstants;

import java.util.List;

import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;

public class MapManagerActivity extends ActionBarActivity implements AppConstants, MaterialTabListener {

    FragmentManager fragmentManager;
    List<Fragment> fragments;

    private ViewPager mPager;
    private MaterialTabHost mTabs;

    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_manager);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fragmentManager = getSupportFragmentManager();
        mPager = (ViewPager) findViewById(R.id.pager);
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(fragmentManager);
        mPager.setAdapter(pagerAdapter);
        mTabs = (MaterialTabHost) findViewById(R.id.materialTabHost);

        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            mTabs.addTab(
                    mTabs.newTab()
                            .setText(pagerAdapter.getPageTitle(i))
                            .setTabListener(this)
            );
        }




        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // when user do a swipe the selected tab change
                mTabs.setSelectedNavigationItem(position);
                fragments = fragmentManager.getFragments();
//                Log.w(DEBUGTAG, "frags list size: " +fragments.size());
//                for (Fragment item:fragments) {
//                    if (item instanceof MapDownloadFragment) {
//                        ((MapDownloadFragment) item).displayListView();
//                                ((MapDownloadFragment) item).dataAdapter.notifyDataSetChanged();
//                    }
//                    if (item instanceof MapDeleteFragment) {
//                        ((MapDeleteFragment) item).displayListView();
//                                ((MapDeleteFragment) item).dataAdapter.notifyDataSetChanged();
//                    }
//                }
            }
        });


    }

    @Override
    public void onTabSelected(MaterialTab materialTab) {
        mPager.setCurrentItem(materialTab.getPosition());

    }

    @Override
    public void onTabReselected(MaterialTab materialTab) {

    }

    @Override
    public void onTabUnselected(MaterialTab materialTab) {

    }


    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch(position) {
                case 0: fragment = MapDownloadFragment.newInstance(); break;
                case 1: fragment =  MapDeleteFragment.newInstance(); break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String[] tabs = {"DOWNLOAD","DELETE"};
            return tabs[position];
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map_manager, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
