package com.tonyandr.caminoguide.settings;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by Tony on 29-Jan-15.
 */
public class UnZipService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */

    public UnZipService() {

        super("UnZipService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        String dataString = workIntent.getDataString();
        Intent intent = new Intent();
        intent.setAction("org.osmdroid.settings.UnzipService");
        intent.putExtra("status", "Installing...");
        sendBroadcast(intent);

        intent.putExtra("status", "Done!");
        sendBroadcast(intent);
    }


}