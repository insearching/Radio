package com.sj.radio.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sj.radio.app.entity.Radio;

import java.util.ArrayList;


public class RadioAdapter extends BaseAdapter{

    private ArrayList<Radio> data;
    private Context context;

    public enum PlaybackStatus {
        NONE, LOADING, PLAYING
    }

    private PlaybackStatus statusArr[];

    public RadioAdapter(Context context, ArrayList<Radio> data){
        this.data = data;
        this.context = context;
        statusArr = new PlaybackStatus[data.size()];
        for(int i=0; i<statusArr.length; i++){
            statusArr[i] = PlaybackStatus.NONE;
        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Radio getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.radio_list_item, null);
            holder = new ViewHolder();

            holder.progressBar = (ProgressBar)convertView.findViewById(R.id.progressBar);
            holder.playIv = (ImageView)convertView.findViewById(R.id.playIv);
            holder.nameTv = (TextView)convertView.findViewById(R.id.nameTv);
            holder.countryTv = (TextView)convertView.findViewById(R.id.countryTv);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

        holder.playIv.setVisibility(statusArr[position] == PlaybackStatus.PLAYING ? View.VISIBLE : View.INVISIBLE);
        holder.progressBar.setVisibility(statusArr[position] == PlaybackStatus.LOADING ? View.VISIBLE : View.INVISIBLE);
        holder.nameTv.setText(data.get(position).getName());
        holder.countryTv.setText(data.get(position).getCountry());

        return convertView;
    }

    class ViewHolder{
        ProgressBar progressBar;
        ImageView playIv;
        TextView nameTv;
        TextView countryTv;
    }

    public void setItemStatus(int position, PlaybackStatus status){
        statusArr[position] = status;
    }
}
