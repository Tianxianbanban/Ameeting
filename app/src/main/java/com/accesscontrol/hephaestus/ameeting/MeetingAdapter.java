package com.accesscontrol.hephaestus.ameeting;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

class MeetingAdapter extends ArrayAdapter<Meeting> {

    private int resuorceId;

    public MeetingAdapter(Context context, int textViewResourceId, List<Meeting> objects){
        super(context,textViewResourceId,objects);
        resuorceId=textViewResourceId;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//            if (convertView == null) {
//                convertView = View.inflate(Inquire.this, R.layout.item_adapter, null);
//            }
        //得到当前行的数据对象
        Meeting meeting = getItem(position);
        //得到当前行的子view
        View view=LayoutInflater.from(getContext()).inflate(resuorceId,parent,false);
        TextView startText = (TextView)view.findViewById(R.id.meetingname);
        TextView endText = (TextView) view.findViewById(R.id.meetingtime);
        //设置数据
//            startText.setText(Ameeting.getData().getResrveInfos().getStartTime());
//            endText.setText(Ameeting.getData().getResrveInfos().getEndTime());
        startText.setText(meeting.getName());
        endText.setText(meeting.getTime());
        Log.d("显示内容", "content: "+meeting.getName());

        return view;
    }
}

