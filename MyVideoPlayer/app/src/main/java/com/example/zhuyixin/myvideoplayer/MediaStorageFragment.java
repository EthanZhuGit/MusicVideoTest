package com.example.zhuyixin.myvideoplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;


public class MediaStorageFragment extends Fragment {
    private static final String TAG = "TAG" + "MediaStorageFragment";
    private StoreManager mStoreManager;
    private Store mCurrentStore;

    private MediaFragment mMediaFragment0, mMediaFragment1, mMediaFragment2;
    private RadioButton mRadioBtn0, mRadioBtn1, mRadioBtn2;
    private RadioGroup mRadioGroup;
    private MediaPlaybackFragment mPlayback;
    private MediaPlaylistFragment mPlaylist;


    public MediaStorageFragment() {
        // Required empty public constructor
    }

    private int mStateRecord = 0;
    private static final int STATE_FORGROUND = 0x00000001;

    public void setMediaPlaybackFragment(MediaPlaybackFragment playback) {
        this.mPlayback = playback;
    }

    public void setMediaPlaylistFragment(MediaPlaylistFragment playlist) {
        this.mPlaylist = playlist;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "=>onAttach=");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStoreManager = StoreManager.getInstance(getActivity());
        mStoreManager.registerStoreChangedListener(mStoreChangedListener);
        Log.d(TAG, "onCreate: " + "registerStoreChangedListener");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "=>onCreateView=");
        View view = inflater.inflate(R.layout.storage_layout, container, false);
        mRadioGroup = (RadioGroup) view.findViewById(R.id.radio_group);
        mRadioBtn0 = (RadioButton) mRadioGroup.findViewById(R.id.storage_0);
        mRadioBtn1 = (RadioButton) mRadioGroup.findViewById(R.id.storage_1);
        mRadioBtn2 = (RadioButton) mRadioGroup.findViewById(R.id.storage_2);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "=>onActivityCreated=");
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "=>onStart=");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "=>onResume=");
        mStateRecord |= STATE_FORGROUND;
        mRadioGroup.setOnCheckedChangeListener(OnCheckedChangeListener);
        updateStorageState();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "=>onPause=");
        mStateRecord &= (~STATE_FORGROUND);
        mRadioGroup.setOnCheckedChangeListener(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "=>onStop=");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "=>onDestroyView=");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "=>onDestroy=");
        mStoreManager.unregisterStoreChangedListener(mStoreChangedListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "=>onDetach=");
    }


    /**
     * 更新存储器状态，
     */
    private void updateStorageState() {
        ArrayList<Store> stores = mStoreManager.getStoreList();
        Log.d(TAG, "updateStorageState: " + stores.size());
        switch (stores.size()) {
            case 1:
                mRadioBtn0.setVisibility(View.VISIBLE);
                mRadioBtn0.setChecked(true);
                mRadioBtn1.setVisibility(View.INVISIBLE);
                mRadioBtn2.setVisibility(View.INVISIBLE);
                break;
            case 2:
                mRadioBtn0.setVisibility(View.VISIBLE);
                mRadioBtn0.setChecked(true);
                mRadioBtn1.setVisibility(View.VISIBLE);
                mRadioBtn1.setText(stores.get(1).getDirectory().getPath());
                mRadioBtn2.setVisibility(View.INVISIBLE);
                break;
            case 3:
                mRadioBtn0.setVisibility(View.VISIBLE);
                mRadioBtn0.setChecked(true);
                mRadioBtn1.setVisibility(View.VISIBLE);
                mRadioBtn1.setText(stores.get(1).getDirectory().getPath());
                mRadioBtn2.setVisibility(View.VISIBLE);
                mRadioBtn2.setText(stores.get(2).getDirectory().getPath());
                break;
        }
    }



    /**
     * 实现onStoreChanged(),
     */
    private StoreManager.IStoreChangedListener mStoreChangedListener = new StoreManager.IStoreChangedListener() {

        public void onStoreChanged(Uri storageVolume, boolean mounted) {
            if (!mounted) {
                ejectStoragePlaying(storageVolume);
            }
            updateStorageState();
        }
    };


    private void ejectStoragePlaying(Uri storageVolume) {
        Uri playingUri = mPlayback.getPlayingUri();
        if (playingUri!=null) {
            if (playingUri.getPath().startsWith(storageVolume.getPath())) {
                Log.d(TAG, "=stop current playback.");
                VideoTabActivity activity = (VideoTabActivity) getActivity();
                activity.switchToPage(0);
                mPlayback.stop();
                mPlaylist.setDataList(null);
            }
        }
    }


    private RadioGroup.OnCheckedChangeListener OnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup arg0, int arg1) {
            int radioButtonId = arg0.getCheckedRadioButtonId();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            switch (radioButtonId) {
                case R.id.storage_0:
                    Log.d(TAG, "===>>0 ");
                    mCurrentStore = mStoreManager.getStoreList().get(0);
                    Log.d(TAG, "onCheckedChanged: " + mCurrentStore.getUri().toString());
                    if (mMediaFragment0 == null) {
                        mMediaFragment0 = MediaFragment.newInstance(mCurrentStore.getUri().toString(), mCurrentStore.mounted());
                    }

                    transaction.replace(R.id.container, mMediaFragment0);
                    transaction.commit();
                    break;
                case R.id.storage_1:
                    Log.d(TAG, "===>>1 ");
                    mCurrentStore = mStoreManager.getStoreList().get(1);
                    if (mMediaFragment1 == null) {
                        mMediaFragment1 = MediaFragment.newInstance(mCurrentStore.getUri().toString(), mCurrentStore.mounted());
                    }

                    transaction.replace(R.id.container, mMediaFragment1);
                    transaction.commit();
                    break;
                case R.id.storage_2:
                    Log.d(TAG, "===>>2 ");
                    mCurrentStore = mStoreManager.getStoreList().get(2);
                    if (mMediaFragment2 == null) {
                        mMediaFragment2 = MediaFragment.newInstance(mCurrentStore.getUri().toString(), mCurrentStore.mounted());
                    }
                    transaction.replace(R.id.container, mMediaFragment2);
                    transaction.commit();
                    break;
                default:
                    transaction.commit();
                    break;
            }
        }
    };


}
