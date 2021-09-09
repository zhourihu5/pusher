package com.dongnao.pusher.live.channel;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.dongnao.pusher.Constant;
import com.dongnao.pusher.MainActivity;
import com.dongnao.pusher.live.LivePusher;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class VideoChannel implements Camera.PreviewCallback, CameraHelper.OnChangedSizeListener, MediaRecorder.CallBack {


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
            File file= new File(activity.getExternalCacheDir(),"a.mp4");
            String path=file.getAbsolutePath();
            mediaRecorder=new MediaRecorder(activity,path,width,height,bitrate,fps);
            mediaRecorder.setCallBack(this);
            mediaRecorder.setOnRecordFinishListener(new MediaRecorder.OnRecordFinishListener() {
                @Override
                public void onRecordFinish(String path) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW);

                                Uri uri=null;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//24 android7
                                    uri = FileProvider.getUriForFile(activity, Constant.FILE_PROVIDER_AUTH, file);
                                } else {
                                    uri=Uri.fromFile(file);
                                }
                                intent.setDataAndType(uri,"video/mp4");
                                activity.startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                                //todo
                            }
                        }
                    });
                }
            });
            try {
                mediaRecorder.start(1.0f);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            mediaRecorder.encodeFrame(1,System.currentTimeMillis());
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
//    public void onActivityResult(int requestCode, int resultCode, Intent data){
//        if(mediaCodecHelper!=null){
//            mediaCodecHelper.onActivityResult(requestCode,resultCode,data);
//        }
//    }

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
    @Override
    public void onPreviewFrame(byte[] data, MediaCodec.BufferInfo mBufferInfo, MediaCodec mEncoder, ByteBuffer outputBuffer, int outputBufferIndex) {
        Log.e("onPreviewFrame", Arrays.toString(data));
        if (isLiving) {
            mLivePusher.native_pushVideo(data);
        }
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
            mediaRecorder.encodeFrame(1,System.currentTimeMillis());
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
        if(mediaRecorder!=null){
            mediaRecorder.stop();
        }
    }


}
