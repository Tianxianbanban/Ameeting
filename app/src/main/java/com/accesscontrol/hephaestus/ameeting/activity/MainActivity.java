package com.accesscontrol.hephaestus.ameeting.activity;

/**
 * 主界面
 * 对应三个项目
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.accesscontrol.hephaestus.ameeting.R;
import com.accesscontrol.hephaestus.ameeting.service.MyService;
import com.accesscontrol.hephaestus.ameeting.util.AboutMac;
import com.google.android.things.device.TimeManager;
import java.util.Calendar;

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
public class MainActivity extends Activity implements android.view.View.OnClickListener{
    private String TAG="MainActivity";

    private Button signin;//签到
    private Button inquire;//预定
    private Button reservation;//查询

    //程序初始即获取mac地址
    //获取mac地址并且加密
    public static String macOriginalString=AboutMac.getMac();
    public static String macMd5String=AboutMac.getMD5String(macOriginalString);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        自动校准系统时间
        导入依赖
        添加设置代码
         */
        try {
            TimeManager timeManager = TimeManager.getInstance();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, 2019);
            calendar.set(Calendar.MONTH, 2);
            calendar.set(Calendar.DAY_OF_MONTH, 5);
            timeManager.setTime(calendar.getTimeInMillis());
        } catch (Exception e) {
            Log.e("SET_TIME", "SET_TIME 权限失效");
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);//用于隐藏标题栏
        setContentView(R.layout.activity_main);

//        //启动服务
        Intent serviceIntent=new Intent(MainActivity.this,MyService.class);
        startService(serviceIntent);

        signin=(Button)findViewById(R.id.bt_main_signin);
        inquire=(Button)findViewById(R.id.bt_main_inquire);
        reservation=(Button)findViewById(R.id.bt_main_reservation);

        //关于三个模块的点击事件
        signin.setOnClickListener(this);
        inquire.setOnClickListener(this);
        reservation.setOnClickListener(this);
    }


    public void onClick(View view){
        if(view==signin){//打开相机，进行人脸信息传递
            Intent signinIntent=new Intent(MainActivity.this,Signin.class);
            startActivity(signinIntent);
        }else if(view==inquire){//发送请求，解析并且展示会议信息
            Intent inquire=new Intent(MainActivity.this,Inquire.class);
            startActivity(inquire);
        }else if(view==reservation){//显示二维码，进行预定
            Intent reservation=new Intent(MainActivity.this,Reservation.class);
            //向预定功能活动传递mac地址信息
            reservation.putExtra("mac_md5_data",macMd5String);
            startActivity(reservation);
        }
    }


}
