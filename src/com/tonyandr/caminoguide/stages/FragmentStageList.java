package com.tonyandr.caminoguide.stages;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.tonyandr.caminoguide.R;
import com.tonyandr.caminoguide.constants.AppConstants;
import com.tonyandr.caminoguide.utils.GeoMethods;
import com.tonyandr.caminoguide.utils.JsonFilesHandler;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentStageList extends Fragment implements AppConstants {
    private SharedPreferences mPrefs;
    FragmentStageView fragmentView;
    FragmentManager fragmentManager;
    private JsonFilesHandler jfh;
    private ListView listView;
    private Parcelable mListState;
    private StagesAdapter adapter;

    //    private StagesAdapter adapter;
    public FragmentStageList() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stage_list, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.stage_activity_list_fragment_title);
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        jfh = new JsonFilesHandler(getActivity());
        mPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Construct the data source
        ArrayList<StageListItem> arrayOfStages = new ArrayList<StageListItem>();
        // Create the adapter to convert the array to views
        adapter = new StagesAdapter(getActivity(), arrayOfStages);
        // Attach the adapter to a ListView
        listView = (ListView) getActivity().findViewById(R.id.stageslistview);
        listView.setAdapter(adapter);

        fillListView();
        if (savedInstanceState != null) {
            listView.setSelectionFromTop(savedInstanceState.getInt("listIndex"), savedInstanceState.getInt("listTop"));
        }

        CurrentStage currentStage = new CurrentStage();
        currentStage.execute();
        fragmentManager = getFragmentManager();
        fragmentView = new FragmentStageView();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                TextView sn = (TextView) view.findViewById(R.id.tv_stagenumber);
                TextView ft = (TextView) view.findViewById(R.id.tv_from_to);
                int stageId = position + 1;
                String fromto = ft.getText().toString();
                final SharedPreferences.Editor edit = mPrefs.edit();
                edit.putInt(PREFS_STAGELIST_STAGEID, stageId);
                edit.putString(PREFS_STAGELIST_FROMTO, fromto);
                edit.commit();
                if (fragmentManager.findFragmentByTag("FragmentList") != null) {
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.replace(R.id.stage_fragment_holder_id, fragmentView, "FragmentView");
                    transaction.addToBackStack(FragmentStageView.class.getName());
                    transaction.commit();
                }
            }
        });
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (listView != null) {
            int index = listView.getFirstVisiblePosition();
            View v = listView.getChildAt(0);
            int top = (v == null) ? 0 : (v.getTop() - listView.getPaddingTop());

            outState.putInt("listIndex", index);
            outState.putInt("listTop", top);
        }
    }

    class CurrentStage extends AsyncTask<Void, Void, Void> {
        private CurrentStage() {

        }

        private GeoMethods geoMethods;
        private int stage = 0;

        @Override
        protected void onPreExecute() {
            geoMethods = new GeoMethods(getActivity());
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Location gps;
                if (mPrefs != null) {
                    String[] loc_string = mPrefs.getString("location-string", "").split(",");
                    if (loc_string.length > 1) {
                        gps = new Location("");
                        gps.setLatitude(Double.parseDouble(loc_string[0]));
                        gps.setLongitude(Double.parseDouble(loc_string[1]));
                        gps.setTime(Long.parseLong(loc_string[2]));
                        stage = geoMethods.onWhichStageSimple(gps);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (stage != 0) {
                adapter.getItem(stage - 1).current = true;
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void fillListView() {
        String[] stageNames = getResources().getStringArray(R.array.stage_names);
        adapter = (StagesAdapter) listView.getAdapter();
        int i = 1;
        for (String item : stageNames) {
            switch (item) {
                case "Saint Jean Pied de Port - Roncesvalles":
                    adapter.add(new StageListItem(i, item, "Valcarlos", false, true));
                    break;
                case "Terradillos de los Templarios - El Burgo Ranero":
                    adapter.add(new StageListItem(i, item, "Terradillos de los Templarios - Calzadilla de los Hermanillos", false, true));
                    break;
                case "El Burgo Ranero - León":
                    adapter.add(new StageListItem(i, item, "Calzadilla de los Hermanillos - León", false, true));
                    break;
                case "León - San Martín del Camino":
                    adapter.add(new StageListItem(i, item, "León - Villar de Mazarife", false, true));
                    break;
                case "San Martín del Camino - Astorga":
                    adapter.add(new StageListItem(i, item, "Villar de Mazarife - Astorga", false, true));
                    break;
                default:
                    adapter.add(new StageListItem(i, item, "", false, false));
                    break;
            }
            i++;
        }
    }
}
