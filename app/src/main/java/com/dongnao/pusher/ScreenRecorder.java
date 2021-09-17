package com.dongnao.pusher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import com.dongnao.pusher.live.LivePusher;

import java.util.Objects;

public class ScreenRecorder extends Service {

    private int mResultCode;
    private Intent mResultData;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;

    private static ScreenRecorder instance;
    private Surface mSurface;
    private VirtualDisplay mVirtualDisplay;
    private volatile boolean stop;

    public static ScreenRecorder getInstance() {
        return instance;
    }
    public static final int MSG_START=1;
    public static final int MSG_STOP=2;
    public Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_START:
                    startLive();
                    break;
                case MSG_STOP:
                    stopLive();
                    break;

            }
        }
    };

    private LivePusher livePusher;
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        initLivePusher();
        instance=this;
        createNotificationChannel();
        mResultCode = intent.getIntExtra("code", -1);
        mResultData = intent.getParcelableExtra("data");
        //mResultData = intent.getSelector();
         mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode,mResultData);
        mSurface=livePusher.getSurface();
        int width=intent.getIntExtra("width",480);
        int height=intent.getIntExtra("height",800);
        int dpi=intent.getIntExtra("dpi",160);
        mVirtualDisplay =mMediaProjection.createVirtualDisplay( "ScreenRecorder-display", width, height,
                dpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                mSurface, null, null);
        startLive();
        return super.onStartCommand(intent, flags, startId);
    }
    public void stopLive() {
        if(livePusher!=null){
            livePusher.stopLive();
//            livePusher.release();
            livePusher=null;
        }
        if(mVirtualDisplay!=null){
            mVirtualDisplay.release();    // 结束录屏之后将画布和录屏管理器设置为空
        }
        if(mMediaProjection!=null){
            mMediaProjection.stop();
        }
        stop=true;
    }
    private void initLivePusher() {
        if(livePusher==null){
            livePusher = new LivePusher(MediaProjectionActivity.getInstance(), 800, 480, 800_000, 10,
                    Camera.CameraInfo.CAMERA_FACING_BACK+Camera.CameraInfo.CAMERA_FACING_FRONT+5);
        }

    }

    private void startLive() {
        initLivePusher();
        if(livePusher!=null){
//        livePusher.startLive("rtmp://47.75.90.219/myapp/mystream");
            livePusher.startLive("rtmp://192.188.0.116/live/mystream");
        }
        stop=false;
    }
    private void createNotificationChannel() {
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
        Intent nfIntent = new Intent(this, MainActivity.class); //点击后跳转的界面，可以设置跳转数据

        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher)) // 设置下拉列表中的图标(大图标)
                //.setContentTitle("SMI InstantView") // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                .setContentText("is running......") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

        /*以下是对Android 8.0的适配*/
        //普通notification适配
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("notification_id");
        }
        //前台服务notification适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("notification_id", "notification_name", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        startForeground(110, notification);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
