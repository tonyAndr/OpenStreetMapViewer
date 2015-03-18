package com.tonyandr.caminoguide.stages;

/**
 * Created by Tony on 10-Feb-15.
 */
public class StageViewAlbItem {
    public String title, tel, type, beds, locality;
    public boolean showSection = false;
    double lat, lng;
    int stage_id;

    public StageViewAlbItem(String title,String tel,String type,String beds, String locality, boolean showSection, double lat, double lng, int stage_id) {
        this.title = title;
        this.tel = tel;
        this.type = type;
        this.beds = beds;
        this.locality = locality;
        this.showSection = showSection;
        this.lat = lat;
        this.lng = lng;
        this.stage_id = stage_id;
    }
}
