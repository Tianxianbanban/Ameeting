package com.accesscontrol.hephaestus.ameeting.util;

/**
 * 先关信息
 * 当前日期的获取
 * 日期更改
 */

import android.content.Context;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ShowInfoUtil {
    //获取当前日期
    public static String getDate(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDateFormat.format(new Date(System.currentTimeMillis()));
        return date;
    }

    //日期减x日
    public static String cutDate(Context context,String nowtimetext, int cutDay){
        //获取现有的日期文本
        //再次确定日期格式
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //文本转日期
        try {
            Date dateFromText=simpleDateFormat.parse(nowtimetext);
            //日期后退
            Calendar specialDateCalenda = Calendar.getInstance();
            specialDateCalenda.setTime(dateFromText); //注意在此处将 specialDate 的值设置为特定日期，即当前文本转化过来的日期
            specialDateCalenda.add(Calendar.DAY_OF_YEAR, -cutDay); //所谓特定时间的cutDay天前
            Date thenDate=specialDateCalenda.getTime();
            String datetext=simpleDateFormat.format(thenDate);
            return datetext;
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(context,"发生错误！",Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    //日期加x日
    public static String addDate(Context context,String nowtimetext,int addDay){
        //获取现有的日期文本
        //再次确定日期格式
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //文本转日期
        try {
            Date dateFromText=simpleDateFormat.parse(nowtimetext);
            //日期后退
            Calendar specialDateCalenda = Calendar.getInstance();
            specialDateCalenda.setTime(dateFromText); //注意在此处将 specialDate 的值设置为特定日期，即当前文本转化过来的日期
            specialDateCalenda.add(Calendar.DAY_OF_YEAR, addDay); //所谓特定时间的addDay天后
            Date thenDate=specialDateCalenda.getTime();
            String datetext=simpleDateFormat.format(thenDate);
            return datetext;
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(context,"发生错误！",Toast.LENGTH_SHORT).show();
            return null;
        }
    }

}
