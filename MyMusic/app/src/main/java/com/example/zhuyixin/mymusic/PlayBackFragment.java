package com.example.zhuyixin.mymusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


public class PlayBackFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "TAG" + "PlayBackFragment";

    private final String ACTION_MUSIC_INFO_UPDATED = "action_music_info_updated";
    private final String ACTION_MUSIC_START = "action_music_start";
    private final String ACTION_MUSIC_PAUSE = "action_music_pause";
    private final String ACTION_MUSIC_CHANGED = "action_music_changed";

    private Button btnPrevious;
    private Button btnPlay;
    private Button btnNext;
    private SeekBar seekBar;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private TextView tvTitle;
    private TextView tvArtist;
    private TextView tvAlbum;
    private ImageView imgAlbum;
    private IMediaPlaybackService mService;
    private LocalBroadcastManager localBroadcastManager;
    private LocalReceiver localReceiver;
    private Store mStore;


    public PlayBackFragment() {
        // Required empty public constructor
    }

    public static PlayBackFragment newInstance() {
        return new PlayBackFragment();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.playback_fragment, container, false);
        btnPrevious = (Button) view.findViewById(R.id.btn_previous);
        btnPrevious.setOnClickListener(this);
        btnNext = (Button) view.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(this);
        btnPlay = (Button) view.findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(this);
        seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        tvCurrentTime= (TextView) view.findViewById(R.id.tv_currentTime);
        tvTotalTime= (TextView) view.findViewById(R.id.tv_totalTime);
        tvTitle= (TextView) view.findViewById(R.id.tv_title);
        tvArtist= (TextView) view.findViewById(R.id.tv_artist);
        tvAlbum= (TextView) view.findViewById(R.id.tv_album);
        imgAlbum= (ImageView) view.findViewById(R.id.img_album);
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_MUSIC_INFO_UPDATED);
        intentFilter.addAction(ACTION_MUSIC_CHANGED);
        intentFilter.addAction(ACTION_MUSIC_PAUSE);
        intentFilter.addAction(ACTION_MUSIC_START);
        localReceiver=new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver,intentFilter);
        return view;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_play:
                try {
                    if (mService.isPlaying()) {
                        mService.pause();
                    }else {
                        mService.play();
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_next:
                try {
                    mService.next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_previous:
                try {
                    mService.previous();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }
    }


    public void setService(IMediaPlaybackService service) {
        mService=service;
        Log.d(TAG, "setService: ");
        try {
            mService.getLatestInfo();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        localBroadcastManager.unregisterReceiver(localReceiver);
    }
    
    



    class LocalReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_MUSIC_INFO_UPDATED:
                    Bundle bundle = intent.getBundleExtra("info");
                    long current=bundle.getLong("current");
                    long total=bundle.getLong("duration");
                    tvCurrentTime.setText(MusicUtil.makeTimeString(getContext(),current/1000));
                    tvTotalTime.setText(MusicUtil.makeTimeString(getContext(), total/1000));
                    seekBar.setMax(((int) total));
                    seekBar.setProgress(((int) current));
                    break;
                case ACTION_MUSIC_CHANGED:
                    Log.d(TAG, "onReceive: " + "change");
                    int playingIndex=MediaModel.getInstance().getPlayingIndex();
                    Store store=MediaModel.getInstance().getPlayingStore();
                    if (store == null) {
                        break;
                    }
                    Uri uri = MediaModel.getInstance().getUriList(store).get(playingIndex);
                    Log.d(TAG, "onReceive: " + uri);
                    MusicItem item = MusicUtil.queryMusicFromContentProvider(getContext(), uri);
                    if (item!=null) {
                        if (item.getImage() != null) {
                            imgAlbum.setImageBitmap(item.getImage());
                        }else {
                            imgAlbum.setImageResource(R.drawable.music);
                        }
                        tvTitle.setText(TextUtils.isEmpty(item.getTitle()) ? item.getFileName() : item.getTitle());
                        tvArtist.setText(item.getArtist());
                        tvAlbum.setText(item.getAlbum());
                    }
                    break;
                case ACTION_MUSIC_START:
                    btnPlay.setText("pause");
                    break;
                case ACTION_MUSIC_PAUSE:
                    btnPlay.setText("play");
                    break;
            }

        }
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener=new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (b) {
                try {
                    mService.seekTo(seekBar.getProgress());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

}
