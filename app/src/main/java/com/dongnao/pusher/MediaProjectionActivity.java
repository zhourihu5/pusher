package com.dongnao.pusher;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.Intent;
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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        initMPManager();
//        startScreenCapture();
    }

    /**
     * 初始化MediaProjectionManager
     * **/
    public void initMPManager(){
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Activity.MEDIA_PROJECTION_SERVICE);
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
        if(livePusher!=null){
            livePusher.stopLive();
        }
        if(mVirtualDisplay!=null){
            mVirtualDisplay.release();    // 结束录屏之后将画布和录屏管理器设置为空
        }
        if(mMediaProjection!=null){
            mMediaProjection.stop();
        }
        stop=true;
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
            mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
            startLive();
            mSurface=livePusher.getSurface();
            mVirtualDisplay =mMediaProjection.createVirtualDisplay(TAG + "-display", width, height,
                    dpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    mSurface, null, null);

        }
    }

    private void initLivePusher() {
        if(livePusher==null){
            livePusher = new LivePusher(this, 800, 480, 800_000, 10,
                    Camera.CameraInfo.CAMERA_FACING_BACK+Camera.CameraInfo.CAMERA_FACING_FRONT+5);
        }

    }

    public void startLive(View view) {
        startScreenCapture();
        startLive();
    }

    private void startLive() {
        initLivePusher();
        if(livePusher!=null){
//        livePusher.startLive("rtmp://47.75.90.219/myapp/mystream");
            livePusher.startLive("rtmp://192.188.0.116/live/mystream");
        }
        stop=false;
        changeText();
    }
}