package com.tonyandr.caminoguide.settings;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.tonyandr.caminoguide.R;
import com.tonyandr.caminoguide.constants.AppConstants;

import java.io.File;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapDeleteFragment extends Fragment implements AppConstants {
    FragmentManager fragmentManager;

    public MaplistAdapter dataAdapter = null;
    private String[] stageNames;
    static final String path_osmdroid = Environment.getExternalStorageDirectory().getPath() + "/osmdroid/";
    private ArrayList<MaplistInfo> mapList;
    private ListView listView;
    static final int LIST_ITEM_STATUS_NONE = 0;
    static final int LIST_ITEM_STATUS_INPROGRESS = 1;
    static final int LIST_ITEM_STATUS_EXIST = 2;

    public MapDeleteFragment() {
        // Required empty public constructor
    }
    public static MapDeleteFragment newInstance(){
        MapDeleteFragment f = new MapDeleteFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map_delete, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        displayListView();
        checkButtonClick();
        stageNames = getResources().getStringArray(R.array.stage_names);
        fragmentManager = getActivity().getSupportFragmentManager();
    }

    class DeleteTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            mapDelete(params[0]);

            return params[0];
        }

        @Override
        protected void onPostExecute(String aVoid) {
            deleteFromList(aVoid);
            for (Fragment item:fragmentManager.getFragments()) {
                if (item instanceof MapDownloadFragment) {
                    ((MapDownloadFragment) item).displayListView();
                    ((MapDownloadFragment) item).dataAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void checkButtonClick() {
        for (int i = 0; i < mapList.size(); i++) {
            MaplistInfo maplistInfo = mapList.get(i);
            if (maplistInfo.isSelected()) {
                mapList.get(i).setStatus(LIST_ITEM_STATUS_INPROGRESS);
                if (listView.getChildAt(i) != null) {
                    (((ViewGroup)listView.getChildAt(i)).getChildAt(0)).setVisibility(View.GONE);
                    (((ViewGroup)listView.getChildAt(i)).getChildAt(1)).setVisibility(View.VISIBLE);
                }
                dataAdapter.notifyDataSetChanged();
                new DeleteTask().execute(maplistInfo.getTitle());
            }
        }
    }
    private void deleteFromList(String title) {
        for (int i = 0; i < mapList.size(); i++) {
            MaplistInfo maplistInfo = mapList.get(i);
            if (title.equals(maplistInfo.getTitle())) {
                mapList.get(i).setStatus(LIST_ITEM_STATUS_NONE);
                mapList.remove(i);
                dataAdapter.notifyDataSetChanged();
            }
        }

    }

    private void DeleteFiles(File f) { // Delete *.zip

        if (f.exists()) {
            f.delete();
        }
        else {
//            Log.d("Delete", "not exists");
        }
    }
    private void mapDelete(String title) { // Delete selected stage's maps
        int count = 1;
        int stageId = 0;
        for (String item : stageNames) {
            if (item.equals(title)) {
                stageId = count;
            }
            count++;
        }

        if (stageId != 0) {
            File f = new File(path_osmdroid + "stage" + stageId + ".zip");
            DeleteFiles(f);
        }
        if (title.equals("Spain Base Map")) {
            File f = new File(path_osmdroid + "map_overview.zip");
            DeleteFiles(f);
        }

    }

    public void displayListView() {

        //Array list of countries
        mapList = new ArrayList<>();
        stageNames = getResources().getStringArray(R.array.stage_names);
        boolean stages_exist = false;

        File f = new File(path_osmdroid + "map_overview.zip");
        if (f.exists()) {
            mapList.add(new MaplistInfo(Math.round(((f.length()/1024)/1024))+"mb", "Spain Base Map", false, true));
        }

        int count = 1;
        for (String item : stageNames) {
            f = new File(path_osmdroid + "stage" + count + ".zip");
            if (f.exists()) {
                mapList.add(new MaplistInfo(Math.round(((f.length()/1024)/1024))+"mb", item, false, true));
            }
            count++;
        }


        //create an ArrayAdaptar from the String Array
        dataAdapter = new MaplistAdapter(getActivity(), mapList);
        listView = (ListView) getActivity().findViewById(R.id.lv_delete);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // When clicked, show a toast with the TextView text
                MaplistInfo maplistInfo = (MaplistInfo) parent.getItemAtPosition(position);
                CheckBox cb = (CheckBox) ((ViewGroup) view).getChildAt(0);
                if (!cb.isEnabled()) {
                    Toast.makeText(view.getContext(),
                            maplistInfo.title + " can't be excluded!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    cb.setChecked(!cb.isChecked());
                    maplistInfo.setSelected(cb.isChecked());
                }
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_mapfragment_delete, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_map) {
            checkButtonClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
