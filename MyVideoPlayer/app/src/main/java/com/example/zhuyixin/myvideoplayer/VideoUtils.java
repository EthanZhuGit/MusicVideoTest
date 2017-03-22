
package com.example.zhuyixin.myvideoplayer;

import android.content.Context;
import android.util.Log;
//import android.media.MediaFile;

import java.io.File;
import java.util.Formatter;
import java.util.Locale;

public class VideoUtils {

    private static final String TAG ="TAG "+  VideoUtils.class.getSimpleName();
    
    private static StringBuilder sFormatBuilder = new StringBuilder();
    private static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
    private static final Object[] sTimeArgs = new Object[5];

    public static String makeTimeString(Context context, long secs) {
        if (context!=null) {
            String durationformat = context
                    .getString(secs < 3600 ? R.string.durationformatshort
                            : R.string.durationformatlong);

            sFormatBuilder.setLength(0);

            final Object[] timeArgs = sTimeArgs;
            timeArgs[0] = secs / 3600;
            timeArgs[1] = secs / 60;
            timeArgs[2] = (secs / 60) % 60;
            timeArgs[3] = secs;
            timeArgs[4] = secs % 60;

            return sFormatter.format(durationformat, timeArgs).toString();
        }else {
            Log.d(TAG, "makeTimeString: " + "context is null");
            return "";
        }
    }
    
//    public static  boolean isVideoFile(File path) {
//        String s = path.getPath();
//        MediaFile.MediaFileType t = MediaFile.getFileType(s);
//        return t != null && MediaFile.isVideoFileType(t.fileType);
//    }

    public static void autoMute() {
        // McuManager mcu = McuManager.getInstance();
        // Device device = mcu.getDeviceInstance();
        // device.enableMute((byte)Device.DEVICE_MUTE_TYPE_OTHER,
        // (byte)Device.DEVICE_MUTE_CHANNEL_FRONT, true);
    }

    public static boolean isVideoFile(File file) {
        String name=file.getName().toLowerCase();
        String type = name.substring(name.lastIndexOf(".") + 1);
        return type.equals("mp4") || type.equals("avi") || type.equals("rmvb") || type.equals("mkv");
    }
}
