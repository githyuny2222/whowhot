package com.example.whowhot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class LogListViewAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private ArrayList<LogListViewItem> item;
    private int layout;

    public LogListViewAdapter(Context context, int layout, ArrayList<LogListViewItem> item){
        this.inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.item = item;
        this.layout = layout;
    }
    @Override
    public int getCount() {
        return item.size();
    }

    @Override
    public Object getItem(int position) {
        return item.get(position).getLogText();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView=inflater.inflate(layout,parent,false);
        }
        LogListViewItem listViewItem = item.get(position);
        //ImageView icon=(ImageView)convertView.findViewById(R.id.imageview);
        //icon.setImageResource(listViewItem.getIcon());
        TextView log = (TextView)convertView.findViewById(R.id.textview);
        log.setText(listViewItem.getLogText());

        return convertView;
    }
}
