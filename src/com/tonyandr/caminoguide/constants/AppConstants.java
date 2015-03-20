// Created by plusminus on 23:11:31 - 22.09.2008
package com.tonyandr.caminoguide.constants;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.osmdroid.util.BoundingBoxE6;

/**
 *
 * This class contains constants used by the sample applications.
 *
 * @author Nicolas Gramlich
 *
 */
public interface AppConstants {
	// ===========================================================
	// Final Fields
	// ===========================================================

	public static final String DEBUGTAG = "APPDEBUGTAG";

	public static final boolean DEBUGMODE = false;

	public static final int NOT_SET = Integer.MIN_VALUE;

	public static final String PREFS_TILE_SOURCE = "tilesource";
    public static final String PREFS_SCROLL_X = "scrollX";
    public static final String PREFS_SCROLL_Y = "scrollY";
    public static final String PREFS_ZOOM_LEVEL = "zoomLevel";
    public static final String PREFS_NAME = "com.tonyandr.camino.prefs";
    public static final String PREFS_SHOW_LOCATION = "showLocation";
	public static final String PREFS_SHOW_COMPASS = "showCompass";
	public static final String PREFS_STOP_FOLLOW_GESTURES = "key_stop_follow";


	public static final String PREFS_STAGELIST_STAGEID = "stagelistStageId";
	public static final String PREFS_STAGELIST_FROMTO = "stagelistFromTo";

    public static final BoundingBoxE6 areaLimitSpain = new BoundingBoxE6(43.78, -0.54, 41.0, -9.338);
    public static final LatLngBounds areaLimitSpainGMap = new LatLngBounds(new LatLng(41.0, -9.338),new LatLng(43.78, -0.54));



    public static final String KEY_USER_LEARNED_DRAWER = "user_learned_drawer";
    public static final String KEY_USER_LEARNED_APP = "user_learned_app";


    public final static String KEY_KM_DONE = "key-km-done";

    public final static String LOCATION_KEY = "location-key";
    public final static String LOCATION_KEY_LAT = "location-key-lat";
    public final static String LOCATION_KEY_LNG = "location-key-lng";
    public final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    static final String KEY_CURRENT_LOCATION = "mCurrentLocation";
    static final String KEY_LAST_UPD_TIME = "mLastUpdateTime";
    static final String KEY_SERVICE_ACTION = "GeoService";
    static final String KEY_FOLLOW_USER = "mFollowUserLocation";
    static final String KEY_FIRST_CAMERA_MOVE = "fisrtCameraMove";
    static final int TRACK_ZOOM_LEVEL = 14;
    static final int FIRST_SHOW_ZOOM_LEVEL = 10;
    static final int SHOW_MARKERS_ZOOM_LEVEL = 14;
    static final int SHOW_STAGE_ZOOM_LEVEL = 12;
    static final int MIN_ZOOM_LEVEL = 8;
    static final int MAX_ZOOM_LEVEL = 18;


    final static String ACTION_QUEUE_RECEIVER = "action_queue_reciever";

    static final int FEEDBACK_STATUS_SENT = 1;
    static final int FEEDBACK_STATUS_WAIT = 0;


    static final String APP_PATH = "/CaminoGuideOffline/";
    static final String APP_OFFLINE_MAP_FILE = "caminofrances.map";

     static final String OSM_FRAGMENT_TAG = "com.tonyandr.caminoguide.osm_tag";
     static final String MF_FRAGMENT_TAG = "com.tonyandr.caminoguide.mf_tag";
     static final String GMS_FRAGMENT_TAG = "com.tonyandr.caminoguide.gms_tag";
	// ===========================================================
	// Methods
	// ===========================================================
}
