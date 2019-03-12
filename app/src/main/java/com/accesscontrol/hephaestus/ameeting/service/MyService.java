package com.accesscontrol.hephaestus.ameeting.service;

/**
 * 该服务用于实现与服务端的长连接
 * 接收开门指令
 * 接收到open信号的时候LED灯闪烁5秒
 */

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.accesscontrol.hephaestus.ameeting.util.AboutMac;
import com.dhh.websocket.Config;
import com.dhh.websocket.RxWebSocket;
import com.dhh.websocket.WebSocketSubscriber;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

import okhttp3.WebSocket;
import okio.ByteString;

public class MyService extends Service {
    private String TAG="MyService";
    /**
     * 关于长连接
     */
    //获取mac地址
    private String macAddress=AboutMac.getMD5String(AboutMac.getMac());
    private String url=new String("ws://134.175.68.103:8080/link/"+macAddress);
//    private String macAddress=new String("1c246af4c12e2b391f693e16f4c45feb");
//    private String url=new String("ws://134.175.68.103:8080/link/"+macAddress);

    /**
     * 关于外设led灯
     */
    Handler handler = new Handler();//用于循环操作
    Gpio gpio;//通用输入输出端口
    boolean ledState = false;//用于标记led的亮起状态

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            //BCM21在两排针脚靠近USB的地方
            gpio = PeripheralManager.getInstance().openGpio("BCM21");//获取通用输入输出，板子的BCM6这个位置连接led
            gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);//设置gpio为输出的设备，并且指定初始值为低，此处即为false；
//            handler.post(runnable);//开始循环
        } catch (IOException e) {
            e.printStackTrace();
        }

        //init config
        Config config = new Config.Builder()
//                .setShowLog(true)
                .setShowLog(true,TAG)
//                .setReconnectInterval(1, TimeUnit.SECONDS)  //set reconnect interval
//                .setSSLSocketFactory(yourSSlSocketFactory, yourX509TrustManager) // wss support
                .build();

        RxWebSocket.setConfig(config);
        // please use WebSocketSubscriber
        RxWebSocket.get(url)
                //RxLifecycle : https://github.com/dhhAndroid/RxLifecycle
//                .compose(RxLifecycle.with(this).<WebSocketInfo>bindOnDestroy())//注释掉，以去除上下文的影响
                .subscribe(new WebSocketSubscriber() {
                    @Override
                    public void onOpen(@NonNull WebSocket webSocket) {
                        Log.d("MainActivity", "onOpen1:");
                    }

                    @Override
                    public void onMessage(@NonNull String text) {
                        Log.d("MainActivity", "返回数据:" + text);
                        if (text.equals("open")){
                            handler.post(runnable);//开始循环
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(5000);//让闪烁持续5秒钟之后关闭
                                        handler.removeCallbacks(runnable);
                                        gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);//设置gpio为输出的设备，并且指定初始值为低，此处即为false；
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }
                    }

                    @Override
                    public void onMessage(@NonNull ByteString byteString) {

                    }

                    @Override
                    protected void onReconnect() {
                        Log.d("MainActivity", "重连:");
                    }

                    @Override
                    protected void onClose() {
                        super.onClose();
                        Log.d(TAG, "onClose: ");
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Log.d(TAG, "onError: ");
                    }

                    @Override
                    public void onStart() {
                        super.onStart();
                        Log.d(TAG, "onStart: ");
                    }
                });
        return super.onStartCommand(intent,flags,startId);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (gpio == null) return;
            try {
                ledState = !ledState;//改变led状态
                gpio.setValue(ledState);//控制led状态
                handler.postDelayed(this, 500);//半秒后再执行本初操作
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 长连接没有处理
     * 只处理了led灯
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);//停止循环
        try {
            gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);//设置gpio为输出的设备，并且指定初始值为低，此处即为false；
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            gpio.close();//关闭通用输入输出
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
