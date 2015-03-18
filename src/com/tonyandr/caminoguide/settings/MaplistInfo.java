package com.tonyandr.caminoguide.settings;

/**
 * Created by Tony on 06-Feb-15.
 */
public class MaplistInfo {

    String size = null;
    String title = null;
    boolean selected = false;
    boolean enabled = true;
    int status = 0;

    public MaplistInfo(String size, String title, boolean selected, boolean enabled) {
        super();
        this.size = size;
        this.title = title;
        this.selected = selected;
        this.enabled = enabled;
    }

    public String getSize() {
        return size;
    }
    public void setSize(String code) {
        this.size = code;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String name) {
        this.title = name;
    }

    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setStatus(int status) {this.status = status; }
    public int getStatus() {return  status; }

}
