package com.accesscontrol.hephaestus.ameeting;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.accesscontrol.hephaestus.ameeting.util.AboutMac;
import com.accesscontrol.hephaestus.ameeting.util.HttpUtil;
import com.accesscontrol.hephaestus.ameeting.util.ShowInfoUtil;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShowMeetingsInfo extends AppCompatActivity implements View.OnClickListener{
    private Button bt_show_cut;
    private Button bt_show_add;
    private TextView tx_show_dateText;
    private ListView lv_show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_meetings_info);

//        bt_show_cut=(Button)findViewById(R.id.bt_show_cut);
//        bt_show_add=(Button)findViewById(R.id.bt_show_add);
//        tx_show_dateText = (TextView) findViewById(R.id.tx_show_dateText);
//        lv_show = (ListView) findViewById(R.id.lv_show);
//        tx_show_dateText.setText(ShowInfoUtil.getDate());
//
//        bt_show_cut.setOnClickListener(this);
//        bt_show_add.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String nowDateText=null;
        String requestDateText=null;
        RequestBody requestBody=null;
        switch (view.getId()) {
            case R.id.bt_show_cut:
                //获取当前显示的日期文本
                nowDateText = tx_show_dateText.getText().toString();
                //将日期倒退一天
                requestDateText = ShowInfoUtil.cutDate(ShowMeetingsInfo.this, nowDateText, 1);
                //将日期与加密mac地址作为参数发起网络请求
                requestBody=new FormBody.Builder()
                    .add("macAddress",AboutMac.getMD5String(AboutMac.getMac()))
                    .add("date",requestDateText)
                    .build();
                HttpUtil.sendOkHttpRequestWithBody(HttpUtil.getGetAllMeetingUrl(), requestBody, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        showServiceInfo(ShowMeetingsInfo.this,"服务器故障！");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData=response.body().string();
                        Log.d("responseData", "onResponse: "+responseData);
                        if (response.code()==200){//如果正常返回信息

                        }
                    }
                });
                break;
            case R.id.bt_show_add:
                break;
        }
    }

    private void showServiceInfo(final Context context, final String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,text,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
