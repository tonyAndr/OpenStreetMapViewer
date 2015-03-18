package com.tonyandr.caminoguide.settings;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.tonyandr.caminoguide.constants.AppConstants;
import com.tonyandr.caminoguide.utils.ParcebleDownloadQueue;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Tony on 03-Mar-15.
 */
public class MoveService extends Service implements AppConstants{
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    static final String path_osmdroid = Environment.getExternalStorageDirectory().getPath() + "/osmdroid/";
    private BroadcastReceiver receiverDownloadComplete;
    private BroadcastReceiver receiverNotificationClicked;
    private BroadcastReceiver receiverQueueAdd;
    private ArrayList<ParcebleDownloadQueue> queue = new ArrayList<>();
    static DownloadManager downloadManager;



    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null)
            queue = intent.getParcelableArrayListExtra("download_list");

        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        registerQueueReceiver();
        registerDownloadReceiver();
        registerNotificationReceiever();

        return super.onStartCommand(intent, flags, startId);
    }

    private void registerNotificationReceiever () {

//        filter for notifications - only acts on notification
//              while download busy
        IntentFilter filter = new IntentFilter(DownloadManager
                .ACTION_NOTIFICATION_CLICKED);

        receiverNotificationClicked = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String extraId = DownloadManager
                        .EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS;
                long[] references = intent.getLongArrayExtra(extraId);
//                for (long reference : references) {
//                    if (reference == myDownloadReference) {
////                        do something with the download file
//                    }
//                }

//                List<PackageInfo>  list = getPackageManager().getInstalledPackages(PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
//                for (PackageInfo it:list) {
//                    Log.w(DEBUGTAG, it.packageName);
//                }
//                    Intent i = new Intent("com.android.providers.downloads");
//                    startActivity(i);

            }
        };
        registerReceiver(receiverNotificationClicked, filter);

    }

    private void registerDownloadReceiver() {
        IntentFilter intentFilter = new IntentFilter(DownloadManager
                .ACTION_DOWNLOAD_COMPLETE);

        receiverDownloadComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long reference = intent.getLongExtra(DownloadManager
                        .EXTRA_DOWNLOAD_ID, -1);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(reference);
                Cursor cursor = downloadManager.query(query);
                if (cursor.moveToFirst()) {
                    int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
                    String savedFilePath = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                    String fileDesc = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));

                    switch (status) {
                        case DownloadManager.STATUS_SUCCESSFUL:
                            Toast.makeText(context,
                                    fileDesc + " download complete",
                                    Toast.LENGTH_SHORT).show();
                            Log.d("Download", "Downloaded " + savedFilePath);

                            File from = new File(savedFilePath);
                            File to = new File(path_osmdroid + from.getName());
                            from.renameTo(to);

                            sendBroadcastToMaplist(fileDesc, true);
                            destroyService(fileDesc);

                            break;
                        case DownloadManager.STATUS_FAILED:
                            Toast.makeText(context, "Unable to download: " + fileDesc, Toast.LENGTH_LONG).show();
                            break;
                        case DownloadManager.STATUS_PAUSED:
                            Toast.makeText(context,"PAUSED: " + reason,Toast.LENGTH_LONG).show();
                            break;
                        case DownloadManager.STATUS_PENDING:
                            Toast.makeText(context,"PENDING!",Toast.LENGTH_LONG).show();
                            break;
                        case DownloadManager.STATUS_RUNNING:
                            Toast.makeText(context,"RUNNING!",Toast.LENGTH_LONG).show();
                            break;
                    }
                } else {
                    Toast.makeText(context, "Download failed", Toast.LENGTH_LONG).show();
                    for(ParcebleDownloadQueue item:queue) {
                        if (item.getLongValue() == reference) {
                            sendBroadcastToMaplist(item.getStrValue(), false);
                        }
                    }
                    destroyService(reference);
                }
                cursor.close();
            }
        };
        registerReceiver(receiverDownloadComplete, intentFilter);
    }

    private void sendBroadcastToMaplist(String title, boolean success) {
        Intent intent = new Intent();
        intent.setAction("action_title_change_maplist");
        intent.putExtra("title_for_maplist", title);
        intent.putExtra("action_for_maplist", success);
        sendBroadcast(intent);
    }

    private void registerQueueReceiver() {
        IntentFilter intentFilter = new IntentFilter(ACTION_QUEUE_RECEIVER);

        receiverQueueAdd = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                queue.add((ParcebleDownloadQueue)intent.getParcelableExtra("download_item"));
//                Log.w(DEBUGTAG, "Item received: " + intent.getStringExtra("download_item_title"));
            }
        };

        registerReceiver(receiverQueueAdd, intentFilter);
        Log.w(DEBUGTAG, "Queue receiver registered");
    }

    private void destroyService(String fileDesc) {
        ParcebleDownloadQueue toDel = new ParcebleDownloadQueue();
        if (queue.size() > 0) {
            for (ParcebleDownloadQueue item : queue) {
                if (item.getStrValue().equals(fileDesc)) {
                    toDel = item;
                }
            }
            queue.remove(toDel);
            if (queue.size() == 0) {
                stopSelf();
            }
        } else {
            stopSelf();
        }
    }

    private void destroyService(long ref) {
        ParcebleDownloadQueue toDel = new ParcebleDownloadQueue();
        if (queue.size() > 0) {
            for (ParcebleDownloadQueue item : queue) {
                if (item.getLongValue() == ref) {
                    toDel = item;

                }
            }
            queue.remove(toDel);
            if (queue.size() == 0) {
                stopSelf();
            }
        } else {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(DEBUGTAG, "receivers unregistered ondestroy");
        unregisterReceiver(receiverQueueAdd);
        unregisterReceiver(receiverDownloadComplete);
        unregisterReceiver(receiverNotificationClicked);
    }
}
