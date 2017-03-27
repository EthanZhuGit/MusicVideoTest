package com.example.zhuyixin.mymusic;

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

    private MediaFragment mMediaFragmentHD, mMediaFragmentUSB, mMediaFragmentSD;
    private RadioButton mRadioBtn0, mRadioBtn1, mRadioBtn2;
    private RadioGroup mRadioGroup;

    public MediaStorageFragment() {

    }

    public static MediaStorageFragment newInstance() {
        return new MediaStorageFragment();
    }

    private int mStateRecord = 0;
    private static final int STATE_FORGROUND = 0x00000001;



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
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
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
        mStoreManager.unregisterStoreChangedListener(mStoreChangedListener);
        mStoreManager.unregisterReceiver();
        Log.d(TAG, "=>onDestroy=");
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
     * 将store的按键设为true
     *
     * @param
     */
    public void setCurrentMediaStore(Store store) {
        mCurrentStore = store;
        int i = mStoreManager.getStoreList().indexOf(store);
        switch (i) {
            case 0:
                mRadioBtn0.setChecked(true);
                break;
            case 1:
                mRadioBtn1.setChecked(true);
                break;
            case 2:
                mRadioBtn2.setChecked(true);
                break;
        }
    }


    /**
     * 实现onStoreChanged(),
     */
    private StoreManager.IStoreChangedListener mStoreChangedListener = new StoreManager.IStoreChangedListener() {

        public void onStoreChanged(Uri storageVolume, boolean mounted) {
            Log.d(TAG, "onStoreChanged: ");
            updateStorageState();
        }
    };

//
//    private void ejectStoragePlaying(Uri storageVolume) {
//        Uri playingUri = mPlayback.getPlayingUri();
//        if (playingUri.getPath().startsWith(storageVolume.getPath())) {
//            Log.d(TAG, "=stop current playback.");
//            VideoTabActivity activity = (VideoTabActivity) getActivity();
//            activity.switchToPage(0);
//            mPlayback.stop();
//        }
//    }


    private RadioGroup.OnCheckedChangeListener OnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup arg0, int arg1) {
            int radioButtonId = arg0.getCheckedRadioButtonId();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            switch (radioButtonId) {
                case R.id.storage_0:
                    Log.d(TAG, "===>>HD ");
                    mCurrentStore = mStoreManager.getStoreList().get(0);
                    Log.d(TAG, "onCheckedChanged: " + mCurrentStore.getUri().toString());
                    if (mMediaFragmentHD == null) {
                        mMediaFragmentHD = MediaFragment.newInstance(mCurrentStore.getUri().toString(), mCurrentStore.mounted());
                    }

                    transaction.replace(R.id.container, mMediaFragmentHD);
                    transaction.commit();
                    break;
                case R.id.storage_1:
                    Log.d(TAG, "===>>USB ");
                    mCurrentStore = mStoreManager.getStoreList().get(1);
                    if (mMediaFragmentUSB == null) {
                        mMediaFragmentUSB = MediaFragment.newInstance(mCurrentStore.getUri().toString(), mCurrentStore.mounted());
                    }

                    transaction.replace(R.id.container, mMediaFragmentUSB);
                    transaction.commit();
                    break;
                case R.id.storage_2:
                    Log.d(TAG, "===>>SD ");
                    mCurrentStore = mStoreManager.getStoreList().get(2);
                    if (mMediaFragmentSD == null) {
                        mMediaFragmentSD = MediaFragment.newInstance(mCurrentStore.getUri().toString(), mCurrentStore.mounted());
                    }
                    transaction.replace(R.id.container, mMediaFragmentSD);
                    transaction.commit();
                    break;
                default:
                    transaction.commit();
                    break;
            }
        }
    };
}
