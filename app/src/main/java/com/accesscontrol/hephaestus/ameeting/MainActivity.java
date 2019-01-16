package com.accesscontrol.hephaestus.ameeting;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.math.BigInteger;
import java.security.MessageDigest;


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
    private Button signin;
    private Button inquire;
    private Button reservation;


    //程序初始即获取mac地址
    //获取mac地址并且加密
    public static String macOriginalString=AboutMac.getMac();
    public static String macMd5String=AboutMac.getMD5String(macOriginalString);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signin=(Button)findViewById(R.id.bt_main_signin);
        inquire=(Button)findViewById(R.id.bt_main_inquire);
        reservation=(Button)findViewById(R.id.bt_main_reservation);


        signin.setOnClickListener(this);
        inquire.setOnClickListener(this);
        reservation.setOnClickListener(this);
    }
    public void onClick(View view){
        if(view==signin){
            Intent signinIntent=new Intent(MainActivity.this,Signin.class);
            startActivity(signinIntent);
        }else if(view==inquire){
            Intent inquire=new Intent(MainActivity.this,Inquire.class);
            startActivity(inquire);
        }else if(view==reservation){

            Intent reservation=new Intent(MainActivity.this,Reservation.class);
            //向预定功能活动传递mac地址信息
            reservation.putExtra("mac_md5_data",macMd5String);
            startActivity(reservation);

        }
    }

}
