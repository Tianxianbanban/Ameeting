package com.accesscontrol.hephaestus.ameeting.util;

/**
 * 接口地址
 * 与网络请求相关
 */

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil {

    //获取该会议室的当日将要进行的会议
    static String getAllMeetingUrl=new String("http://134.175.68.103:8080/getAllMeeting");
    //人脸校验开门
    static String faceOpenUrl=new String("http://134.175.68.103:8080/faceOpen");

    public static String getGetAllMeetingUrl() {
        return getAllMeetingUrl;
    }

    public static String getFaceOpenUrl() {
        return faceOpenUrl;
    }

    //网络请求
    public static void sendOkHttpRequestWithBody(String url, RequestBody requestBody, Callback callback){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

}
