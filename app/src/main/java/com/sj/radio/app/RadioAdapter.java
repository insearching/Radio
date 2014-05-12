package com.sj.radio.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sj.radio.app.entity.Radio;

import java.util.ArrayList;


public class RadioAdapter extends BaseAdapter{

    private ArrayList<Radio> data;
    private Context context;

    public RadioAdapter(Context context, ArrayList<Radio> data){
        this.data = data;
        this.context = context;
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
            holder.nameTv = (TextView)convertView.findViewById(R.id.nameTv);
            holder.countryTv = (TextView)convertView.findViewById(R.id.countryTv);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();

        }

        holder.nameTv.setText(data.get(position).getName());
        holder.countryTv.setText(data.get(position).getCountry());

        return convertView;
    }

    class ViewHolder{
        TextView nameTv;
        TextView countryTv;
    }
}
