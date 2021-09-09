package com.dongnao.pusher;

public class Constant {

    public static final String MIME_TYPE = "video/avc";
    public static final int VIDEO_WIDTH = 800;
    public static final int VIDEO_HEIGHT = 480;
//    public static final int VIDEO_BITRATE = 800_000;
//    对于比特率，其实完全可以这样处理，N*width*height，N可设置为1 2 3或者1 3 5等，来区分低/中/高的码率
    public static final int VIDEO_BITRATE = VIDEO_WIDTH*VIDEO_HEIGHT*5;
    public static final int VIDEO_FRAMERATE = 30;
    public static final int VIDEO_IFRAME_INTER =1;
    public static final String FILE_PROVIDER_AUTH = "com.dongnao.pusher.fileprovider";
//    public static final int VIDEO_DPI = 0;
}
