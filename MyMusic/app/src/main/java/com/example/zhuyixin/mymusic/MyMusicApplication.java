package com.example.zhuyixin.mymusic;

import android.app.Application;
import android.media.AudioManager;
import android.util.Log;


public class MyMusicApplication extends Application {
    private static final String TAG = "TAG" + "MyMusicApplication";
    private AudioManager mAudioManager;
    private IAudioFocusListener mMyAudioFocusListener;
    private boolean mHasAbandon=true;

    @Override
    public void onCreate() {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        Log.d(TAG, "onCreate: ");

    }

    public void registerAudioFocusListener(IAudioFocusListener listener){
        this.mMyAudioFocusListener=listener;
    }


    public boolean requestAudioFocus(boolean oneshot) {
        boolean success=false;
        if (oneshot) {
            success = AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                    mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.USE_DEFAULT_STREAM_TYPE,
                            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }else {
            success = AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.USE_DEFAULT_STREAM_TYPE, AudioManager.AUDIOFOCUS_GAIN);
        }

        if (success) {
            mHasAbandon=false;
        }
        return success;
    }

    public AudioManager.OnAudioFocusChangeListener mAudioFocusListener=new AudioManager.OnAudioFocusChangeListener() {


        @Override
        public void onAudioFocusChange(int i) {
            switch (i) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.d(TAG, "onAudioFocusChange: " + "loss");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.d(TAG, "onAudioFocusChange: " + "loss transient");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.d(TAG, "onAudioFocusChange: " + "loss tran can duck");
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.d(TAG, "onAudioFocusChange: " + "gain");
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                    Log.d(TAG, "onAudioFocusChange: " + "gain tran");
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
                    Log.d(TAG, "onAudioFocusChange: " + "gain tran exc");
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    Log.d(TAG, "onAudioFocusChange: " + "gain tran may duck");
                    break;
            }
            if (mMyAudioFocusListener != null) {
                mMyAudioFocusListener.onAudioFocusChange(i);
            }

        }
    };


    public interface IAudioFocusListener{
        public void onAudioFocusChange(int focusChange);
    }

    public void abandonAudioFocus() {
        mHasAbandon = true;
        mAudioManager.abandonAudioFocus(mAudioFocusListener);
    }

    public boolean hasAbandon() {
        return mHasAbandon;
    }


}
