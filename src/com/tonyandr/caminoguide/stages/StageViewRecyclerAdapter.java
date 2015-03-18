package com.tonyandr.caminoguide.stages;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tonyandr.caminoguide.R;

import java.util.Collections;
import java.util.List;

/**
 * Created by Tony on 19-Feb-15.
 */

public class StageViewRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private LayoutInflater inflater;
    private Context context;

    List<StageViewAlbItem> data = Collections.emptyList();
    public StageViewRecyclerAdapter(Context context, List<StageViewAlbItem> data) {
        this.data = data;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            //inflate your layout and pass it to view holder
            View view = inflater.inflate(R.layout.stage_albergues_list_item, parent, false);
            return new VHItem(view);
        } else if (viewType == TYPE_HEADER) {
            //inflate your layout and pass it to view holder
            View view = inflater.inflate(R.layout.fragment_stage_view_header, parent, false);
            return new VHHeader(view);
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VHItem) {
            StageViewAlbItem dataItem = getItem(position);
            //cast holder to VHItem and set data
            VHItem vhItem = (VHItem) holder;
            if (dataItem.showSection) {
                vhItem.tvSection.setText(dataItem.locality);
                vhItem.tvSection.setVisibility(View.VISIBLE);
            } else {
                vhItem.tvSection.setVisibility(View.GONE);
            }
//            vhItem.tvIcon.setText("A");
            vhItem.tvTitle.setText(dataItem.title);
            vhItem.tvTel.setText(dataItem.tel);
            vhItem.tvBeds.setText(dataItem.beds + " beds, " + dataItem.type);

        } else if (holder instanceof VHHeader) {
            //cast holder to VHHeader and set data for header.
            VHHeader vhHeader = (VHHeader) holder;

        }
    }

    @Override
    public int getItemCount() {
        return data.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    private StageViewAlbItem getItem(int position) {
        return data.get(position - 1);
    }

    class VHItem extends RecyclerView.ViewHolder {
//        TextView tvIcon;
        TextView tvTitle;
        TextView tvTel;
        TextView tvBeds;
        TextView tvSection;
        ImageView ibFindAlb;

        public VHItem(View itemView) {
            super(itemView);
//            tvIcon = (TextView) itemView.findViewById(R.id.iv_icon);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_alb_title);
            tvTel = (TextView) itemView.findViewById(R.id.tv_alb_tel);
            tvBeds = (TextView) itemView.findViewById(R.id.tv_alb_beds);
            tvSection = (TextView) itemView.findViewById(R.id.tv_alblist_section);
            ibFindAlb = (ImageView) itemView.findViewById(R.id.ib_find_alb);
        }
    }

    class VHHeader extends RecyclerView.ViewHolder {
        Button button;

        public VHHeader(View itemView) {
            super(itemView);
        }
    }
}