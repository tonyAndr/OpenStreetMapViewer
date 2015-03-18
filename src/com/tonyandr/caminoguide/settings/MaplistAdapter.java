package com.tonyandr.caminoguide.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tonyandr.caminoguide.R;

import java.util.ArrayList;

/**
 * Created by Tony on 07-Feb-15.
 */
public class MaplistAdapter extends ArrayAdapter<MaplistInfo> {
    public ArrayList<MaplistInfo> mapList;

    public MaplistAdapter(Context context, ArrayList<MaplistInfo> maps) {
        super(context, 0, maps);
        this.mapList = new ArrayList<MaplistInfo>();
        this.mapList.addAll(maps);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        MaplistInfo maplistInfo = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.maplist_row, parent, false);
        }
        // Lookup view for data population
        TextView size = (TextView) convertView.findViewById(R.id.size_id);
        CheckBox cb = (CheckBox) convertView.findViewById(R.id.cb_id);
        TextView title = (TextView) convertView.findViewById(R.id.title_id);
        ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);

        size.setText(maplistInfo.getSize());
        title.setText(maplistInfo.getTitle());
        cb.setChecked(maplistInfo.isSelected());
        cb.setEnabled(maplistInfo.isEnabled());
        cb.setVisibility((maplistInfo.getStatus() == 1) ? View.GONE : View.VISIBLE);
        progressBar.setVisibility((maplistInfo.getStatus() == 1) ? View.VISIBLE : View.GONE);

        return convertView;
    }
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        // Get the data item for this position
//        MaplistInfo map = getItem(position);
//        // Check if an existing view is being reused, otherwise inflate the view
//        final ViewHolder viewHolder; // view lookup cache stored in tag
//        if (convertView == null) {
//            viewHolder = new ViewHolder();
//            LayoutInflater inflater = LayoutInflater.from(getContext());
//            convertView = inflater.inflate(R.layout.maplist_row, parent, false);
//            viewHolder.size = (TextView) convertView.findViewById(R.id.size_id);
//            viewHolder.cb = (CheckBox) convertView.findViewById(R.id.cb_id);
//            viewHolder.title = (TextView) convertView.findViewById(R.id.title_id);
//            convertView.setTag(viewHolder);
//
////            viewHolder.cb.setOnClickListener( new View.OnClickListener() {
////                public void onClick(View v) {
////                    CheckBox cb = (CheckBox) v ;
////                    MaplistInfo maplistInfo = (MaplistInfo) v.getTag();
//////                        Toast.makeText(v.getContext(),
//////                                "Clicked on Checkbox: " + cb.getText() +
//////                                        " is " + cb.isChecked(),
//////                                Toast.LENGTH_SHORT).show();
////                    if (!cb.isEnabled()) {
////                        Toast.makeText(v.getContext(),
////                                cb.getText() + " can't be excluded!",
////                                Toast.LENGTH_SHORT).show();
////                    }
////                    maplistInfo.setSelected(cb.isChecked());
////                }
////            });
//        } else {
//            viewHolder = (ViewHolder) convertView.getTag();
//        }
//
////                convertView.setOnClickListener(new View.OnClickListener() {
////                    @Override
////                    public void onClick(View v) {
////                        ViewGroup vg = (ViewGroup) v;
////                        CheckBox cb = (CheckBox) vg.getChildAt(0);
////                        if (!cb.isEnabled()) {
////                            Toast.makeText(v.getContext(),
////                                    cb.getText() + " can't be excluded!",
////                                Toast.LENGTH_SHORT).show();
////
////                            Log.d("VH", "v.tag "+v.getTag());
////                        }
////                        else {
////                            cb.setChecked(!cb.isChecked());
////                            MaplistInfo maplistInfo = (MaplistInfo) v.getTag();
////                        Toast.makeText(v.getContext(),
////                                "Clicked on Checkbox: " + cb.getText() +
////                                        " is " + cb.isChecked(),
////                                Toast.LENGTH_SHORT).show();
////                            maplistInfo.setSelected(cb.isChecked());
////                            Log.d("VH", maplistInfo.getTitle());
////
////                        }
////
////
////                    }
////                });
//
//
//        // Populate the data into the template view using the data object
////        viewHolder.tvNumber.setText(stage.number);
//        viewHolder.size.setText(map.getSize());
//        viewHolder.title.setText(map.getTitle());
//        viewHolder.cb.setChecked(map.isSelected());
//        if(map.getTitle().equals("Spain Base Map")) {
//            viewHolder.cb.setEnabled(false);
//        }
//        // Return the completed view to render on screen
//        return convertView;
//    }
}
