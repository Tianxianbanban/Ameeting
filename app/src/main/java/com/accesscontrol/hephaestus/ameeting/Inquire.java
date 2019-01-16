package com.accesscontrol.hephaestus.ameeting;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */

/**
 * 动态显示列表
 * handler+thread处理联网请求，得到json数据，解析成list<meeting>
 * 使用baseAdapter显示文本列表
 */
public class Inquire extends Activity implements View.OnClickListener{
    protected static final int WHAT_SUCCESS = 1;
    protected static final int WHAT_ERROR = 2;

    private Button cuttime,addtime;
    private TextView timeText;
    private ListView inquire_listview;
    private MeetingAdapter adapter;
    private List<Meeting> meetings;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_SUCCESS:
                    inquire_listview.setAdapter(adapter);
                    break;
                case WHAT_ERROR:
                    //加载数据失败提示
                    Toast.makeText(Inquire.this, "数据加载失败", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inquire);

        /**
         * 时间
         */
        //布局顶部加减时间按钮
        cuttime=(Button)findViewById(R.id.cuttime);
        addtime=(Button)findViewById(R.id.addtime);
        //布局顶部时间文本
        timeText = (TextView) findViewById(R.id.text_inquire_timeText);
        //listview会议排列
        inquire_listview = (ListView) findViewById(R.id.inquire_listview);
        adapter = new MeetingAdapter();
        //获取当前时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final String string = simpleDateFormat.format(new Date(System.currentTimeMillis()));
        timeText.setText(string);

        cuttime.setOnClickListener(this);
        addtime.setOnClickListener(this);
        Toast.makeText(Inquire.this, "000000000", Toast.LENGTH_SHORT).show();


        /**
         * 会议列表
         */
        //启动线程请求服务器动态加载数据并且显示
        new Thread() {
            public void run() {
                //联网得到json字符串
                // try {
//                    String jsonString = requestJson();
//                    //解析json
//                    Gson gson=new Gson();
//                    //String s=gson.fromJson(jsonString,String.class);
//                    meeting =gson.fromJson(jsonString, new TypeToken<List<Meeting>>(){}.getType());
//
                String jsonStringFromOkHttp=sendRequestWithOkHttp();
                Log.d("look for json :",jsonStringFromOkHttp);
                //显示，更新界面
                Gson gson=new Gson();
                meetings=gson.fromJson(jsonStringFromOkHttp,new TypeToken<List<Meeting>>(){}.getType());
                handler.sendEmptyMessage(WHAT_SUCCESS);//发送请求成功
                // } catch (Exception e) {
                //  e.printStackTrace();
                // Log.d("look for json :","发送请求失败");
                //  handler.sendEmptyMessage(WHAT_ERROR);//发送请求失败
            }
            //  }
        }.start();
    }

    private String sendRequestWithOkHttp(){
        String result=null;
        try{
            OkHttpClient client=new OkHttpClient();
            Request request=new Request.Builder().get().url("http://192.168.2.105/data.json").build();
            Response response=client.newCall(request).execute();
            String responseData=response.body().string();
            result=responseData;
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    //改变日期
    public void onClick(View view){
        if(view==cuttime){
            //获取现有的日期文本
            String nowtimetext=timeText.getText().toString();
            //再次确定日期格式
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            //文本转日期
            try {
                Date dateFromText=simpleDateFormat.parse(nowtimetext);
                //日期后退
                Calendar specialDateCalenda = Calendar.getInstance();
                specialDateCalenda.setTime(dateFromText); //注意在此处将 specialDate 的值设置为特定日期，即当前文本转化过来的日期
                specialDateCalenda.add(Calendar.DAY_OF_YEAR, -1); //所谓特定时间的1天前
                Date thenDate=specialDateCalenda.getTime();
                String datetext=simpleDateFormat.format(thenDate);
                timeText.setText(datetext);
                Log.d("youcansee", "onClick: ");
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(this,"错误",Toast.LENGTH_SHORT).show();
            }

        }else if(view==addtime){
            //获取现有的日期文本
            String nowtimetext=timeText.getText().toString();
            //再次确定日期格式
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            //文本转日期
            try {
                Date dateFromText=simpleDateFormat.parse(nowtimetext);
                //日期后退
                Calendar specialDateCalenda = Calendar.getInstance();
                specialDateCalenda.setTime(dateFromText); //注意在此处将 specialDate 的值设置为特定日期，即当前文本转化过来的日期
                specialDateCalenda.add(Calendar.DAY_OF_YEAR, 1); //所谓特定时间的1天前
                Date thenDate=specialDateCalenda.getTime();
                String datetext=simpleDateFormat.format(thenDate);
                timeText.setText(datetext);
                Log.d("youcansee", "onClick: ");
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(this,"错误",Toast.LENGTH_SHORT).show();
            }
        }
    }


    //请求json数据
//    private String requestJson() throws Exception {
//
//        String path = "http://10.0.2.2/data.json";
//        String result = null;
//        URL url = null;
//        //try {
//        url = new URL(path);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setConnectTimeout(1000);
//        connection.setReadTimeout(1000);
//        connection.connect();
//
//        int responsecode = connection.getResponseCode();
//        if (responsecode == 200) {
//            InputStream is = connection.getInputStream();
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            byte[] buffer = new byte[1024];
//            int len = -1;
//            while ((len = is.read(buffer)) != -1) {
//                baos.write(buffer, 0, len);
//            }
//            baos.close();
//            is.close();
//            connection.disconnect();
//            result = baos.toString();
//        } else {
//
//        }
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
//        return result;
//    }

    class MeetingAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return meetings.size();
        }
        @Override
        public Object getItem(int position) {
            return meetings.get(position);
        }
        @Override
        public long getItemId(int position) {
            return 0;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(Inquire.this, R.layout.item_adapter, null);
            }
            //得到当前行的数据对象
            Meeting Ameeting = meetings.get(position);
            //得到当前行的子view
            TextView nameText = (TextView) convertView.findViewById(R.id.meetingname);
            TextView timeText = (TextView) convertView.findViewById(R.id.meetingtime);
            //设置数据
            nameText.setText(Ameeting.getName());
            timeText.setText(Ameeting.getTime());
            return convertView;
        }
    }
}
