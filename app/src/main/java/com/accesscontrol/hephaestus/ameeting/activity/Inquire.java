package com.accesscontrol.hephaestus.ameeting.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.accesscontrol.hephaestus.ameeting.R;
import com.accesscontrol.hephaestus.ameeting.adapter.MeetingAdapter;
import com.accesscontrol.hephaestus.ameeting.json.GetAllMeeting;
import com.accesscontrol.hephaestus.ameeting.util.AboutMac;
import com.accesscontrol.hephaestus.ameeting.util.HttpUtil;
import com.accesscontrol.hephaestus.ameeting.util.ShowInfoUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 动态显示会议列表
 */
public class Inquire extends Activity {//AndroidThings对于AppCompatActivity注意
    String md5Mac=AboutMac.getMD5String(AboutMac.getMac());
    String date=ShowInfoUtil.getDate();

    String nowDateText;
    String requestDateText;

    protected static final int WHAT_SUCCESS = 1;
    protected static final int WHAT_ERROR = 2;


    private Button cuttime,addtime;
    private TextView timeText;
    private ListView inquire_listview;
    private List<GetAllMeeting.Content.Info> infoList;
    private TextView tx_aboutmeetinglist_null;//没有会议的时候显示
    private ProgressBar pb_inquire;//圆形进度条


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);//用于隐藏标题栏
        setContentView(R.layout.activity_inquire);

        cuttime=(Button)findViewById(R.id.cuttime);
        addtime=(Button)findViewById(R.id.addtime);
        timeText = (TextView) findViewById(R.id.text_inquire_timeText);
        inquire_listview = (ListView) findViewById(R.id.inquire_listview);
        tx_aboutmeetinglist_null=(TextView)findViewById(R.id.tx_aboutmeetinglist_null);
        pb_inquire=(ProgressBar)findViewById(R.id.pb_inquire);

        //界面初始
        timeText.setText(date);
        setData(date);

        //点击事件
        cuttime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //显示进度条
                pb_inquire.setVisibility(View.VISIBLE);
                //获取当前显示的日期文本
                nowDateText = timeText.getText().toString();
                //将日期倒退一天
                requestDateText = ShowInfoUtil.cutDate(Inquire.this, nowDateText, 1);
                //将待显示的日期数据设置进
                timeText.setText(requestDateText);
                //将日期与加密mac地址作为参数发起网络请求
                setData(requestDateText);
            }
        });
        addtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pb_inquire.setVisibility(View.VISIBLE);
                nowDateText = timeText.getText().toString();
                requestDateText = ShowInfoUtil.addDate(Inquire.this, nowDateText, 1);
                timeText.setText(requestDateText);
                setData(requestDateText);
            }
        });
    }


    //信息显示设置
    private void setData(String dateText){
        final String url=HttpUtil.getGetAllMeetingUrl() +"?macAddress="+md5Mac+"&date="+date;
        Log.d("url", "onCreate: "+url);
        RequestBody requestBody=new FormBody.Builder()
                .add("macAddress",md5Mac)
                .add("date",dateText)
                .build();
        HttpUtil.sendOkHttpRequestWithBody(HttpUtil.getGetAllMeetingUrl(), requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tx_aboutmeetinglist_null.setText("服务器故障！");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData=response.body().string();
                Log.d("responseData", "onResponse: "+responseData);
                if (response.code()==200){//如果正常返回信息就继续解析
                    Gson gson=new Gson();
                    GetAllMeeting getAllMeeting=gson.fromJson(responseData, GetAllMeeting.class);
                    infoList=getAllMeeting.getData().getReserveInfos();
                    //将已经进行的会议过滤掉，将不会显示在屏幕上
                    for (int i=0;i<infoList.size();i++){
                        if (infoList.get(i).getFlag()==2){
                            infoList.remove(i);
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tx_aboutmeetinglist_null.setText("");
                            pb_inquire.setVisibility(View.GONE);
                            if (infoList.size()==0){
                                tx_aboutmeetinglist_null.setText("暂无会议……");
                            }
                            MeetingAdapter meetingAdapter=new MeetingAdapter(Inquire.this,R.layout.item_adapter,infoList);
                            inquire_listview.setAdapter(meetingAdapter);//将信息设置进适配器
                        }
                    });
                }
            }
        });

    }





    /**
     * 顶部菜单
     * @param menu
     * @return
     */
    //顶部菜单，选择返回或者预定
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inquiremenu,menu);
        return true;
    }
    //顶部菜单点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_inquire_back:
                finish();//返回主页
                break;
            case R.id.item_inquire_reservation://进行预定
                Intent reservation=new Intent(Inquire.this,Reservation.class);
                //向预定功能活动传递mac地址信息
                reservation.putExtra("mac_md5_data",MainActivity.macMd5String);
                startActivity(reservation);

        }
        return true;
    }
}
