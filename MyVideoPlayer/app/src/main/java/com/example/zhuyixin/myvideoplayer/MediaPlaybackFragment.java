package com.example.zhuyixin.myvideoplayer;

import android.app.AlertDialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;


public class MediaPlaybackFragment extends Fragment implements View.OnClickListener,MediaPlayer.OnPreparedListener,View.OnTouchListener,MediaPlayer.OnCompletionListener{

    private static final String TAG ="TAG"+"MediaPlaybackFrag";

    private static final int MSG_UPDATE_PROGRESS = 0x01;
    private static final int MSG_NOTIFY_POSITION = 0x02;
    private static final int MSG_FADE_OUT        = 0x03;
    private static final int MSG_BRAKE_ON        = 0x04;
    private static final int MSG_BRAKE_OFF       = 0x05;
    private static final int MSG_NOTIFY_MEDIA_INFO = 0x06;
    private static final int MSG_UPDATE_PLAY_STATE = 0x07;
    public static final int DEFAULT_TIMEOUT        = 5000;



    private CustomVideoView mVideoView;
    private SeekBar mSeekBar;
    private TextView mCurrentTime;
    private TextView mTotalTime;
    private View mMediaControlBar;
    private Button mRatio;
    private Button mPlayPause;
    private ArrayList<Uri> mPlayList =new ArrayList<>();
    private int mPositionToPlay;
    // duration position!
    private int mDurationPosition = -1;

    private boolean mDragging = false;

    private Animation mShowControlBarAnimation;
    private Animation mHideControlBarAnimation;
    private boolean mShowing = true;

    private boolean mTransientLossOfFocus;
    private boolean mNeedResumePlay;
    private boolean mIsPlayingMode;

    private int mCurrentPage = 1;
    private VideoTabActivity mActivity;
    private VideoApplication mApplication;

    private boolean fullMode=false;

    private Store mStore;

    private Uri mUri;
    private MediaPlaylistFragment.OnPositionChangedListener mPositionListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (VideoTabActivity)getActivity();
        mApplication=(VideoApplication)mActivity.getApplication();
        mApplication.registerAudioFoucsListener(mMyAudioFocusListener);
        if (mPlayList.size() != 0) {
            mUri = mPlayList.get(0);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.video_playback_layout, container, false);
        setupView(view);
        return view ;
    }


    private void setupView(View view) {
        mMediaControlBar = view.findViewById(R.id.media_controller_bar);
        mVideoView = (CustomVideoView) view.findViewById(R.id.video_surface_view);
        mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
        mCurrentTime = (TextView) view.findViewById(R.id.tv_startTime);
        mTotalTime = (TextView) view.findViewById(R.id.tv_totalTime);
        view.findViewById(R.id.btn_previous).setOnClickListener(this);
        view.findViewById(R.id.btn_next).setOnClickListener(this);
        view.findViewById(R.id.btn_stop).setOnClickListener(this);
        mRatio = (Button) view.findViewById(R.id.btn_full);
        mPlayPause = (Button) view.findViewById(R.id.btn_play);
        mRatio.setOnClickListener(this);
        mPlayPause.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChanged);
        mVideoView.setOnTouchListener(this);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnCompletionListener(this);
    }


    public void onResume() {
        super.onResume();
        Log.d(TAG, "=onResume=");
        // don't play while audio focus transient loss.
        // don't play while audio focus transient loss.
        if (!mTransientLossOfFocus && !isMediaPlaying()) {
            if (isPlayingMode()) {
                mVideoView.start();
            } else {
                play(mUri);
            }
        }


        mHandler.removeMessages(MSG_NOTIFY_MEDIA_INFO);
        mHandler.sendEmptyMessage(MSG_NOTIFY_MEDIA_INFO);
        mHandler.removeMessages(MSG_UPDATE_PROGRESS);
        mHandler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
    }

    public void onPause() {
        super.onPause();
        Log.d(TAG, "=onPause=");
        pause();
        mHandler.removeCallbacksAndMessages(null);
    }

    public void onStop() {
        super.onStop();
        Log.d(TAG, "=onStop=");
        stop();
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "=onDestroy=");
        mVideoView.setVisibility(View.GONE);
        if (!mApplication.hasAbandon()) {
            mApplication.abandonAudioFocus();
        }
    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if (!isVisibleToUser) {
//            pause();
//            Log.d(TAG, "setUserVisibleHint: " + "pause");
//        }else {
//            play(mUri);
//            Log.d(TAG, "setUserVisibleHint: " + "play");
//        }
//    }

    @Override
    public void onClick(View view) {
        showBars(DEFAULT_TIMEOUT);
        switch (view.getId()) {
            case R.id.btn_play:
                if (!isPlayingMode()) {
                    doPlay(0);
                } else if (mVideoView.isPlaying()) {
                    pause();
                } else {
                    resume();
                }
                break;
            case R.id.btn_full:
                if (fullMode) {
                    mRatio.setText("full");
                } else {
                    mRatio.setText("not full");
                }
                fullMode=!fullMode;
                mVideoView.setScreenFull(fullMode);
                break;
            case R.id.btn_stop:
                stop();
                break;
            case R.id.btn_previous:
                playPrevious();
                break;
            case R.id.btn_next:
                playNext();
                break;
        }
    }


    @Override
    public boolean onTouch(View arg0, MotionEvent arg1) {
        if (arg1.getAction() == MotionEvent.ACTION_UP) {
            if (mCurrentPage == 2) {
                //mActivity.switchToPage(1);
                return true;
            }
            if (mShowing) {
                hideBars();
            } else {
                showBars(DEFAULT_TIMEOUT);
            }
        }
        return true;
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        playNext();
    }


    private void notifyPosition() {
        if (mPositionListener != null) {
            mPositionListener.onPositionChanged(mPositionToPlay);
        }
    }

    public void clearPlayList() {
        mPlayList.clear();
        mUri = null;
    }

    public void play(Uri uri) {
        mUri = uri;
        if (uri != null) {
            // play from store duration postion.
        } else if (uri != null) {
            mDurationPosition = 0;

        }
        doPlay(mDurationPosition);
    }

    public void play(int index) {
        if (mPlayList == null || mPlayList.size() == 0) {
            Log.d(TAG, "Nothing to play. mPlayList.size = 0!");
            return;
        }
        mPositionToPlay = index;
        mHandler.sendEmptyMessage(MSG_NOTIFY_POSITION);
        mUri = mPlayList.get(index);
        // play from start.
        doPlay(0);
    }

    private void doPlay(int duration) {
        Log.d(TAG, "doPlay  mUri = " + mUri);
        Uri uri = mUri;
        if (uri == null) {
            Log.d(TAG, "Nothing to play. mUri = null!");
            return;
        }
        mNeedResumePlay = true;
        if (!canPlay()) {
            return;
        }
        mVideoView.stopPlayback();
        mVideoView.setVideoURI(uri);
        if (duration > 0) {
            mVideoView.seekTo(duration);
        }
        mIsPlayingMode = true;
        mVideoView.start();
    }


    public boolean isSamePlaying(Uri uri) {
        return mPlayList.contains(uri);
    }

    public boolean isPlayingMode() {
        return mIsPlayingMode;
    }

    public boolean isMediaPlaying() {
        return isPlayingMode() && mVideoView.isPlaying();
    }

    public void resume() {
        if (!canPlay()) {
            return;
        }
        if (isPlayingMode()) {
            mVideoView.start();
        } else {
            play(mUri);
        }
        updatePlayBtnState();
    }

    public void pause() {
        mNeedResumePlay = true;
        if (mVideoView!=null) {
            mDurationPosition = mVideoView.getCurrentPosition();
            Log.d(TAG, "=pause= mDurationPosition = " + mDurationPosition);
            mVideoView.pause();
            updatePlayBtnState();
        }
    }

    public void stop() {
        Log.d(TAG, "stop: ");
        mNeedResumePlay=false;
        mIsPlayingMode = false;
        mVideoView.stopPlayback();
        updatePlayBtnState();
    }

    public void playNext() {
        if (mPlayList == null || mPlayList.size() == 0) {
            return;
        }
        mPositionToPlay = (++mPositionToPlay) % mPlayList.size();
        if (mVideoView.isPlaying()) {
            VideoUtils.autoMute();
            mVideoView.stopPlayback();
        }
        play(mPositionToPlay);
    }

    public void playPrevious() {
        if (mPlayList == null || mPlayList.size() == 0) {
            return;
        }
        mPositionToPlay = (--mPositionToPlay) % mPlayList.size();
        if (mPositionToPlay < 0) {
            mPositionToPlay = mPlayList.size() - 1;
        }
        if (mVideoView.isPlaying()) {
            VideoUtils.autoMute();
            mVideoView.stopPlayback();
        }
        play(mPositionToPlay);
    }



    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onPrepared");
        showBars(DEFAULT_TIMEOUT);
        int maxValue = mediaPlayer.getDuration() / 1000;
        mHandler.removeMessages(MSG_UPDATE_PROGRESS);
        mHandler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_PLAY_STATE, 500);
        if(maxValue > 0) {
            // reset progress bar;
            mSeekBar.setProgress(0);
            mSeekBar.setMax(maxValue);
//            mTotalTime.setText(VideoUtils.makeTimeString(getActivity(), maxValue));
            mTotalTime.setText(VideoUtils.makeTimeString(mActivity, maxValue));

        }
    }


    private SeekBar.OnSeekBarChangeListener mSeekBarChanged = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            showBars(DEFAULT_TIMEOUT);
            if(mVideoView.getDuration() <= 0){
                seekBar.setProgress(0);
                return;
            }
            mDragging = false;
            mHandler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
            Log.d(TAG, "onStopTrackingTouch: ");
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            showBars(DEFAULT_TIMEOUT);
            if(mVideoView.getDuration() <= 0){
                return;
            }
            mDragging = true;
            mHandler.removeMessages(MSG_UPDATE_PROGRESS);
            Log.d(TAG, "onStartTrackingTouch: ");
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int arg1, boolean fromuser) {
            if(mVideoView.getDuration() <= 0){
                seekBar.setProgress(0);
                return;
            }
            if(!fromuser){
                return;
            }
            showBars(DEFAULT_TIMEOUT);
            long newposition = seekBar.getProgress();
            mVideoView.seekTo((int) newposition *1000);
//            mCurrentTime.setText(VideoUtils.makeTimeString(getActivity(), newposition));
            mCurrentTime.setText(VideoUtils.makeTimeString(mActivity, newposition));

            Log.i(TAG, "newposition = "+ newposition);
        }
    };

    private int setProgress(){
        if (mVideoView == null || mDragging) {
            return 0;
        }
        int position = mVideoView.getCurrentPosition();
        int duration = mVideoView.getDuration();
        if (mSeekBar != null){
            if (duration >= 0) {
                long pos = position/1000 ;
                mSeekBar.setProgress((int)pos);
            } else {
                mSeekBar.setProgress(0);
            }
        }

        if (mCurrentTime != null) {
//            mCurrentTime.setText(VideoUtils.makeTimeString(getActivity(), position/1000));
            mCurrentTime.setText(VideoUtils.makeTimeString(mActivity, position/1000));

        }

        return position;
    }


    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            int pos;
            switch(msg.what){
                case MSG_UPDATE_PROGRESS:
                    pos = setProgress();
                    msg = obtainMessage(MSG_UPDATE_PROGRESS);
                    sendMessageDelayed(msg, 1000 - (pos % 1000));
                    break;
                case MSG_NOTIFY_POSITION:
                    notifyPosition();
                    break;
                case MSG_BRAKE_ON:
                    //mBrakeView.setVisibility(View.GONE);
                    showBars(DEFAULT_TIMEOUT);
                    break;
                case MSG_BRAKE_OFF:
                    //mBrakeView.setVisibility(View.VISIBLE);
                    //mActivity.quitFullScreen();
                    break;
                case MSG_FADE_OUT:
                    if (mCurrentPage == 1) {
                        hideBars();
                    }
                    break;
                case MSG_NOTIFY_MEDIA_INFO:
//                    if (mVideoView.isPlaying()) {
//                        notifyMediaInfo();
//                    }
                    sendEmptyMessageDelayed(MSG_NOTIFY_MEDIA_INFO, 1000);
                    break;
                case MSG_UPDATE_PLAY_STATE:
                    updatePlayBtnState();
                    break;
                default:
                    break;
            }
        }
    };

    public void registerPositionListener(MediaPlaylistFragment.OnPositionChangedListener listener) {
        this.mPositionListener = listener;
    }

    public void showBars(int timeout) {
        Message msg = mHandler.obtainMessage(MSG_FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(MSG_FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }

        if (!mShowing) {
            mShowing = true;
            mMediaControlBar.setVisibility(View.VISIBLE);
            mActivity.quitFullScreen();
            if (mShowControlBarAnimation == null) {
                mShowControlBarAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.media_control_bar_up);
            }

            mMediaControlBar.startAnimation(mShowControlBarAnimation);
        }
    }

    public void hideBars() {
        if (mShowing) {
            mShowing = false;
            Log.d(TAG, "hideBars: "+mShowing);
            if (mHideControlBarAnimation == null) {
                mHideControlBarAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.media_control_bar_down);
                mHideControlBarAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation arg0) {
                    }
                    @Override
                    public void onAnimationRepeat(Animation arg0) {
                    }
                    @Override
                    public void onAnimationEnd(Animation arg0) {
                        if (!mShowing) {
                            mMediaControlBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
            mMediaControlBar.startAnimation(mHideControlBarAnimation);
            mActivity.enterFullScreen();
        }
    }


    private void updatePlayBtnState() {
        if(isMediaPlaying()) {
            mPlayPause.setText("Pause");
        } else {
            mPlayPause.setText("Play");
        }
    }


    public void setCurrentPage(int currentPage) {
        this.mCurrentPage = currentPage;
    }


    private boolean canPlay() {
        Log.d(TAG, "canPlay: " + mApplication.requestAudioFocus(false));
        return mApplication.requestAudioFocus(false);
    }



    public void listPlay(Store store, ArrayList<Uri> playList) {
        this.mStore = store;
        this.mPlayList = playList;
        if (isSamePlaying(mUri)) {
            mPositionToPlay = mPlayList.indexOf(mUri);
            mHandler.sendEmptyMessage(MSG_NOTIFY_POSITION);
            if (!isPlayingMode()) {
                play(mUri);
            }
            return;
        }
        // default play first one.
        play(0);
    }

    public Uri getPlayingUri() {
        return mUri;
    }


    private VideoApplication.IAudioFocusListener mMyAudioFocusListener = new VideoApplication.IAudioFocusListener() {
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.d(TAG, "AUDIOFOCUS_LOSS");
                    mActivity.finish();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                    mTransientLossOfFocus = true;
                    if (isMediaPlaying()) {
                        mNeedResumePlay = true;
                        pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.d(TAG, "AUDIOFOCUS_GAIN");
                    mTransientLossOfFocus = false;
                    if (mNeedResumePlay) {
                        resume();
                    }
                    break;
                default:
                    break;
            }
        }
    };


}
