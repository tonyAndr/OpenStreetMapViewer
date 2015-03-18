package com.tonyandr.caminoguide.settings;


import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.tonyandr.caminoguide.R;
import com.tonyandr.caminoguide.constants.AppConstants;
import com.tonyandr.caminoguide.utils.ParcebleDownloadQueue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//import android.app.Fragment;
//import android.app.FragmentTransaction;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapDownloadFragment extends Fragment implements AppConstants{
    FragmentManager fragmentManager;
    public MaplistAdapter dataAdapter = null;
    private String[] stageNames;
    private List<String> mapsUrls;
    private String mapsUrl = ""; // TODO
    static final String path_osmdroid = Environment.getExternalStorageDirectory().getPath() + "/osmdroid/";
    static DownloadManager downloadManager;
    private BroadcastReceiver receiverDownloadComplete;
//    BroadcastReceiver receiverNotificationClicked;
    static long myDownloadReference;
    private ArrayList<MaplistInfo> mapList;
    private ListView listView;

    static final int LIST_ITEM_STATUS_NONE = 0;
    static final int LIST_ITEM_STATUS_INPROGRESS = 1;
    static final int LIST_ITEM_STATUS_EXIST = 2;


    public MapDownloadFragment() {
        // Required empty public constructor
    }
    public static MapDownloadFragment newInstance() {
        MapDownloadFragment f = new MapDownloadFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        createBroadcastReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map_download, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        displayListView();
        stageNames = getResources().getStringArray(R.array.stage_names);
        downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        fragmentManager = getActivity().getSupportFragmentManager();
    }

    public long mapDownload(String title, int position) {
        File f = new File(path_osmdroid);
        if (!f.exists()) {
            f.mkdirs();
        }
        f = new File(path_osmdroid + "/temp/");
        if (!f.exists()) {
            f.mkdirs();
        }
//        mapsUrls.add("http://alberguenajera.es/projects/ecn/8-11_allmap.zip");
//        for (int i = 0; i < stageNames.length; i++) {
        int i = 0;
        mapsUrl = null;
        if (mapList.get(position).getStatus() == LIST_ITEM_STATUS_NONE) {
            while (mapsUrl == null && i < 31) {
//                Log.d("Download", "Compare " + stageNames[i] + " and " + title);
                if (stageNames[i].equals(title)) {
                    int stage_id = i + 1;
                    if (!(new File(path_osmdroid + "stage" + stage_id + ".zip")).exists() && !(new File(path_osmdroid + "temp/stage" + stage_id + ".zip")).exists()) {
                        mapsUrl = "http://alberguenajera.es/projects/ecn/stage" + stage_id + ".zip";
                    }
                } else if (title.equals("Spain Base Map")) {
                    if (!(new File(path_osmdroid + "map_overview.zip")).exists() && !(new File(path_osmdroid + "temp/map_overview.zip")).exists()) {
                        mapsUrl = "http://alberguenajera.es/projects/ecn/map_overview.zip";
                    }
                } else {
//                mapsUrl = null;
//                    Log.d("Download", "mapUrl still is null");
                }
                i++;
            }
        }
//        }
        if (mapsUrl != null) {
            mapList.get(position).setStatus(LIST_ITEM_STATUS_INPROGRESS);
            if (listView.getChildAt(position) != null) {
                (((ViewGroup) listView.getChildAt(position)).getChildAt(0)).setVisibility(View.GONE);
                (((ViewGroup) listView.getChildAt(position)).getChildAt(1)).setVisibility(View.VISIBLE);
            }
            dataAdapter.notifyDataSetChanged();
//            Log.d("Download", "Url to load: " + mapsUrl);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mapsUrl));
            request.setTitle(title);
            request.setDescription("Download in progress...");
//        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI, DownloadManager.Request.NETWORK_MOBILE);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setVisibleInDownloadsUi(true);
            String fileName = URLUtil.guessFileName(mapsUrl, null, MimeTypeMap.getFileExtensionFromUrl(mapsUrl));
            request.setDestinationInExternalPublicDir("/osmdroid/temp/", fileName);
            myDownloadReference = downloadManager.enqueue(request);
            Log.d(DEBUGTAG, "Added to queue " + title);
            Log.d(DEBUGTAG, "Added to queue ref " + myDownloadReference);
        } else {
            Toast.makeText(getActivity(), "Choose any items", Toast.LENGTH_LONG).show();
            return -1;
        }

        return myDownloadReference;
    }

    private void checkButtonClick() {
        ArrayList<ParcebleDownloadQueue> queue = new ArrayList<>();
        for (int i = 0; i < mapList.size(); i++) {
            MaplistInfo maplistInfo = mapList.get(i);
            if (maplistInfo.isSelected()) {
//                        responseText.append("\n" + maplistInfo.getTitle());
                String title = maplistInfo.getTitle();
                long reference = mapDownload(title, i);
                ParcebleDownloadQueue p = new ParcebleDownloadQueue();
                p.setLongValue(reference);
                p.setStrValue(title);
                queue.add(p);

                Intent intent = new Intent();
                intent.setAction(ACTION_QUEUE_RECEIVER);
                intent.putExtra("download_item", p);
                getActivity().sendBroadcast(intent);
                maplistInfo.setSelected(false);
                dataAdapter.notifyDataSetChanged();
            }
        }

        if (!isMyServiceRunning(MoveService.class)) {
            Intent intent = new Intent(getActivity(),MoveService.class);
            intent.putExtra("download_list", queue);
            getActivity().startService(intent);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                //your service is running
                Log.w(DEBUGTAG, "Service already running!");
                return true;
            }
        }
        return false;
    }


    private void createBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter("action_title_change_maplist");
        receiverDownloadComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String title = intent.getStringExtra("title_for_maplist");
                boolean action = intent.getBooleanExtra("action_for_maplist", false);

                for (int i = 0; i < mapList.size(); i++) {
                    if (action) {
                        if (((mapList.get(i)).getTitle()).equals(title)) {
                            mapList.get(i).setStatus(LIST_ITEM_STATUS_EXIST);
                            mapList.remove(i);
                            dataAdapter.notifyDataSetChanged();
                        }
                    } else {
                        if (((mapList.get(i)).getTitle()).equals(title)) {
                            mapList.get(i).setSelected(true);
                            mapList.get(i).setStatus(LIST_ITEM_STATUS_NONE);
                            if (listView.getChildAt(i) != null) {
                                (((ViewGroup) listView.getChildAt(i)).getChildAt(0)).setVisibility(View.VISIBLE);
                                (((ViewGroup) listView.getChildAt(i)).getChildAt(1)).setVisibility(View.GONE);
                            }
                            dataAdapter.notifyDataSetChanged();
                        }
                    }
                }
                for (Fragment item:fragmentManager.getFragments()) {
                    if (item instanceof MapDeleteFragment) {
                        ((MapDeleteFragment) item).displayListView();
                        ((MapDeleteFragment) item).dataAdapter.notifyDataSetChanged();
                    }
                }
            }
        };

        getActivity().registerReceiver(receiverDownloadComplete, intentFilter);
    }


    public void displayListView() {

        //Array list of countries
        mapList = new ArrayList<>();
        stageNames = getResources().getStringArray(R.array.stage_names);
        File f = new File(path_osmdroid + "map_overview.zip");
        if (!f.exists()) {
            mapList.add(new MaplistInfo(32+"mb", "Spain Base Map", true, false));
        }
        int count = 1;
        for (String item : stageNames) {
            f = new File(path_osmdroid + "stage" + count + ".zip");
            if (!f.exists()) {
                mapList.add(new MaplistInfo(15+"mb", item, false, true));
            }
            count++;
        }


        //create an ArrayAdaptar from the String Array
        dataAdapter = new MaplistAdapter(getActivity(), mapList);
        listView = (ListView) getActivity().findViewById(R.id.lv_dload);
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
//                Toast.makeText(getActivity(),
//                        "Clicked on Row #" + position + " with: " + maplistInfo.getTitle(),
//                        Toast.LENGTH_SHORT).show();

            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();
        Log.w(DEBUGTAG, "Dload fr resumed");
//        filter for notifications - only acts on notification
//              while download busy
//        IntentFilter filter = new IntentFilter(DownloadManager
//                .ACTION_NOTIFICATION_CLICKED);
//
//        receiverNotificationClicked = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String extraId = DownloadManager
//                        .EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS;
//                long[] references = intent.getLongArrayExtra(extraId);
//                for (long reference : references) {
//                    if (reference == myDownloadReference) {
////                        do something with the download file
//                    }
//                }
//            }
//        };
//        registerReceiver(receiverNotificationClicked, filter);

//        filter for download - on completion
//        IntentFilter intentFilter = new IntentFilter(DownloadManager
//                .ACTION_DOWNLOAD_COMPLETE);
//
//        receiverDownloadComplete = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                long reference = intent.getLongExtra(DownloadManager
//                        .EXTRA_DOWNLOAD_ID, -1);
////                if (myDownloadReference == reference) {
////                    do something with the download file
//                DownloadManager.Query query = new DownloadManager.Query();
//                query.setFilterById(reference);
//                Cursor cursor = downloadManager.query(query);
//
//                cursor.moveToFirst();
////                        get the status of the download
//                int columnIndex = cursor.getColumnIndex(DownloadManager
//                        .COLUMN_STATUS);
//                int status = cursor.getInt(columnIndex);
//
//                int fileNameIndex = cursor.getColumnIndex(DownloadManager
//                        .COLUMN_LOCAL_FILENAME);
//                String savedFilePath = cursor.getString(fileNameIndex);
//                String fileDesc = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
////                        get the reason - more detail on the status
//                int columnReason = cursor.getColumnIndex(DownloadManager
//                        .COLUMN_REASON);
//                int reason = cursor.getInt(columnReason);
//
//                switch (status) {
//                    case DownloadManager.STATUS_SUCCESSFUL:
//                        Toast.makeText(context,
//                                fileDesc + " download complete",
//                                Toast.LENGTH_SHORT).show();
//                        Log.d(DEBUGTAG, "Downloaded " + savedFilePath);
//                        for (int i = 0; i < mapList.size(); i++) {
//                            if (((mapList.get(i)).getTitle()).equals(fileDesc)) {
//                                mapList.get(i).setStatus(LIST_ITEM_STATUS_EXIST);
//                                mapList.remove(i);
//                                dataAdapter.notifyDataSetChanged();
//                            }
//                        }
////                        new UnzipTask().execute(savedFilePath);
////                        File from = new File(savedFilePath);
////                        File to = new File(path_osmdroid + from.getName());
////                        from.renameTo(to);
//                        break;
//                    case DownloadManager.STATUS_FAILED:
//                        Toast.makeText(context,
//                                "Unable to download: " + fileDesc,
//                                Toast.LENGTH_LONG).show();
//                        for (int i = 0; i < mapList.size(); i++) {
//                            if (((mapList.get(i)).getTitle()).equals(fileDesc)) {
//                                mapList.get(i).setStatus(LIST_ITEM_STATUS_NONE);
//                                (((ViewGroup) listView.getChildAt(i)).getChildAt(0)).setVisibility(View.VISIBLE);
//                                (((ViewGroup) listView.getChildAt(i)).getChildAt(1)).setVisibility(View.GONE);
//                                dataAdapter.notifyDataSetChanged();
//                            }
//                        }
//                        break;
//                    case DownloadManager.STATUS_PAUSED:
//                        Toast.makeText(context,
//                                "PAUSED: " + reason,
//                                Toast.LENGTH_LONG).show();
//                        break;
//                    case DownloadManager.STATUS_PENDING:
//                        Toast.makeText(context,
//                                "PENDING!",
//                                Toast.LENGTH_LONG).show();
//                        break;
//                    case DownloadManager.STATUS_RUNNING:
//                        Toast.makeText(context,
//                                "RUNNING!",
//                                Toast.LENGTH_LONG).show();
//                        break;
//                }
//                cursor.close();
////                }
//            }
//        };
//
//        getActivity().registerReceiver(receiverDownloadComplete, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
//        getActivity().unregisterReceiver(receiverDownloadComplete);
//            unregisterReceiver(receiverNotificationClicked);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(receiverDownloadComplete);
    }

    private void deleteFromList(String title) {
        ArrayList<MaplistInfo> mapList = dataAdapter.mapList;
        for (int i = 0; i < mapList.size(); i++) {
            MaplistInfo maplistInfo = mapList.get(i);
            if (title.equals(maplistInfo.getTitle())) {
                mapList.remove(i);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_mapfragment_download, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_download_map) {
            checkButtonClick();
            return true;
        }
//        if (id == R.id.action_delete_fragment) {
//            if (fragmentManager.findFragmentByTag("MapDownloadFragment") != null) {
//                fragmentDelete = new MapDeleteFragment();
//                FragmentTransaction transaction = fragmentManager.beginTransaction();
//                transaction.replace(R.id.mapmanager_fragment_holder_id, fragmentDelete, "MapDeleteFragment");
//                transaction.addToBackStack(MapDeleteFragment.class.getName());
//                transaction.commit();
//            }
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

}
