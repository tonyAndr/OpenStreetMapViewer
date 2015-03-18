package com.tonyandr.caminoguide.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.tonyandr.caminoguide.NavigationDrawerLayout;
import com.tonyandr.caminoguide.R;
import com.tonyandr.caminoguide.constants.AppConstants;

import java.io.File;


public class SettingsActivity extends ActionBarActivity implements AppConstants, FragmentManager.OnBackStackChangedListener {

    private Toolbar toolbar;
    private FragmentManager fragmentManager;
    private int backstackCount;
    private NavigationDrawerLayout drawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        fragmentManager = getFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this);

        drawerFragment = (NavigationDrawerLayout) getSupportFragmentManager().findFragmentById(R.id.fragment_nav_drawer);
        drawerFragment.setUp(R.id.fragment_nav_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);

        fragmentManager.beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
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


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class SettingsFragment extends PreferenceFragment {

        public SettingsFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.settings);

            Preference dl_pref = findPreference("dl_key");
            dl_pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
//                    getActivity().startActivity(new Intent(getActivity(), MapManagerActivity.class));
//                    Toast.makeText(getActivity(), "Starting download process...", Toast.LENGTH_SHORT).show();
                    notMapFileDialog("Download map");
                    return true;
                }
            });
//            final CheckBoxPreference offline_pref = (CheckBoxPreference) findPreference("pref_key_offline_mode");
//            offline_pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//                @Override
//                public boolean onPreferenceClick(Preference preference) {
//                    if (!(new File(Environment.getExternalStorageDirectory().getPath() + "/osmdroid/map_overview.zip")).exists()) {
//                        ((CheckBoxPreference)preference).setChecked(false);
//                        checkMaps();
//                    }
//                    return true;
//                }
//            });

            Preference choose_map = findPreference("pref_key_choose_map");
            choose_map.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.equals("2")) {
                        File f = new File(Environment.getExternalStorageDirectory().getPath() + APP_PATH + APP_OFFLINE_MAP_FILE);
                        if (!f.exists()) {
                            notMapFileDialog("Offline map not found");
                        }
                    }
                    return true;
                }
            });
        }


        private void notMapFileDialog(String header) {
            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(header);
            builder.setMessage("Do you want download it now? (24mb)");
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog
                    Intent intent = new Intent(getActivity(), MapManagerActivity.class);
                    startActivity(intent);
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }


}
