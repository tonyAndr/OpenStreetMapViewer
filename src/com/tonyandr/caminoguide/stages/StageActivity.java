package com.tonyandr.caminoguide.stages;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.tonyandr.caminoguide.NavigationDrawerLayout;
import com.tonyandr.caminoguide.R;
import com.tonyandr.caminoguide.map.GeoService;


public class StageActivity extends ActionBarActivity implements FragmentManager.OnBackStackChangedListener {

    FragmentStageList fragmentList;
    FragmentStageView fragmentView;
    FragmentManager fragmentManager;
    private Toolbar toolbar;
    private NavigationDrawerLayout drawerFragment;
    public TextView geoOutTextViewLon;
    public TextView geoOutTextViewLat;
    public TextView geoOutTextViewTime;
    public int backstackCount;
    public Location transferMyLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stage);
        fragmentManager = getFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this);
        if (fragmentManager.findFragmentByTag("FragmentList") == null) {
            fragmentList = new FragmentStageList();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.stage_fragment_holder_id, fragmentList, "FragmentList");
//                    transaction.addToBackStack(FragmentStageList.class.getName());
                            transaction.commit();
//            fragmentList.setCommunicater(this);
        }

        geoOutTextViewLon = (TextView) findViewById(R.id.debugGeoOutputLon);
        geoOutTextViewLat = (TextView) findViewById(R.id.debugGeoOutputLat);
        geoOutTextViewTime = (TextView) findViewById(R.id.debugGeoOutputTime);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        drawerFragment = (NavigationDrawerLayout) getSupportFragmentManager().findFragmentById(R.id.fragment_nav_drawer);
        drawerFragment.setUp(R.id.fragment_nav_drawer,(DrawerLayout)findViewById(R.id.drawer_layout), toolbar);

//        getFragmentManager().addOnBackStackChangedListener(this);

    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
////        getMenuInflater().inflate(R.menu.menu_map, menu);
//
//        return true;
//    }
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == android.R.id.home) {
//            onBackPressed();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }


    @Override
    protected void onResume() {
        super.onResume();
        onBackStackChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopService(new Intent(this, GeoService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, GeoService.class));
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() == 0) {
            this.finish();
        } else {
            fragmentManager.popBackStack();
        }
    }

    @Override
    public void onBackStackChanged() {
        backstackCount = fragmentManager.getBackStackEntryCount();
        if (backstackCount == 0) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        drawerFragment.setActionBarArrowDependingOnFragmentsBackStack(backstackCount);
        if (backstackCount != 0) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }

}
