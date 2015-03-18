package com.tonyandr.caminoguide.stages;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tonyandr.caminoguide.R;

import java.util.ArrayList;

/**
 * Created by Tony on 20-Jan-15.
 */
public class StagesAdapter extends ArrayAdapter<StageListItem> {
//    THIS WORKS!
//    public StagesAdapter(Context context, ArrayList<Stage> users) {
//        super(context, 0, users);
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        // Get the data item for this position
//        Stage stage = getItem(position);
//        // Check if an existing view is being reused, otherwise inflate the view
//        if (convertView == null) {
//            convertView = LayoutInflater.from(getContext()).inflate(R.layout.stage_list_item, parent, false);
//        }
//        // Lookup view for data population
//        TextView tvStage = (TextView) convertView.findViewById(R.id.tv_stagenumber);
//        TextView tvFromTo = (TextView) convertView.findViewById(R.id.tv_from_to);
//        // Populate the data into the template view using the data object
//        tvStage.setText("Stage "+(stage.number < 10 ? "0"+stage.number : stage.number));
//        tvFromTo.setText(stage.start_point + " - " + stage.end_point);
//        // Return the completed view to render on screen
//        return convertView;
//    }
//    THIS WORKS!


    //   BUT THIS MUST BE BETTER (let's just trust the smarter guy from github)
    // View lookup cache
    private static class ViewHolder {
//        TextView tvNumber;
        TextView tvStage;
        TextView tvFromTo;
        ImageView ivTriangle;
        RelativeLayout layout;
        TextView tvFromToAlt;
    }

    public StagesAdapter(Context context, ArrayList<StageListItem> stages) {
        super(context, R.layout.stage_list_item, stages);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        StageListItem stage = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.stage_list_item, parent, false);
//            viewHolder.tvNumber = (TextView) convertView.findViewById(R.id.tv_number);
            viewHolder.tvStage = (TextView) convertView.findViewById(R.id.tv_stagenumber);
            viewHolder.tvFromTo = (TextView) convertView.findViewById(R.id.tv_from_to);
            viewHolder.tvFromToAlt = (TextView) convertView.findViewById(R.id.tv_from_to_alt);
            viewHolder.ivTriangle = (ImageView) convertView.findViewById(R.id.triangleIcon);
            viewHolder.layout = (RelativeLayout) convertView.findViewById(R.id.stage_list_item_layout);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object
//        viewHolder.tvNumber.setText(stage.number);

        viewHolder.tvStage.setText((stage.number < 10 ? "0"+stage.number : stage.number)+".");
        viewHolder.tvFromTo.setText(stage.fromTo);
        if (!stage.current) {
            viewHolder.ivTriangle.setImageResource(R.drawable.list_triangle);
            viewHolder.layout.setBackgroundColor(android.R.attr.background);
            viewHolder.tvFromTo.setTextColor(getContext().getResources().getColor(R.color.colorPrimaryText));
            viewHolder.tvStage.setTextColor(getContext().getResources().getColor(R.color.colorSecondaryText));
        } else {
            viewHolder.layout.setBackgroundColor(Color.rgb(244,68,68));
            viewHolder.tvStage.setTextColor(Color.WHITE);
            viewHolder.tvFromTo.setTextColor(Color.WHITE);
            viewHolder.ivTriangle.setImageResource(R.drawable.list_triangle_white);
        }
        if (stage.has_alt) {
            viewHolder.tvFromToAlt.setVisibility(View.VISIBLE);
            viewHolder.tvFromToAlt.setText("alt. "+stage.fromToAlt);
        } else {
            viewHolder.tvFromToAlt.setVisibility(View.GONE);
        }


        // Return the completed view to render on screen
        return convertView;
    }
}