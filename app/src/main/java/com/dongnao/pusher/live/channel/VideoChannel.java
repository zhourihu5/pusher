package com.dongnao.pusher.live.channel;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.dongnao.pusher.Constant;
import com.dongnao.pusher.MainActivity;
import com.dongnao.pusher.live.LivePusher;
import com.dongnao.pusher.video.VideoActivity;
import com.dongnao.pusher.video.codec.ISurface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class VideoChannel implements Camera.PreviewCallback, CameraHelper.OnChangedSizeListener, ISurface {


    private LivePusher mLivePusher;
    private CameraHelper cameraHelper;
//    private MediaCodecHelper mediaCodecHelper;
    MediaRecorder mediaRecorder;
    private int mBitrate;
    private int mFps;
    private volatile boolean isLiving;

    public VideoChannel(LivePusher livePusher, Activity activity, int width, int height, int bitrate, int fps, int cameraId) {
        mLivePusher = livePusher;
        mBitrate = bitrate;
        mFps = fps;
        if(cameraId== Camera.CameraInfo.CAMERA_FACING_BACK
                ||
                cameraId== Camera.CameraInfo.CAMERA_FACING_FRONT
        ){
            cameraHelper = new CameraHelper(activity, cameraId, width, height);
            //1、让camerahelper的
            cameraHelper.setPreviewCallback(this);
            //2、回调 真实的摄像头数据宽、高
            cameraHelper.setOnChangedSizeListener(this);
        }else {
//            mediaCodecHelper=new MediaCodecHelper(activity,width,height,bitrate,fps);
//            mediaCodecHelper.setCallBack(this);
//            mediaCodecHelper.initMPManager();
//            mediaCodecHelper.startScreenCapture();
//            File file= new File(activity.getExternalCacheDir(),"a.mp4");
            File file= new File(activity.getExternalFilesDir("video"),"a.mp4");
//            File file= new File(Environment.getExternalStorageDirectory(),"b.mp4");
//            File file= new File("/sdcard/a.mp4");
            String path=file.getAbsolutePath();
            mediaRecorder=new MediaRecorder(activity,path,width,height,bitrate,fps);
            mediaRecorder.setiSurface(this);
            mediaRecorder.setOnRecordFinishListener(new MediaRecorder.OnRecordFinishListener() {
                @Override
                public void onRecordFinish(String path) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            try {
//                                Intent intent = new Intent(Intent.ACTION_VIEW);
//
//                                Uri uri=null;
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//24 android7
//                                    uri = FileProvider.getUriForFile(activity, Constant.FILE_PROVIDER_AUTH, file);
//                                } else {
//                                    uri=Uri.fromFile(file);
//                                }
//                                intent.setDataAndType(uri,"video/mp4");
//                                activity.startActivity(intent);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                //todo
//                            }
                            Intent intent=new Intent(activity, VideoActivity.class);
                            intent.putExtra("path",path);
                            activity.startActivity(intent);
                        }
                    });
                }
            });
            onChanged(height,width);
        }
    }
    public Surface getSurface(){
        if(mediaRecorder!=null){
            return mediaRecorder.getInputSurface();
        }
        return null;
    }

    public void setPreviewDisplay(SurfaceHolder surfaceHolder) {
        if(cameraHelper!=null){
            cameraHelper.setPreviewDisplay(surfaceHolder);
        }
    }

    /**
     * 得到nv21数据 已经旋转好的
     *
     * @param data
     * @param camera
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (isLiving) {
            mLivePusher.native_pushVideo(data);
            Log.e("onPreviewFrame", "onPreviewFrame: "+data);
        }
    }
//    @Override
    public void onPreviewFrame(byte[] data, MediaCodec.BufferInfo mBufferInfo, MediaCodec mEncoder, ByteBuffer outputBuffer, int outputBufferIndex) {
        onPreviewFrame(data,null);
        Log.e("onPreviewFrame", "data.length:"+data.length);
        printHexString("onPreviewFrame",data);
    }
    /**
     * 将指定byte数组以16进制的形式打印到控制台
     *
     * @param hint
     *            String
     * @param b
     *            byte[]
     * @return void
     */
    public static void printHexString(String hint, byte[] b)
    {
        System.out.print(hint);
        for (int i = 0; i < b.length; i++)
        {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1)
            {
                hex = '0' + hex;
            }
            System.out.print(hex.toUpperCase() + " ");
        }
        System.out.println("");
    }

    public void switchCamera() {
        if(cameraHelper!=null){
            cameraHelper.switchCamera();
        }
    }

    /**
     * 真实摄像头数据的宽、高
     * @param w
     * @param h
     */
    @Override
    public void onChanged(int w, int h) {
        //初始化编码器
        mLivePusher.native_setVideoEncInfo(w, h, mFps, mBitrate);
    }

    public void startLive() {
        isLiving = true;
        if(mediaRecorder!=null){
            try {
                mediaRecorder.start(1.0f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopLive() {
        isLiving = false;
        if(mediaRecorder!=null){
            mediaRecorder.stop();
        }
    }

    public void release() {
        if(cameraHelper!=null){
            cameraHelper.release();
        }
        if(isLiving){
            stopLive();
        }
    }

    byte[]outData;
    @Override
    public void offer(byte[] data) {
        outData=data;
        onPreviewFrame(data,null);
    }

    @Override
    public byte[] poll() {
        return outData;
    }

    @Override
    public void setVideoParamerters(int width, int height, int fps) {
        onChanged(width,height);
    }
}
