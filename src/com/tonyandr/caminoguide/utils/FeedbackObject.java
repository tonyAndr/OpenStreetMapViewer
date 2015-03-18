package com.tonyandr.caminoguide.utils;

/**
 * Created by Tony on 04-Mar-15.
 */
public class FeedbackObject {
    public long id;
    public String text;
    public double lat;
    public double lng;

    public FeedbackObject(long id, String text, double lat, double lng) {
        this.id = id;
        this.text = text;
        this.lat = lat;
        this.lng = lng;
    }
}
