package com.tonyandr.caminoguide.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tonyandr.caminoguide.R;

import java.util.Collections;
import java.util.List;

/**
 * Created by Tony on 01-Feb-15.
 */
public class DrawRecycleAdapter extends RecyclerView.Adapter<DrawRecycleAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    public ClickListener clickListener;

    List<DrawRecycleInformation> data = Collections.emptyList();
    private Context context;
    public DrawRecycleAdapter(Context context, List<DrawRecycleInformation> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.drawrecycle_row, viewGroup, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, int i) {
        DrawRecycleInformation current = data.get(i);
        viewHolder.title.setText(current.title);
        viewHolder.icon.setImageResource(current.iconId);
        if(current.active) {
            viewHolder.layout.setBackground(context.getResources().getDrawable(R.drawable.item_bg_active));
        } else {
            viewHolder.layout.setBackground(context.getResources().getDrawable(R.drawable.item_bg));
        }
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView title;
        ImageView icon;
        RelativeLayout layout;


        public MyViewHolder(View itemView) {
            super(itemView);
            layout = (RelativeLayout) itemView;
            title = (TextView) itemView.findViewById(R.id.listText);
            icon = (ImageView) itemView.findViewById(R.id.listIcon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.itemClicked(v, getPosition());
            }
        }
    }

    public interface ClickListener {
        public void itemClicked(View view, int position);
    }
}
