package com.dongnao.pusher;

import android.Manifest;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Camera;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.TextView;

import com.dongnao.pusher.live.LivePusher;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

public class MediaProjectionActivity extends AppCompatActivity {

    private static final String TAG = "MediaProjectionActivity";
    private static final int REQUEST_CODE_A = 1;
    private LivePusher livePusher;
    TextView textView;
    AtomicInteger count=new AtomicInteger(0);
    Handler handler;
     final int CHANGE_TEXT=1;
     volatile boolean stop=true;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private int width=480;
    private int height=800;
    private Surface mSurface;

    private static MediaProjectionActivity instance;

    public static MediaProjectionActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance=this;
        setContentView(R.layout.activity_media_projection);
        textView=findViewById(R.id.textView);
        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                changeText();
            }
        };
        handler.sendEmptyMessageDelayed(CHANGE_TEXT,1000);
        requestPermision();
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
    }

    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
    };
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;
    public void requestPermision() {
        Log.e(TAG,"requestPermision");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
                return;
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
                return;
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
                return;
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            boolean granted=true;
            for (int i = 0; i < permissions.length; i++) {
                Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" +
                        grantResults[i]);
                if(grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                    granted=false;
                    break;
                }
            }
            if(granted){
            }
        }
    }


    /**
     * 开始截屏
     * **/
    public void startScreenCapture(){
        Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, REQUEST_CODE_A);
    }
    @Override
    protected void onPause() {
        super.onPause();
//        if(stop){
//            stopLive(textView);
//        }
    }
    public void stopLive(View view) {
        try {
            stop=true;
            ScreenRecorder.getInstance().handler.sendEmptyMessage(ScreenRecorder.MSG_STOP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if(!stop){
//            startLive(textView);
//        }
    }
     void changeText(){
        textView.setText("count:"+count.incrementAndGet());
        if(!stop){
            handler.sendEmptyMessageDelayed(CHANGE_TEXT,1000);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode==RESULT_OK&&requestCode==REQUEST_CODE_A){
            int dpi=getResources().getDisplayMetrics().densityDpi;
//            int dpi=getResources().getDisplayMetrics().density;
            Intent service = new Intent(this, ScreenRecorder.class);
            service.putExtra("code", resultCode);
            service.putExtra("data", data);
            service.putExtra("width",width);
            service.putExtra("height",height);
            service.putExtra("dpi",dpi);
            stop=false;
            changeText();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(service);
            }else {
                startService(service);
            }


//            mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
//            mSurface=livePusher.getSurface();
//            mVirtualDisplay =mMediaProjection.createVirtualDisplay(TAG + "-display", width, height,
//                    dpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
//                    mSurface, null, null);
//            startLive();
        }
    }


    public void startLive(View view) {
        startScreenCapture();

    }

}