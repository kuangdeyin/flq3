package com.ifenglian.rocklet.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ifenglian.rocklet.R;
import com.ifenglian.rocklet.bean.Device;
import com.ifenglian.rocklet.util.Config;

import java.util.List;

/**
 * Created by Administrator on 2015/11/10.
 */
public class ManagerAdapter extends RecyclerView.Adapter {

    private List<Device> deviceList;
    private Context context;

    public static interface OnRecyclerViewListener {

        void onItemClick(int position);

        boolean onItemLongClick(int position);
    }

    private OnRecyclerViewListener onRecyclerViewListener;

    public void setOnRecyclerViewListener(OnRecyclerViewListener onRecyclerViewListener) {
        this.onRecyclerViewListener = onRecyclerViewListener;
    }

    public ManagerAdapter(List<Device> deviceList) {
        this.deviceList = deviceList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_device, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        MyHolder holder = (MyHolder) viewHolder;
        holder.position = position;
        Device device = deviceList.get(position);

        holder.image.setImageBitmap(device.getHeadImage());
        holder.name.setText(device.getName());
        holder.level_value.setVisibility(View.INVISIBLE);
        holder.electricity.setVisibility(View.INVISIBLE);
        holder.level_textvalue.setVisibility(View.GONE);
        int electricity = Integer.parseInt(device.getElectricity());

        if (electricity <= 15) {
            holder.electricity.setImageResource(R.drawable.battery_low);
            holder.electricity.setVisibility(View.VISIBLE);
            holder.level_value.setVisibility(View.VISIBLE);
            holder.level_textvalue.setVisibility(View.GONE);
        } else {
            holder.level_textvalue.setVisibility(View.VISIBLE);
        }
        if (Config.devices.size() > 0) {
            for (Device device1 : Config.devices) {
                if (device1.getMac().equals(device.getMac())) {
                    if (device.getState() == 0) {
                        holder.state.setTextColor(0xfffc4e4e);
                        holder.state.setText(R.string.stopConnect);
                    } else if (device.getState() == 1) {
                        holder.state.setTextColor(0xff51c5c4);
                        holder.state.setText(R.string.connect);
                    } else {
                        holder.state.setTextColor(0xff51c5c4);
                        holder.state.setText(R.string.isconnet);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public int position;
        public ImageView image;
        public TextView name;
        public ImageView electricity;
        public TextView state;
        public TextView level_value;
        public TextView level_textvalue;

        public MyHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            electricity = (ImageView) itemView.findViewById(R.id.electricity);
            state = (TextView) itemView.findViewById(R.id.state);
            level_value = (TextView) itemView.findViewById(R.id.level_value);
            level_textvalue = (TextView) itemView.findViewById(R.id.level_textvalue);
        }

        @Override
        public void onClick(View v) {
            if (null != onRecyclerViewListener) {
                onRecyclerViewListener.onItemClick(position);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (null != onRecyclerViewListener) {
                return onRecyclerViewListener.onItemLongClick(position);
            }
            return false;
        }
    }
}
