package com.accesscontrol.hephaestus.ameeting.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.accesscontrol.hephaestus.ameeting.R;
import com.accesscontrol.hephaestus.ameeting.json.GetAllMeeting;

import java.util.List;

public class MeetingAdapter extends ArrayAdapter<GetAllMeeting.Content.Info>{
    private int resourceId;
//    private List<GetAllMeeting.Content.Info> mObjects;

    public MeetingAdapter(Context context, int resource, List<GetAllMeeting.Content.Info> objects) {
        super(context, resource, objects);
        resourceId=resource;
//        this.mObjects=objects;
    }

    @Override
    public View getView(int position,View convertView,ViewGroup parent) {
        GetAllMeeting.Content.Info info=getItem(position);
        View view=LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        TextView tx_meetingTopic=(TextView)view.findViewById(R.id.tx_meetingTopic);//会议主题
        TextView meetingtime_start=(TextView)view.findViewById(R.id.meetingtime_start);//起始时间
        TextView meetingtime_end=(TextView)view.findViewById(R.id.meetingtime_end);//结束时间
        TextView tx_aboutmeeting=(TextView)view.findViewById(R.id.tx_aboutmeeting);//会议进行情况
        ImageView iv_indication=(ImageView)view.findViewById(R.id.iv_indication);//情况指示图标

        tx_meetingTopic.setText(info.getTopic());
        meetingtime_start.setText("起始:"+info.getStartTime());
        meetingtime_end.setText("结束:"+info.getEndTime());

        int position_flag=info.getFlag();
        if (position_flag==0){//正在进行
            iv_indication.setImageResource(R.drawable.green);//绿色指示正在进行
            tx_aboutmeeting.setText("进行中…");
        }else if (position_flag==1){//未开始
            iv_indication.setImageResource(R.drawable.yellow);//黄色指示未开始
        }
        return view;
    }
}
