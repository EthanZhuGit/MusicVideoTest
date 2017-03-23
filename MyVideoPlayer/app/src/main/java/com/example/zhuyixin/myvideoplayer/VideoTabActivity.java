package com.example.zhuyixin.myvideoplayer;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class VideoTabActivity extends AppCompatActivity {
    private static final String TAG = "TAG" + "VideoTabActivity";

    private MediaPlaybackFragment mMediaPlaybackFragment = new MediaPlaybackFragment();
    private MediaPlaylistFragment mMediaPlaylistFragment = new MediaPlaylistFragment();
    private MediaStorageFragment mMediaStorageFragment = new MediaStorageFragment();

    private StoreManager mStoreManager;
    private ViewPager mViewPager;
    private FragmentPagerAdapter mFragmentPagerAdapter;

    private List<Fragment> fragmentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_main);
        mStoreManager = StoreManager.getInstance(this);
        mMediaStorageFragment.setMediaPlaybackFragment(mMediaPlaybackFragment);
        mMediaStorageFragment.setMediaPlaylistFragment(mMediaPlaylistFragment);
        initViewPager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }



    private void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.id_viewpager);
        mViewPager.addOnPageChangeListener(mPageChangeListener);
        fragmentList.add(mMediaStorageFragment);
        fragmentList.add(mMediaPlaybackFragment);
        fragmentList.add(mMediaPlaylistFragment);
        mFragmentPagerAdapter=new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragmentList.get(position);
            }

            @Override
            public int getCount() {
                return fragmentList.size();
            }

            @Override
            public float getPageWidth(int position) {
                if (position == 2) {
                    return super.getPageWidth(position) / 3;
                } else {
                    return super.getPageWidth(position);
                }
            }
        };
        mViewPager.setAdapter(mFragmentPagerAdapter);
        mViewPager.setCurrentItem(0,true);
    }

    public void play(int index) {
        mMediaPlaybackFragment.play(index);
    }

    public void switchToPage(int page) {
        mViewPager.setCurrentItem(page, true);
    }


    public void switchToPlay(Store store, ArrayList<Uri> fileUriList, boolean all) {
        mViewPager.setCurrentItem(1, true);
        mMediaPlaybackFragment.registerPositionListener(mMediaPlaylistFragment.mPositionChangedListener);
        mMediaPlaylistFragment.setDataList(fileUriList);
        mMediaPlaybackFragment.listPlay(store, fileUriList);
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int arg0) {
            if (arg0 == 0) {
                mMediaPlaybackFragment.setCurrentPage(0);
                mMediaPlaybackFragment.pause();
                quitFullScreen();
            } else if (arg0 == 2) {
                mMediaPlaybackFragment.setCurrentPage(2);
                mMediaPlaybackFragment.showBars(mMediaPlaybackFragment.DEFAULT_TIMEOUT);
            } else {
                mMediaPlaybackFragment.setCurrentPage(1);
                if (mMediaPlaybackFragment.isPlayingMode()){
                    if (!mMediaPlaybackFragment.isMediaPlaying()){
                        mMediaPlaybackFragment.resume();
                    }
                }
                mMediaPlaybackFragment.showBars(mMediaPlaybackFragment.DEFAULT_TIMEOUT);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };


    public void enterFullScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    public void quitFullScreen() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}
