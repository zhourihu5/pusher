package com.dongnao.pusher;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import com.dongnao.pusher.live.LivePusher;

public class CameraActivity extends AppCompatActivity {

    static final String TAG="MainActivity";
    private LivePusher livePusher;
    SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        surfaceView = findViewById(R.id.surfaceView);
        requestPermision();

//        createLivePusher(surfaceView);
    }

    private void createLivePusher(SurfaceView surfaceView) {
        if(livePusher==null){
            livePusher = new LivePusher(this, 800, 480, 800_000, 10, Camera.CameraInfo.CAMERA_FACING_BACK);
            //  设置摄像头预览的界面
            livePusher.setPreviewDisplay(surfaceView.getHolder());
        }
        Log.e(TAG,"createLivePusher livePusher="+livePusher);
    }

    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;


    public void requestPermision() {
        Log.e(TAG,"requestPermision");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
           if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
                return;
            }
            else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
                return;
            }
        }
        createLivePusher(surfaceView);
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
                createLivePusher(surfaceView);
            }
        }
    }


    public void switchCamera(View view) {
        requestPermision();
        if(livePusher!=null){
            livePusher.switchCamera();
        }
    }

    public void startLive(View view) {
        requestPermision();
        if(livePusher!=null){
//        livePusher.startLive("rtmp://47.75.90.219/myapp/mystream");
            livePusher.startLive("rtmp://192.188.0.116/live/mystream");
            isStart=true;
        }
    }
    volatile boolean isStart;
    @Override
    protected void onPause() {
        super.onPause();
//        if(!isStart){
            stopLive(surfaceView);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if(isStart){
            startLive(surfaceView);
//        }
    }

    public void stopLive(View view) {
        if(livePusher!=null){
            livePusher.stopLive();
        }
        isStart=false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(livePusher!=null){
            livePusher.release();
        }
    }
}
