// Created by plusminus on 23:11:31 - 22.09.2008
package com.tonyandr.caminoguideoff.constants;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;


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

    public static final String PREFS_NAME = "com.tonyandr.camino.prefs";

	public static final String PREFS_STAGELIST_STAGEID = "stagelistStageId";
	public static final String PREFS_STAGELIST_FROMTO = "stagelistFromTo";

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

    static final int FEEDBACK_STATUS_SENT = 1;
    static final int FEEDBACK_STATUS_WAIT = 0;


    static final String APP_PATH = "/CaminoGuideOffline/";
    static final String APP_OFFLINE_MAP_FILE = "caminofrances.map";

     static final String MF_FRAGMENT_TAG = "com.tonyandr.caminoguide.mf_tag";
     static final String GMS_FRAGMENT_TAG = "com.tonyandr.caminoguide.gms_tag";
     static final String GMS_FRAGMENT_MAPTYPE_TAG = "com.tonyandr.caminoguide.gms_maptype_tag";


     static final String MAPTYPE_PREF = "map-type-pref";
     static final int MAPTYPE_PREF_HYBRID = 0;
     static final int MAPTYPE_PREF_NORMAL = 1;


    static final String KEY_LICENSE = "license-key";
    // ===========================================================
	// Methods
	// ===========================================================
}
