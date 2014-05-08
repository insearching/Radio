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

        TextView nameTv;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.radio_list_item, null);
            nameTv = (TextView)convertView.findViewById(R.id.nameTv);
            convertView.setTag(nameTv);
        }
        else{
            nameTv = (TextView)convertView.getTag();
        }

        nameTv.setText(data.get(position).getName());

        return convertView;
    }

}
