package com.accesscontrol.hephaestus.ameeting.activity;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.ParcelableSpan;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.accesscontrol.hephaestus.ameeting.data.Constants;
import com.accesscontrol.hephaestus.ameeting.R;
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
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public class Signin extends Activity{
    String TAG="Signin";
    private ImageView imageView;//显示抓拍照片
    private TextView tx_signin_infos;//反馈信息显示

    //有关人脸特征提取
    private FaceEngine faceEngine;
    private int faceEngineCode = -1;
    //请求权限的请求吗
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    //所需的所有权限信息
    private static String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE
    };

    private Bitmap bitmap;

    private CameraManager mCameraManager;//摄像头管理器
    private Handler childHandler;
    private String mCameraID;//摄像头Id 0 为后  1 为前
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;

    //为了使照片竖直显示
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    //该类用于接收相机的连接状态的更新
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            //打开摄像头
            mCameraDevice = camera;
            //拍照
            takePicture();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            //关闭摄像头
            if (null != mCameraDevice) {
                Log.d(TAG, "onDisconnected: onDisconnected");
                mCameraDevice.close();
                mImageReader.close();
            }
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraDevice.close();//!
            mCameraDevice = null;
            //有错误
//            MyUtil.setToast(MainActivity.this, "摄像头开启失败", false);
            Toast.makeText(Signin.this,"摄像头开启失败"+"错误码"+error,Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);//用于隐藏标题栏
        setContentView(R.layout.activity_signin);

//        ActionBar actionBar=getActionBar();
//        actionBar.hide();
        imageView=(ImageView)findViewById(R.id.image_sign_image);//用于展示图片
        tx_signin_infos=(TextView)findViewById(R.id.tx_signin_infos) ;//信息反馈显示

        //进行基本权限的检查
        if (!Util.checkPermissions(Signin.this,NEEDED_PERMISSIONS)) {
            Log.d(TAG, "onCreate: 缺少权限"+NEEDED_PERMISSIONS);
        } else {
            activeEngine();//激活引擎
            initEngine();//初始化引擎
        }
    }

    private void handThread(){
        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        childHandler = new Handler(handlerThread.getLooper());
    }
    private void readyWork(){//有关摄像头的设备设置与权限
        if (mCameraManager == null) {
            //获取摄像头的管理者
            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        }
        String cameraIds[] = {};
        try {
            cameraIds = mCameraManager.getCameraIdList();//获取摄像头设备列表
            Log.d(TAG, "openCamera: "+cameraIds[0]);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Cam access exception getting IDs", e);
        }
        if (cameraIds.length < 1) {
            Log.e(TAG, "No cameras found");
            return;
        }
        mCameraID = cameraIds[0];
        try {
            //进行权限确认
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(Signin.this,"没有相机权限",Toast.LENGTH_SHORT).show();
                Log.d(TAG, "openCamera: 没有相机权限");
                return;
            }
            ////打开相机，第一个参数指示打开哪个摄像头，第二个参数stateCallback为相机的状态回调接口，
            // 第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            mCameraManager.openCamera(mCameraID, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void openCamera() {
        handThread();//关于相机线程
        readyWork();
        mImageReader = ImageReader.newInstance(imageView.getWidth(), imageView.getHeight(), ImageFormat.JPEG, 1);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {//处理临时照片
            @Override
            public void onImageAvailable(ImageReader reader) {

                mCameraDevice.close();//将相机关闭
                mCameraDevice = null;
                // 拿到拍照照片数据
                Image image = reader.acquireNextImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);//由缓冲区存入字节数组
                bitmap= BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                final Bitmap bitmap = Bitmap.createScaledBitmap(bitmap, 10, 10, true);
                //展示图片之间已经可以对图片进行属性检测
                //如果检测不通过则继续拍照
//                byte[] requestBytes=getProcessBitmap(bitmap);//图片处理得到的字节数组
//                if (requestBytes!=null){
//                    byte[] data = Base64.encode(requestBytes,0,requestBytes.length,0);
//                    getFeedback(data.toString());
//                }
                if (bitmap != null) {//在主线程中展示照片
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bitmap);
                            Log.d(TAG, "onImageAvailable: 设置进imageview");
//                            openCamera();
                        }
                    });
//                    getFeedback(requestData);//网络请求
                }
            }
        }, childHandler);


    }



    private void takePicture(){
        try {
            Log.d(TAG, "takePicture: 已经打开摄像头");
            final CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            builder.addTarget(mImageReader.getSurface());
            // 自动对焦
//            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            //   打开闪光灯
//            builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);//注意下
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            // 根据设备方向计算设置照片的方向
            builder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            //有关创建一个捕获会话，CameraCaptureSession 是一个事务，用来向相机设备发送获取图像的请求。
            mCameraDevice.createCaptureSession(Arrays.asList(mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull final CameraCaptureSession session) {
                    if (mCameraDevice == null)
                        return;
                    try {
                        CaptureRequest mCaptureRequest = builder.build();
                        session.capture(mCaptureRequest, null, childHandler);
                        Log.d(TAG, "onConfigured: 已经创建了一个捕获会话。");

                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                        Log.d(TAG, "onConfigured: CameraAccessException");
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(Signin.this,"配置错误",Toast.LENGTH_SHORT).show();
                }
            },childHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }





    /**
     * 激活引擎
     */
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
                            tx_signin_infos.setText("引擎激活失败！");
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

    /*
    初始化引擎
     */
    private void initEngine() {
        //初始化引擎返回90121功能过期原因为Android Things设备时间问题，修正为标准时间即可成功初始化引擎
        faceEngine = new FaceEngine();
        faceEngineCode = faceEngine.init(this, FaceEngine.ASF_DETECT_MODE_IMAGE, FaceEngine.ASF_OP_0_HIGHER_EXT,
                16, 10, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_AGE | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_LIVENESS);
        VersionInfo versionInfo = new VersionInfo();
        faceEngine.getVersion(versionInfo);
        Log.i("initEngine", "initEngine: init: " + faceEngineCode + "  version:" + versionInfo);

        if (faceEngineCode != ErrorInfo.MOK) {
            tx_signin_infos.setText("引擎初始化失败，返回错误码为："+faceEngineCode);
            Log.d("faceEngineCode", "initEngine: init_failed");
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
     *有关提示信息
     * @param stringBuilder 提示的字符串的存放对象
     * @param styleSpan     添加的字符串的格式
     * @param strings       字符串数组
     */
    private void addNotificationInfo(SpannableStringBuilder stringBuilder, ParcelableSpan styleSpan, String... strings) {
        if (stringBuilder == null || strings == null || strings.length == 0) {
            return;
        }
        int startLength = stringBuilder.length();
        for (String string : strings) {
            stringBuilder.append(string);
        }
        int endLength = stringBuilder.length();
        if (styleSpan != null) {
            stringBuilder.setSpan(styleSpan, startLength, endLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /*
    在获取人脸特征之前呢对bitmap进行的处理
     */
    public byte[] getProcessBitmap(Bitmap bitmap){
        //1
        byte[] featureByte = new byte[0];//存放人脸特征的byte数组

        final SpannableStringBuilder notificationSpannableStringBuilder = new SpannableStringBuilder();//与文本样式有关
        if (faceEngineCode != ErrorInfo.MOK) {
            addNotificationInfo(notificationSpannableStringBuilder, null, " face engine not initialized!");
            Log.d(TAG, "getProcessBitmap……引擎初始化失败!");
            return null;
        }
        //如果引擎为空，传递显示一段信息
        if (faceEngine == null) {
            addNotificationInfo(notificationSpannableStringBuilder, null, " faceEngine is null!");
            Log.d(TAG, "getProcessBitmap……faceEngine is null!");
            return null;
        }

        //2确保传给引擎的BGR24数据宽度为4的倍数
        bitmap = Util.alignBitmapForBgr24(bitmap);
        //如果bitmap为空，则反馈消息
        if (bitmap == null) {
            addNotificationInfo(notificationSpannableStringBuilder, null, " bitmap is null!");
            Log.d(TAG, "getalignBitmapForBgr24……bitmap is null! ");
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        //bitmap转bgr
        byte[] bgr24 = Util.bitmapToBgr(bitmap);//bitmap转化为bgr数据
        addNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "start face detection,imageWidth is " + width + ", imageHeight is " + height + "\n");
        //如果bgr数据为空，反馈信息
        if (bgr24 == null) {
            addNotificationInfo(notificationSpannableStringBuilder, new ForegroundColorSpan(Color.RED), "can not get bgr24 data of bitmap!\n");
            Log.d(TAG, "getbitmapToBgr……can not get bgr24 data of bitmap!");
            return null;
        }

        List<FaceInfo> faceInfoList = new ArrayList<>();

        /**
         * 成功获取到了BGR24 数据，开始人脸检测
         */
        long fdStartTime = System.currentTimeMillis();
        int detectCode = faceEngine.detectFaces(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList);
        if (detectCode == ErrorInfo.MOK) {
            Log.i(TAG, "开始人脸检测时间点1……processImage: fd costTime = " + (System.currentTimeMillis() - fdStartTime));
        }


        /**
         * 若检测结果人脸数量大于0，若人脸数量为0，则无法进行下一步操作，操作结束
         */
        if (faceInfoList.size() > 0) {
            addNotificationInfo(notificationSpannableStringBuilder, null, "face list:\n");
            for (int i = 0; i < faceInfoList.size(); i++) {
                addNotificationInfo(notificationSpannableStringBuilder, null, "face[", String.valueOf(i), "]:", faceInfoList.get(i).toString(), "\n");
                Log.d(TAG, "人脸数量大于零 face["+String.valueOf(i)+"]:"+faceInfoList.get(i).toString());
            }
        } else {
            addNotificationInfo(notificationSpannableStringBuilder, null, "can not do further action, exit!");
            Toast.makeText(Signin.this,"未检测到人脸！",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "操作结束can not do further action, exit!");
            return null;
        }
        addNotificationInfo(notificationSpannableStringBuilder, null, "\n");
        /**
         * 上一步已获取到人脸位置和角度信息，传入给process函数，进行年龄、性别、三维角度检测
         */
        long processStartTime = System.currentTimeMillis();
        int faceProcessCode = faceEngine.process(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList, FaceEngine.ASF_AGE | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_LIVENESS);

        if (faceProcessCode != ErrorInfo.MOK) {
            addNotificationInfo(notificationSpannableStringBuilder, new ForegroundColorSpan(Color.RED), "process failed! code is ", String.valueOf(faceProcessCode), "\n");
            Log.d("",  "process failed! code is "+String.valueOf(faceProcessCode));
        } else {
            Log.i("consumetime2", "processImage: process costTime = " + (System.currentTimeMillis() - processStartTime));
        }
        //年龄信息结果
        List<AgeInfo> ageInfoList = new ArrayList<>();
        //性别信息结果
        List<GenderInfo> genderInfoList = new ArrayList<>();
        //人脸三维角度结果
        List<Face3DAngle> face3DAngleList = new ArrayList<>();
        //活体检测结果
        List<LivenessInfo> livenessInfoList = new ArrayList<>();
        //获取年龄、性别、三维角度、活体结果
        int ageCode = faceEngine.getAge(ageInfoList);
        int genderCode = faceEngine.getGender(genderInfoList);
        int face3DAngleCode = faceEngine.getFace3DAngle(face3DAngleList);
        int livenessCode = faceEngine.getLiveness(livenessInfoList);

        if ((ageCode | genderCode | face3DAngleCode | livenessCode) != ErrorInfo.MOK) {
            addNotificationInfo(notificationSpannableStringBuilder, null, "at least one of age,gender,face3DAngle detect failed!,codes are:",
                    String.valueOf(ageCode), " , ", String.valueOf(genderCode), " , ", String.valueOf(face3DAngleCode));
            Log.d(TAG, "年龄、性别与三维角度检测：at least one of age,gender,face3DAngle detect failed!,codes are:"+
                    String.valueOf(ageCode)+","+String.valueOf(genderCode)+ " , "+String.valueOf(face3DAngleCode));

            return null;
        }
        /**
         * 年龄、性别、三维角度已获取成功，添加信息到提示文字中
         */
        //年龄数据
        if (ageInfoList.size() > 0) {
            addNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "age of each face:\n");
        }
        for (int i = 0; i < ageInfoList.size(); i++) {
            addNotificationInfo(notificationSpannableStringBuilder, null, "face[", String.valueOf(i), "]:", String.valueOf(ageInfoList.get(i).getAge()), "\n");
        }
        addNotificationInfo(notificationSpannableStringBuilder, null, "\n");

        //性别数据
        if (genderInfoList.size() > 0) {
            addNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "gender of each face:\n");
        }
        for (int i = 0; i < genderInfoList.size(); i++) {
            addNotificationInfo(notificationSpannableStringBuilder, null, "face[", String.valueOf(i), "]:"
                    , genderInfoList.get(i).getGender() == GenderInfo.MALE ?
                            "MALE" : (genderInfoList.get(i).getGender() == GenderInfo.FEMALE ? "FEMALE" : "UNKNOWN"), "\n");
        }
        addNotificationInfo(notificationSpannableStringBuilder, null, "\n");


        //人脸三维角度数据
        if (face3DAngleList.size() > 0) {
            addNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "face3DAngle of each face:\n");
            for (int i = 0; i < face3DAngleList.size(); i++) {
                addNotificationInfo(notificationSpannableStringBuilder, null, "face[", String.valueOf(i), "]:", face3DAngleList.get(i).toString(), "\n");
            }
        }
        addNotificationInfo(notificationSpannableStringBuilder, null, "\n");

        //活体检测数据
        if (livenessInfoList.size() > 0) {
            addNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "liveness of each face:\n");
            for (int i = 0; i < livenessInfoList.size(); i++) {
                String liveness = null;
                switch (livenessInfoList.get(i).getLiveness()) {
                    case LivenessInfo.ALIVE:
                        liveness = "ALIVE";
                        break;
                    case LivenessInfo.NOT_ALIVE:
                        liveness = "NOT_ALIVE";
                        break;
                    case LivenessInfo.UNKNOWN:
                        liveness = "UNKNOWN";
                        break;
                    case LivenessInfo.FACE_NUM_MORE_THAN_ONE:
                        liveness = "FACE_NUM_MORE_THAN_ONE";
                        break;
                    default:
                        liveness = "UNKNOWN";
                        break;
                }
                addNotificationInfo(notificationSpannableStringBuilder, null, "face[", String.valueOf(i), "]:", liveness, "\n");
            }
        }
        addNotificationInfo(notificationSpannableStringBuilder, null, "\n");

        /**
         * 最后将图片内的所有人脸进行一一比对并添加到提示文字中
         */
        if (faceInfoList.size() > 0) {
            FaceFeature[] faceFeatures = new FaceFeature[faceInfoList.size()];
            int[] extractFaceFeatureCodes = new int[faceInfoList.size()];

            addNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "faceFeatureExtract:\n");
            for (int i = 0; i < faceInfoList.size(); i++) {
                faceFeatures[i] = new FaceFeature();
                //从图片解析出人脸特征数据
                long frStartTime = System.currentTimeMillis();
                extractFaceFeatureCodes[i] = faceEngine.extractFaceFeature(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList.get(i), faceFeatures[i]);

                if (extractFaceFeatureCodes[i] != ErrorInfo.MOK) {
                    Toast.makeText(Signin.this,"检测失败！请正确进行检测…",Toast.LENGTH_SHORT).show();
                    addNotificationInfo(notificationSpannableStringBuilder, null, "faceFeature of face[", String.valueOf(i), "]",
                            " extract failed, code is ", String.valueOf(extractFaceFeatureCodes[i]), "\n");
                } else {
//                    Log.i(TAG, "processImage: fr costTime = " + (System.currentTimeMillis() - frStartTime));
                    addNotificationInfo(notificationSpannableStringBuilder, null, "faceFeature of face[", String.valueOf(i), "]",
                            " extract success\n");
                }
            }
            addNotificationInfo(notificationSpannableStringBuilder, null, "\n");

            //将人脸特征数据转换成byte数组，目前只取一个人脸
            featureByte=faceFeatures[0].getFeatureData();
            
            //人脸特征的数量大于1，将所有特征进行比较
            if (faceFeatures.length >= 1) {
                addNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "similar of faces:\n");
                for (int i = 0; i < faceFeatures.length; i++) {
                    for (int j = i ; j < faceFeatures.length; j++) {
                        addNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD_ITALIC), "compare face[", String.valueOf(i), "] and  face["
                                , String.valueOf(j), "]:\n");//对比的对象改为了同一个人脸信息
                        //若其中一个特征提取失败，则不进行比对
                        boolean canCompare = true;
                        if (extractFaceFeatureCodes[i] != 0) {
                            addNotificationInfo(notificationSpannableStringBuilder, null, "faceFeature of face[", String.valueOf(i), "] extract failed, can not compare!\n");
                            canCompare = false;
                        }
                        if (extractFaceFeatureCodes[j] != 0) {
                            addNotificationInfo(notificationSpannableStringBuilder, null, "faceFeature of face[", String.valueOf(j), "] extract failed, can not compare!\n");
                            canCompare = false;
                        }
                        if (!canCompare) {
                            continue;
                        }

                        FaceSimilar matching = new FaceSimilar();
                        //比对两个人脸特征获取相似度信息
                        faceEngine.compareFaceFeature(faceFeatures[i], faceFeatures[j], matching);
                        //新增相似度比对结果信息
                        addNotificationInfo(notificationSpannableStringBuilder, null, "similar of face[", String.valueOf(i), "] and  face[",
                                String.valueOf(j), "] is:", String.valueOf(matching.getScore()), "\n");
                    }
                }
            }
        }
        Log.d(TAG, "getProcessBitmap: 所有信息"+notificationSpannableStringBuilder);
        return featureByte;
    }

    private void getFeedback(String requestData){
        final String url=HttpUtil.getGetAllMeetingUrl() +"?encryptedString="+requestData+"&macAddress="+AboutMac.getMD5String(AboutMac.getMac());
        Log.d("url", "onCreate: "+url);
        RequestBody requestBody=new FormBody.Builder()
                .add("encryptedString",requestData)
                .add("macAddress",AboutMac.getMD5String(AboutMac.getMac()))

                .build();
        HttpUtil.sendOkHttpRequestWithBody(HttpUtil.getFaceOpenUrl(), requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Signin.this,"服务器故障！",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });

    }

    @Override
    protected void onDestroy() {
        mCameraDevice.close();//将相机关闭
        mCameraDevice = null;
        //关闭引擎
        if (faceEngine != null) {
            faceEngineCode = faceEngine.unInit();
            faceEngine = null;
            Log.i("unInitEngine", "unInitEngine: " + faceEngineCode);
        }
        super.onDestroy();
    }

    /**
     * 顶部菜单
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
                finish();//返回主页
                break;
            case R.id.item_signin_signin://打开摄像头签到
//                handThread();
//                readyWork();
                openCamera();
                break;
        }
        return true;
    }

}
