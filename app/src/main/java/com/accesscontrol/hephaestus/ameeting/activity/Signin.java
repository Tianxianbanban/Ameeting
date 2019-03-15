package com.accesscontrol.hephaestus.ameeting.activity;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.accesscontrol.hephaestus.ameeting.adapter.MeetingAdapter;
import com.accesscontrol.hephaestus.ameeting.data.Constants;
import com.accesscontrol.hephaestus.ameeting.R;
import com.accesscontrol.hephaestus.ameeting.json.FaceOpen;
import com.accesscontrol.hephaestus.ameeting.json.GetAllMeeting;
import com.accesscontrol.hephaestus.ameeting.util.AboutMac;
import com.accesscontrol.hephaestus.ameeting.util.HttpUtil;
import com.accesscontrol.hephaestus.ameeting.util.Util;
import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.VersionInfo;
import com.bilal.androidthingscameralib.InitializeCamera;
import com.bilal.androidthingscameralib.OnPictureAvailableListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

//import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.Manifest.permission.CAMERA;

public class Signin extends Activity implements OnPictureAvailableListener {
    /**
     * 有关日志
     * 与
     * 用户指引
     */
    String TAG="Signin";
    private TextView tx_signin_infos;//反馈信息显示

    private MyThread myThred=new MyThread();

    /**
     * 有关摄像头
     */
    Handler handler = new Handler();//用于循环操作
    private InitializeCamera mInitializeCamera;
    private ImageView imageView;//显示抓拍照片
    private Bitmap bitmap;
    private ImageView image_sign_imagestop;//抓拍定格
    boolean state=false;//记录检测状态，要记得处理

    /**
     * 有关引擎使用
     */
    //有关人脸特征提取
    private FaceEngine faceEngine;
    private int faceEngineCode = -1;
    //请求权限的请求吗
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    //所需的所有权限信息
    private static String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //线程启动
        Thread thread=new Thread(myThred);
        thread.start();

        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);//用于隐藏标题栏
        setContentView(R.layout.activity_signin);

//        ActionBar actionBar=getActionBar();//用于隐藏标题栏
//        actionBar.hide();
        imageView=(ImageView)findViewById(R.id.image_sign_image);//用于展示图片
        tx_signin_infos=(TextView)findViewById(R.id.tx_signin_infos) ;//信息反馈显示
        image_sign_imagestop=(ImageView)findViewById(R.id.image_sign_imagestop);//抓拍定格

        /**
         * 摄像头相关使用准备
         */
        // 检查摄像头的使用权限
        if (checkSelfPermission(CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(Signin.this,"相机使用权限未授予！",Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Camera Permission Missing");
            return;
        }
        mInitializeCamera = new InitializeCamera(this, this, 640, 480, 1);
        handler.post(runnable);//开始循环

        /**
         * 有关虹软人脸识别接口的使用准备
         */
        //进行基本权限的检查
        if (!Util.checkPermissions(Signin.this,NEEDED_PERMISSIONS)) {
            Log.d(TAG, "onCreate: 缺少权限"+NEEDED_PERMISSIONS);
        } else {
            activeEngine();//激活引擎
            initEngine();//初始化引擎
        }
    }


    /**
     * 相机相关
     * @param imageBytes
     */
    private void onPictureTaken(final byte[] imageBytes) {
        if (imageBytes != null) {
            Bitmap abitmap=null;
            abitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            imageView.setImageBitmap(abitmap);

            myThred.addBitMap(abitmap);

        }
    }
    @Override
    public void onPictureAvailable(byte[] imageBytes) {
        onPictureTaken(imageBytes);
    }
    //相机的抓拍
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mInitializeCamera.captureImage();
            handler.postDelayed(this, 20);//2秒后再执行本初操作
        }
    };


    /**
     * 引擎相关
     */
   //激活引擎
    public void activeEngine() {
        io.reactivex.Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                FaceEngine faceEngine = new FaceEngine();
                int activeCode = faceEngine.active(Signin.this, Constants.APP_ID, Constants.SDK_KEY);
                emitter.onNext(activeCode);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {
                        if (activeCode == ErrorInfo.MOK) {
                            Log.d("activeEngine", "onNext: active_success!");
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            Log.d("activeEngine", "onNext: already_activated");
                        } else {
                            tx_signin_infos.setText("人脸识别引擎激活失败！");
                            Log.d("activeEngine", "onNext: active_failed"+activeCode);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });


    }

    //初始化引擎
    private void initEngine() {
        //初始化引擎返回90121功能过期原因为Android Things设备时间问题，修正为标准时间即可成功初始化引擎
        //90115 SDK未激活
        faceEngine = new FaceEngine();
        //设置识别上限为1人
        faceEngineCode = faceEngine.init(this, FaceEngine.ASF_DETECT_MODE_IMAGE, FaceEngine.ASF_OP_0_HIGHER_EXT,
                16, 1,
                FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_AGE | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_LIVENESS);
        VersionInfo versionInfo = new VersionInfo();
        faceEngine.getVersion(versionInfo);
        Log.i(TAG, "initEngine: init: " + faceEngineCode + "  version:" + versionInfo);

        if (faceEngineCode != ErrorInfo.MOK) {
            tx_signin_infos.setText("引擎初始化失败，返回错误码为："+faceEngineCode);
            Log.d(TAG, "initEngine: init_failed"+faceEngineCode);
        }
        //如果引擎为空，传递显示一段信息
        if (faceEngine == null) {
            showUserToast(Signin.this,"人脸识别引擎未开启！");
            Log.d(TAG, "getProcessBitmap……faceEngine is null!人脸识别引擎未开启！");
        }
    }
    private void unInitEngine() {
        if (faceEngine != null) {
            faceEngineCode = faceEngine.unInit();
            faceEngine = null;
            Log.i("unInitEngine", "unInitEngine: " + faceEngineCode);
        }
    }

    /**
     * 获取人脸特征之前呢对bitmap进行的处理
     */
    public String getProcessBitmap(Bitmap bitmap){
        //1准备
        String featureByteString = null;//存放人脸特征的byte数组base64加密字符串
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        List<FaceInfo> faceInfoList = new ArrayList<>();//人脸信息

        //确保传给引擎的BGR24数据宽度为4的倍数
        bitmap = Util.alignBitmapForBgr24(bitmap);
        //bitmap转bgr
        byte[] bgr24 = Util.bitmapToBgr(bitmap);//bitmap转化为bgr数据
        //开始检测
        long startTime = System.currentTimeMillis();//检测的起始时间点
        int detectCode = faceEngine.detectFaces(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList);//图片格式选取BGR24
        if (detectCode == ErrorInfo.MOK) {//检测成功
            Log.i(TAG, "1.人脸检测结束，已经耗时 " + (System.currentTimeMillis() - startTime));
        }
        //进行活体检测
        int faceProcessCode = faceEngine.process(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList,
                FaceEngine.ASF_AGE | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_LIVENESS);
        if (faceProcessCode != ErrorInfo.MOK) {
            Log.d(TAG,  "活体检测失败，错误码为："+String.valueOf(faceProcessCode));
        } else {
            Log.i(TAG, "2.活体检测结束，已经耗时 " + (System.currentTimeMillis() - startTime));
        }
        //获取活体结果
        List<LivenessInfo> livenessInfoList = new ArrayList<>();
        int livenessCode = faceEngine.getLiveness(livenessInfoList);
        //活体检测数据，只检测第一个人的
        String liveness = null;
        if (faceInfoList.size()>0){
            if (livenessInfoList.get(0).getLiveness()==LivenessInfo.ALIVE){
                liveness = "ALIVE";
                Log.d(TAG, "getProcessBitmap: 处理结果确认活体！");
            }else{
                Log.d(TAG, "getProcessBitmap: 活体检测结果码为："+livenessInfoList.get(0).getLiveness());
            }
            //活体才能进行如下操作
            if (liveness.equals("ALIVE")){
                FaceFeature faceFeature=new FaceFeature();
                int extractFaceFeatureCode = faceEngine.extractFaceFeature(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList.get(0),faceFeature);
                if (extractFaceFeatureCode == ErrorInfo.MOK) {
                    Log.d(TAG,"人脸特征byte数组length="+faceFeature.getFeatureData().length);
                    //人脸特征byte数组base64加密
                    final String faceFeatureByteBase64 = Base64.encodeToString(faceFeature.getFeatureData(), Base64.NO_WRAP);
                    featureByteString=faceFeatureByteBase64;
                    Log.d(TAG, "人脸特征数据检测成功！ faceFeatureByteBase64"+featureByteString);//base64加密人脸特征数据
                    return featureByteString;//返回数据
                }else{
                    Log.d(TAG, "特征检测失败,"+"错误码为： "+ String.valueOf(extractFaceFeatureCode));
                    return null;
                }
            }else{
                return null;
            }
        }else{
            return null;
        }
    }

    /**
     * 网络请求将活体人脸特征上传
     * @param requestData
     */
    private void getFeedback(final String requestData){
        final String url=HttpUtil.getGetAllMeetingUrl() +"?encryptedString="+requestData+"&macAddress="+AboutMac.getMD5String(AboutMac.getMac());
        Log.d("url", "onCreate: "+url);
        RequestBody requestBody=new FormBody.Builder()
                .add("encryptedString",requestData)
                .add("macAddress",AboutMac.getMD5String(AboutMac.getMac()))
                .build();
        HttpUtil.sendOkHttpRequestWithBody(HttpUtil.getFaceOpenUrl(), requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showUserToast(Signin.this,"服务器故障!");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData=response.body().string();
                Log.d("responseData", "onResponse: "+responseData);
                if (response.code()==200){//如果正常返回信息就继续解析
                    Gson gson=new Gson();
                    FaceOpen faceOpen=gson.fromJson(responseData, FaceOpen.class);
                    if (faceOpen.getCode()==0){
                        myThred.setFinash(true);
                        showUserToast(Signin.this,"开门！！！");
                        finish();
                    }
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        //关闭引擎
        if (faceEngine != null) {
            faceEngineCode = faceEngine.unInit();
            faceEngine = null;
            Log.i("unInitEngine", "unInitEngine: " + faceEngineCode);
        }
        super.onDestroy();
    }

    /**
     * 顶部菜单设置
     * 与点击事件
     * @param menu
     * @return
     */
    //顶部菜单，选择返回或者预定
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.signinmenu,menu);
        return true;
    }
    //顶部菜单点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_signin_back:
//                myThred.clear();
                finish();//返回主页
                break;
            case R.id.item_signin_signin://打开摄像头签到
                Toast.makeText(Signin.this,"开始人脸签到",Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        myThred.setFinash(false);
        Thread thread=new Thread(myThred);
        thread.start();
        super.onResume();
    }

    /**
     * 返回UI线程对用户进行Toast提示
     */
    public void showUserToast(final Context context, final String info){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,info,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private  class MyThread implements Runnable{

        volatile boolean isFinash=false;
        private   Queue<Bitmap> queue=new ConcurrentLinkedQueue<>();
        private Date date=null;

        public void setFinash(boolean finash) {
            isFinash = finash;
        }

        public void addBitMap(Bitmap bitmap){
            if (date!=null){
                Date date=new Date();
                long cha=date.getTime()-this.date.getTime();
                if (cha/1000>=2){
                    Log.d(TAG, "addBitMap: cha"+cha);
                    queue.offer(bitmap);
                }
            }else {
                queue.offer(bitmap);
            }

        }

        public void clear(){
            queue.clear();
        }

        @Override
        public void run() {
            //将获取到的bitmap进行处理，获取人脸特征
                    while (!isFinash){
                        Bitmap bitmap=queue.poll();
                        if (bitmap!=null){
                            //将获取到的bitmap进行处理，获取人脸特征，上传人脸特征
                            String featureByteStringWaitToGetFeedback=getProcessBitmap(bitmap);
                            if (featureByteStringWaitToGetFeedback!=null) {
                                getFeedback(featureByteStringWaitToGetFeedback);//网络请求
                            }
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

        }
    }

}
