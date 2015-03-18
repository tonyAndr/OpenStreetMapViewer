package com.tonyandr.caminoguide;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tonyandr.caminoguide.constants.AppConstants;
import com.tonyandr.caminoguide.feedback.AboutActivity;
import com.tonyandr.caminoguide.feedback.FeedbackActivity;
import com.tonyandr.caminoguide.map.MapActivity;
import com.tonyandr.caminoguide.settings.SettingsActivity;
import com.tonyandr.caminoguide.stages.StageActivity;
import com.tonyandr.caminoguide.utils.DrawRecycleAdapter;
import com.tonyandr.caminoguide.utils.DrawRecycleInformation;

import java.util.ArrayList;
import java.util.List;

//import android.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class NavigationDrawerLayout extends Fragment implements DrawRecycleAdapter.ClickListener, AppConstants{

    private RecyclerView recyclerView;
    public ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;
    private View containerView;
    private DrawRecycleAdapter adapter;
    private boolean isDrawerOpened = false;
    private FragmentManager fragmentManager;



//    public static final String PREF_FILE_NAME = "draw_pref";

    public NavigationDrawerLayout() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserLearnedDrawer = Boolean.valueOf(readFromPreferences(getActivity(), KEY_USER_LEARNED_DRAWER, "false"));
        if (savedInstanceState != null) {
            mFromSavedInstanceState = true;
        }
//        fragmentManager = getFragmentManager();
//        fragmentManager.addOnBackStackChangedListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_navigation_drawer_layout, container, false);
        recyclerView = (RecyclerView) layout.findViewById(R.id.drawerList);
        adapter = new DrawRecycleAdapter(getActivity(), getData());
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return layout;
    }
    public List<DrawRecycleInformation> getData() {
        //load only static data inside a drawer
        List<DrawRecycleInformation> data = new ArrayList<>();
//        int icon = R.drawable.list_circle;
        String[] titles = {"Map", "Stages", "Settings", "Feedback", "About"};
        int[] icons = {R.drawable.ic_nav_map, R.drawable.ic_nav_stages, R.drawable.ic_nav_settings, R.drawable.ic_nav_feedback, R.drawable.ic_nav_about};
        int active_id = 0;

        if (getActivity() instanceof MapActivity)
            active_id = 0;
        if (getActivity() instanceof StageActivity)
            active_id = 1;
        if (getActivity() instanceof SettingsActivity)
            active_id = 2;
        if (getActivity() instanceof FeedbackActivity)
            active_id = 3;
//        if (getActivity() instanceof )

        for (int i = 0; i < titles.length; i++) {
            DrawRecycleInformation current = new DrawRecycleInformation();
            current.iconId = icons[i];
            current.title = titles[i];
            if (i == active_id)
                current.active = true;
            data.add(current);
        }
        return data;
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout, final Toolbar toolbar) {
        containerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    saveToPreferences(getActivity(), KEY_USER_LEARNED_DRAWER, mUserLearnedDrawer + "");
                }
                getActivity().invalidateOptionsMenu();

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                toolbar.setAlpha(1 - slideOffset / 2);
            }
        };
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(containerView);
        }

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
    }

    public static void saveToPreferences(Context context, String preferenceName, String preferenceValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferenceName, preferenceValue);
        editor.apply();
    }

    public static String readFromPreferences(Context context, String preferenceName, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceName, defaultValue);
    }

    @Override
    public void itemClicked(View view, int position) {
        mDrawerLayout.closeDrawer(containerView);
        switch (position) {
            case 0:
                startActivity(new Intent(getActivity(), MapActivity.class));
                break;
            case 1:
                startActivity(new Intent(getActivity(), StageActivity.class));
                break;
            case 2:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                break;
            case 3:
                startActivity(new Intent(getActivity(), FeedbackActivity.class));
                break;
            case 4:
                startActivity(new Intent(getActivity(), AboutActivity.class));
                break;
            default:
                break;
        }
    }

    public void setActionBarArrowDependingOnFragmentsBackStack(int backStackCount) {
        if (backStackCount != 0) {
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(getActivity(), "CLICK", Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed();
                }
            });
        } else {
            mDrawerToggle.setDrawerIndicatorEnabled(true);
        }

//        Toast.makeText(getActivity(), "back " + backStackCount, Toast.LENGTH_SHORT).show();
    }

}
