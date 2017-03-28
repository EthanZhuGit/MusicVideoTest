package com.example.zhuyixin.mymusic;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Adapter;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MusicBrowserActivity extends AppCompatActivity{
    private static final String TAG = "TAG" + "MusicBrowserActivity";
    private MediaStorageFragment mMediaStorageFragment;
    private PlayBackFragment mPlayBackFragment;
    private ViewPager mViewPager;
    private FragmentPagerAdapter mFragmentPagerAdapter;
    private List<Fragment> pageList = new ArrayList<>();
    private IMediaPlaybackService service;
    private Intent startServiceIntent;
    private boolean isPlaying=false;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.music_browser_activity);
        initView();

        Log.d(TAG, "onCreate: ");

    }


    @Override
    protected void onResume() {
        super.onResume();
        startServiceIntent =new Intent(this, MediaPlaybackService.class);
        startService(startServiceIntent);
        bindService(startServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void initView() {
//        mBrowserFragment = BrowserFragment.newInstance();
        mMediaStorageFragment = MediaStorageFragment.newInstance();
        mPlayBackFragment = PlayBackFragment.newInstance();
        pageList.add(mMediaStorageFragment);
        pageList.add(mPlayBackFragment);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mFragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return pageList.get(position);
            }

            @Override
            public int getCount() {
                return pageList.size();
            }
        };
        mViewPager.setAdapter(mFragmentPagerAdapter);
        mViewPager.setCurrentItem(0);
    }

    private ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            service = IMediaPlaybackService.Stub.asInterface(iBinder);
            mPlayBackFragment.setService(service);
            try {
                isPlaying=service.isPlaying();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    public void switchToPage(int page) {
        mViewPager.setCurrentItem(page);
    }


    public void switchToPlay(Store store, int index, boolean all) {
        mViewPager.setCurrentItem(1, true);
        MediaModel.getInstance().setPlayingStore(store);
        MediaModel.getInstance().setPlayingIndex(index);
        try {
            service.play(index);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }



    public void isPlaying() {
        if (isPlaying) {
            mViewPager.setCurrentItem(1);
            try {
                service.getLatestInfo();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
        try {
            if (!service.isPlaying()){
                stopService(startServiceIntent);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");

    }
}
