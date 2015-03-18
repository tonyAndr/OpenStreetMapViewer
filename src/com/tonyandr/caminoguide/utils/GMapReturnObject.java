package com.tonyandr.caminoguide.utils;

import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by Tony on 21-Feb-15.
 */
public class GMapReturnObject {
    public double length;
    public PolylineOptions polylineOptions;
    public GMapReturnObject(double length, PolylineOptions polylineOptions) {
        this.length = length;
        this.polylineOptions = polylineOptions;
    }
}
