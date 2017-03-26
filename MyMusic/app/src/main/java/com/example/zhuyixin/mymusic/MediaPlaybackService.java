package com.example.zhuyixin.mymusic;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackService extends Service implements MediaPlayer.OnPreparedListener,MediaPlayer.OnCompletionListener {
    private static final String TAG = "TAG" + "MediaPlaybackService";
    private static final int MSG_UPDATE_PROGRESS = 0x01;
    private final String ACTION_MUSIC_INFO_UPDATED = "action_music_info_updated";
    private final String ACTION_MUSIC_CHANGED = "action_music_changed";
    private final String ACTION_MUSIC_START = "action_music_start";
    private final String ACTION_MUSIC_PAUSE = "action_music_pause";
    private MediaPlayer mediaPlayer;
    private StoreManager mStoreManager;
    private List<Uri> playList;
    private MediaModel mediaModel;
    private LocalBroadcastManager localBroadcastManager;
    private NotificationManager manager;
    private RemoteViews remoteViews;
    private PlayBackFragment fragment;
    private String musicFilePath;
    private AudioManager mAudioManager;
    private boolean mRequestToken;
    public MediaPlaybackService() {
    }

    private Store playingStore;
    private int playingIndex=-1;


    public IMediaPlaybackService.Stub mBinder=new IMediaPlaybackService.Stub() {
        @Override
        public void play(int index) throws RemoteException {
            Log.d(TAG, "play: ");
            playCurrentStore(index);
        }

        @Override
        public void seekTo(int position) throws RemoteException{
            mediaPlayer.seekTo(position);
        }

        @Override
        public void pause() throws RemoteException{
            mediaPlayer.pause();
            notifyState(ACTION_MUSIC_PAUSE);
        }

        @Override
        public void start() throws RemoteException{
            mediaPlayer.start();
            notifyState(ACTION_MUSIC_START);
        }

        @Override
        public void next() throws RemoteException{
            playNext();
        }

        @Override
        public void previous() throws RemoteException{
            playPrevious();
        }

        @Override
        public boolean isPlaying() throws RemoteException{
            return getIsPlaying();
        }

        @Override
        public void getLatestInfo() throws RemoteException{
            if (mediaPlayer != null) {
                notifyState(ACTION_MUSIC_CHANGED);
                if (mediaPlayer.isPlaying()) {
                    notifyState(ACTION_MUSIC_START);
                }else {
                    notifyState(ACTION_MUSIC_PAUSE);
                }
            }
        }

    };



    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaModel=MediaModel.getInstance();
        mStoreManager = StoreManager.getInstance(this);
        mediaPlayer=new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onPrepared");
        notifyState(ACTION_MUSIC_CHANGED);
        mHandler.removeMessages(MSG_UPDATE_PROGRESS);
        mHandler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onCompletion: ");
        playNext();
        notifyState(ACTION_MUSIC_CHANGED);
    }



    private void playCurrentStore(int index) {
        if (playingIndex == -1) {
            getInfo();
            playIndex(index);
        } else if (playingIndex != index) {
            getInfo();
            mediaPlayer.reset();
            playIndex(index);
        }

    }

    private void playNext() {
        getInfo();
        Log.d(TAG, "playNext: " + playingIndex);
        Log.d(TAG, "playNext: " + playList.size());
        int newPosition=Math.abs((playingIndex+1)%playList.size());
        mediaPlayer.reset();
        playIndex(newPosition);
        mediaModel.setPlayingIndex(newPosition);
    }



    private void playPrevious() {
        getInfo();
        Log.d(TAG, "playPrevious: " + playingIndex);
        Log.d(TAG, "playPrevious: " + playList.size());
        int newPosition=Math.abs((playingIndex-1)%playList.size());
        mediaPlayer.reset();
        playIndex(newPosition);
        mediaModel.setPlayingIndex(newPosition);
    }

    private void playIndex(int index) {
        if (!canPlay()){
            return;
        }
        try {
            mediaPlayer.setDataSource(playList.get(index).getPath());
            mediaModel.setPlayingIndex(index);
            Log.d(TAG, "playIndex: " + "new position "+index+" ");
            musicFilePath=playList.get(index).getPath();
            Log.d(TAG, "playIndex: "+musicFilePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
        notifyState(ACTION_MUSIC_START);
    }

    private boolean getIsPlaying() {
        return mediaPlayer.isPlaying();
    }

    private boolean canPlay() {
        return requestAudioFocus();
    }

    private boolean requestAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager
                .requestAudioFocus(audioFocusChangeListener,
                        AudioManager.USE_DEFAULT_STREAM_TYPE,
                        AudioManager.AUDIOFOCUS_GAIN);
    }



    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch(msg.what){
                case MSG_UPDATE_PROGRESS:
                    updateSeekBar();
                    msg = obtainMessage(MSG_UPDATE_PROGRESS);
                    sendMessageDelayed(msg, 500);
                    break;
                default:
                    break;
            }
        }
    };

    private void updateSeekBar() {

        Intent intent=new Intent(ACTION_MUSIC_INFO_UPDATED);
        Bundle b = new Bundle();
        b.putLong("duration", mediaPlayer.getDuration());
        b.putLong("current", mediaPlayer.getCurrentPosition());
        intent.putExtra("info",b);
        localBroadcastManager.sendBroadcast(intent);

    }


    /**
     * 获取存储在MediaModel中的当前播放存储器和音乐在Uri链表中的索引
     */
    private void getInfo() {

        playingStore=mediaModel.getPlayingStore();
        playingIndex=mediaModel.getPlayingIndex();
        playList = mediaModel.getUriList(playingStore);
    }

    private void notifyState(String action) {
        Intent intent;
        switch (action) {
            case ACTION_MUSIC_START:
                initNotification();
                intent = new Intent(ACTION_MUSIC_START);
                localBroadcastManager.sendBroadcast(intent);
                break;
            case ACTION_MUSIC_PAUSE:
                manager.cancelAll();
                intent = new Intent(ACTION_MUSIC_PAUSE);
                localBroadcastManager.sendBroadcast(intent);
                break;
            case ACTION_MUSIC_CHANGED:
                intent = new Intent(ACTION_MUSIC_CHANGED);
                localBroadcastManager.sendBroadcast(intent);
                Log.d(TAG, "notifyState: " + "change");
                break;
        }
    }

    private void initNotification() {
        Notification.Builder builder = new Notification.Builder(this);
        Intent intentStartActivity = new Intent(this, MusicBrowserActivity.class);
        PendingIntent pendingIntentStartActivity = PendingIntent.getActivity(this, 1, intentStartActivity, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
        String title = musicFilePath.substring(musicFilePath.lastIndexOf("/") + 1);
        remoteViews.setTextViewText(R.id.tv_notice_title,title);
//        remoteViews.setOnClickPendingIntent(R.id.tv_notice_title,pendingIntentStartActivity);
        builder.setContent(remoteViews).setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentIntent(pendingIntentStartActivity);
        Notification notification=builder.build();
        notification.flags=Notification.FLAG_NO_CLEAR;
        manager.notify(100,notification);
    }


    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.d(TAG, "AUDIOFOCUS_LOSS");
                    mediaPlayer.stop();
                    stopSelf();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                    mediaPlayer.pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.d(TAG, "AUDIOFOCUS_GAIN");
                    mediaPlayer.start();
                    break;
                default:
                    break;
            }
        }
    };



    @Override
    public void onDestroy() {
        super.onDestroy();
        mAudioManager.abandonAudioFocus(audioFocusChangeListener);
        manager.cancelAll();
        Log.d(TAG, "onDestroy: ");
    }
}
