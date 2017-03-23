package com.example.zhuyixin.mymusic;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
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

    private PlayBackFragment fragment;
    public MediaPlaybackService() {
    }

    private Store playingStore;
    private int playingIndex;
    private int oldPlayingIndex;

    public IMediaPlaybackService.Stub mBinder=new IMediaPlaybackService.Stub() {
        @Override
        public void play() throws RemoteException {
            playCurrentStore();
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
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext());
        Intent intent1 = new Intent(this, MusicBrowserActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, intent1, 0))
                .setContentTitle("hehe")
                .setContentText("jejejejej");
        Notification notification=builder.build();
        startForeground(110,notification);
        return super.onStartCommand(intent, flags, startId);


    }

    private void playCurrentStore() {
        getInfo();
        if (oldPlayingIndex == playingIndex) {
            mediaPlayer.start();
            notifyState(ACTION_MUSIC_START);
        }else {
            mediaPlayer.reset();
            playIndex(playingIndex);
        }

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
        playNext();
        notifyState(ACTION_MUSIC_CHANGED);
    }



    private void playNext() {
        mediaPlayer.reset();
        getInfo();
        int newPosition=Math.abs((playingIndex+1)%playList.size());
        playIndex(newPosition);
        mediaModel.setPlayingIndex(newPosition);
    }



    private void playPrevious() {
        mediaPlayer.reset();
        getInfo();
        int newPosition=Math.abs((playingIndex-1)%playList.size());
        playIndex(newPosition);
        mediaModel.setPlayingIndex(newPosition);
    }

    private void playIndex(int index) {
        try {
            mediaPlayer.setDataSource(playList.get(index).getPath());
            mediaModel.setPlayingIndex(index);
            Log.d(TAG, "playIndex: " + "new position "+index);
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
        oldPlayingIndex=playingIndex;
        playingStore=mediaModel.getPlayingStore();
        playingIndex=mediaModel.getPlayingIndex();
        playList = mediaModel.getUriList(playingStore);
    }

    private void notifyState(String action) {
        Intent intent;
        switch (action) {
            case ACTION_MUSIC_START:
                intent = new Intent(ACTION_MUSIC_START);
                localBroadcastManager.sendBroadcast(intent);
                break;
            case ACTION_MUSIC_PAUSE:
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

}
