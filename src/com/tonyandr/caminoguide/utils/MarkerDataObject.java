package com.tonyandr.caminoguide.utils;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Tony on 21-Feb-15.
 */
public class MarkerDataObject {
    public LatLng pointLocation;
    public String title;
    public String snippet;
    public int iconId;

    public MarkerDataObject(LatLng pointLocation, String title, String snippet, int iconId) {
        this.iconId = iconId;
        this.pointLocation = pointLocation;
        this.title = title;
        this.snippet = snippet;
    }
}
