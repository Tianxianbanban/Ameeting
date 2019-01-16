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
            //获取mac地址并且加密
            String macOriginalString=getMac();
            String macMd5String=getMD5String(macOriginalString);

            Intent reservation=new Intent(MainActivity.this,Reservation.class);
            //向预定功能活动传递mac地址信息
            reservation.putExtra("mac_md5_data",macMd5String);
            startActivity(reservation);
        }
    }

    /**
     * 属于预定功能
     * 这是使用adb shell命令来获取mac地址的方式
     */
    public static String getMac() {
        String macSerial = null;
        String str = "";

        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return macSerial;
    }
    //md5加密
    public static String getMD5String(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8位字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            //一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方）
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
