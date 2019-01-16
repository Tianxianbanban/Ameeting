package com.accesscontrol.hephaestus.ameeting;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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
public class Reservation extends Activity {
    private Button start;
    private Button end;
    private Button cancle;
    private Button confirm;
    private TextView text_reservation_start;//开始时间默认显示当前日期
    private TextView Text_reservation_end;
    private TextView text_reservation_starttime;//时间
    private TextView Text_reservation_endtime;


    private int year,month,dayofmonth;//年月日
    private int hourofday,minute;//时分

    private DatePickerDialog dataPickerDialog;//日期选择对话框
    private TimePickerDialog timePickerDialog;//时间选择对话框

    boolean startdateconfirm=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

/*
        end=(Button)findViewById(R.id.bt_reservation_end);
        start=(Button)findViewById(R.id.bt_reservation_start);
        cancle=(Button)findViewById(R.id.bt_reservation_cancle);
        confirm=(Button)findViewById(R.id.bt_reservation_confirm);
        text_reservation_start=(TextView)findViewById(R.id.text_reservation_start);
        Text_reservation_end=(TextView)findViewById(R.id.text_reservation_end);
        text_reservation_starttime=(TextView)findViewById(R.id.text_reservation_starttime);
        Text_reservation_endtime=(TextView)findViewById(R.id.text_reservation_starttime);
        //要获取时间可以使用Calenda对象
        Calendar calendar=Calendar.getInstance();
        year=calendar.get(Calendar.YEAR);
        month=calendar.get(Calendar.MONTH);
        dayofmonth=calendar.get(Calendar.DAY_OF_MONTH);
        hourofday=calendar.get(Calendar.HOUR_OF_DAY);
        minute=calendar.get(Calendar.MINUTE);

        //日期选择框
        dataPickerDialog=new DatePickerDialog(this,new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker arg0, int year, int monthofyear,
                                  int dayofmonth) {
                // 把获取的日期显示在文本框内，月份从0开始计数，所以要加1
                String dateText = year + "-" + (monthofyear + 1) + "-" + dayofmonth;
                text_reservation_start.setText(dateText);//or edittext?
                timePickerDialog.show();//日期文本设置完毕，立即弹出日期选择框
                //startdateconfirm=true;
            }
        },year,month,dayofmonth);
        //时间选择框
        timePickerDialog=new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                //把获取的时间显示在文本框内，
                String timeText=hourOfDay + ":" + minute;
                text_reservation_starttime.setText(timeText);
            }
        },hourofday,minute,true);
        //各个按钮点击时间
        start.setOnClickListener(this);
        end.setOnClickListener(this);
        cancle.setOnClickListener(this);
        confirm.setOnClickListener(this);
    }
    public void onClick(View view){
        if(view==start){
            //startdateconfirm=false;
            Toast.makeText(this,"设置开始时间",Toast.LENGTH_SHORT).show();
            dataPickerDialog.show();//显示日期选择框,日期选择框选定日期即自动弹出时间选择框
            //用户选择具体时间
        }else if(view==end){
            //用户滑动设置时间
            Toast.makeText(this,"设置结束时间",Toast.LENGTH_SHORT).show();
        }else if(view==cancle){
            //取消操作，退回主界面
            finish();
            Toast.makeText(this,"点击取消",Toast.LENGTH_SHORT).show();
        }else if(view==confirm){
            //点击确认，存储数据，弹出确认确认信息
            Toast.makeText(this,"设置确认",Toast.LENGTH_SHORT).show();
        }*/
    }


}
