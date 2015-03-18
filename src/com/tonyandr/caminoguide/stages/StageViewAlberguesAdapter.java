package com.tonyandr.caminoguide.stages;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tonyandr.caminoguide.R;

import java.util.ArrayList;

/**
 * Created by Tony on 10-Feb-15.
 */
public class StageViewAlberguesAdapter extends ArrayAdapter<StageViewAlbItem> {

    Context context;
    private FragmentManager fragmentManager;


    private static class ViewHolder {
//        TextView tvIcon;
        TextView tvTitle;
        TextView tvTel;
        TextView tvBeds;
        TextView tvSection;
        ImageView ibFindAlb;
    }

    public StageViewAlberguesAdapter(Context context, ArrayList<StageViewAlbItem> rows) {
        super(context, R.layout.stage_albergues_list_item, rows);
        this.context = context;
    }

    private FragmentManager getManager () {
        try{
            final Activity activity = (Activity) context;

            // Return the fragment manager
            return activity.getFragmentManager();

            // If using the Support lib.
            // return activity.getSupportFragmentManager();

        } catch (ClassCastException e) {
//            Log.d("FUCK", "Can't get the fragment manager with this");
        }
        return null;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        final StageViewAlbItem row = getItem(position);
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.stage_albergues_list_item, parent, false);
//            viewHolder.tvIcon = (TextView) convertView.findViewById(R.id.iv_icon);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_alb_title);
            viewHolder.tvTel = (TextView) convertView.findViewById(R.id.tv_alb_tel);
            viewHolder.tvBeds = (TextView) convertView.findViewById(R.id.tv_alb_beds);
            viewHolder.tvSection = (TextView) convertView.findViewById(R.id.tv_alblist_section);
            viewHolder.ibFindAlb = (ImageView) convertView.findViewById(R.id.ib_find_alb);
            convertView.setTag(viewHolder);

//            viewHolder.ibFindAlb.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    Bundle bundle = new Bundle();
//                    bundle.putInt("stage_id", row.stage_id);
//                    bundle.putDouble("lat", row.lat);
//                    bundle.putDouble("lng", row.lng);
//                    Log.d("LV", "Clicked: "+row.title + " " + row.lat +":"+row.lng);
//                    if (getManager() != null) {
//                        OSMFragment osmFragment = new OSMFragment();
//                        osmFragment.setArguments(bundle);
//                        fragmentManager = getManager();
//                        if (fragmentManager.findFragmentByTag("FragmentView") != null) {
//                            FragmentTransaction transaction = fragmentManager.beginTransaction();
//                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//                            transaction.replace(R.id.stage_fragment_holder_id, osmFragment, "OSMFragment");
//                            transaction.addToBackStack(OSMFragment.class.getName());
//                            transaction.commit();
//                        }
//                    }
//
//                }
//            });

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object
        if (row.showSection) {
            viewHolder.tvSection.setText(row.locality);
            viewHolder.tvSection.setVisibility(View.VISIBLE);
        } else {
            viewHolder.tvSection.setVisibility(View.GONE);
        }
//        viewHolder.tvIcon.setText("A");
        viewHolder.tvTitle.setText(row.title);
        viewHolder.tvTel.setText(row.tel);
        viewHolder.tvBeds.setText(row.beds + " beds, " + row.type);
        // Return the completed view to render on screen
        return convertView;
    }

}
