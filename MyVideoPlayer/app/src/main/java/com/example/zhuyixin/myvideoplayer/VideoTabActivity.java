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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_main);
        mStoreManager = StoreManager.getInstance(this);
        mMediaStorageFragment.setMediaPlaybackFragment(mMediaPlaybackFragment);
        initViewPager();
    }


    private void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.id_viewpager);
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

    public void switchToPage(int page) {
        mViewPager.setCurrentItem(page, true);
    }


    public void switchToPlay(Store store, ArrayList<Uri> fileUriList, boolean all) {
//        if (all) {
//            mStoreManager.putBoolean(StoreManager.KEY_ALL_VIDEO, true);
//        } else {
//            mStoreManager.putBoolean(StoreManager.KEY_ALL_VIDEO, false);
//        }
        mViewPager.setCurrentItem(1, true);
//        mMediaStorageFragment.setCurrentMediaStore(store);
        //mMediaPlaybackFragment.registerPositionListener(mMediaPlaylistFragment.mPositionChangedListener);
        //mMediaPlaylistFragment.setDataList(fileUriList);
        mMediaPlaybackFragment.listPlay(store, fileUriList);
    }
}
