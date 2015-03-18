package com.tonyandr.caminoguide.feedback;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.tonyandr.caminoguide.NavigationDrawerLayout;
import com.tonyandr.caminoguide.R;

public class AboutActivity extends ActionBarActivity {
    private Toolbar toolbar;
    private NavigationDrawerLayout drawerFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        drawerFragment = (NavigationDrawerLayout) getSupportFragmentManager().findFragmentById(R.id.fragment_nav_drawer);
        drawerFragment.setUp(R.id.fragment_nav_drawer,(DrawerLayout)findViewById(R.id.drawer_layout), toolbar);
    }

}
