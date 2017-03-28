package com.example.zhuyixin.mymusic;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MediaModel {

    private final String TAG = "TAG " + MediaModel.class.getSimpleName();
    private static MediaModel sInstance;
    private LoadTask mLoadTask;
    private ArrayList<String> mScanExclude = new ArrayList<String>();
    private HashMap<String, IDataChangedListener> mListeners = new HashMap<String, IDataChangedListener>();
    private HashMap<String, ArrayList<Uri>> mData = new HashMap<>();

    private Store playingStore;
    private int playingIndex;
    private MediaModel() {
        loadExcludeConfig();
    }


    public void setPlayingIndex(int playingIndex) {
        this.playingIndex = playingIndex;
        Log.d(TAG, "setPlayingIndex: " + playingIndex);
    }

    public void setPlayingStore(Store store) {
        this.playingStore=store;
        Log.d(TAG, "setPlayingStore: " + playingStore.getUri().toString());
    }

    public int getPlayingIndex() {
        return playingIndex;
    }

    public Store getPlayingStore() {
        return playingStore;
    }

    public ArrayList<Uri> getUriList(Store store){
        if (mData != null) {
            return mData.get(store.getDirectory().getPath());
        } else {
            return null;
        }
    }
    public synchronized static MediaModel getInstance() {
        if (sInstance == null) {
            sInstance = new MediaModel();
        }
        return sInstance;
    }

    public void registerDataChangedListener(Store store, IDataChangedListener listener) {
        mListeners.put(store.getDirectory().getPath(), listener);
    }

    public void unregisterDataChangedListener(Store store, IDataChangedListener listener) {
        mListeners.remove(store.getDirectory().getPath());
    }

    public ArrayList<Uri> startAllVideoLoader(Store store, OnAllVideoLoadListener listener) {
        Log.d(TAG, "startAllVideoLoader: ");
        ArrayList<Uri> list = mData.get(store.getDirectory().getPath());
        if (list != null) {
//             get from cache.
            return list;
        } else {
            mLoadTask = new LoadTask(store, listener);
            mLoadTask.execute(store.getDirectory());
            return null;
        }
    }

    /**
     * 当存储器插拔后更新数据
     * @param store
     * @param storageVolume
     * @param mounted
     */
    public void updateData(Store store, Uri storageVolume, boolean mounted) {
        Log.d(TAG, "updateData: ");
        if (store != null) {
            if (mLoadTask != null && mLoadTask.mStore.equals(store)) {
                mLoadTask.cancel(true);
            }
            mLoadTask = new LoadTask(store, null);
            mLoadTask.execute(store.getDirectory());
        } else {
            String path = storageVolume.getPath();
            if (mData.containsKey(path)) {
                mData.remove(path);
            }
        }

    }

    private void loadExcludeConfig() {
        File file = new File("/bootlogo/config/scan_exclude");
        BufferedReader bufferedReader = null;
        try {
            if (file.isFile() && file.exists()) {
                bufferedReader = new BufferedReader(new FileReader(file));
                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    Log.d(TAG, "=loadExcludeConfig=" + lineTxt);
                    mScanExclude.add(lineTxt);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface IDataChangedListener {
        public void onDataChanged(Store store, ArrayList<Uri> dataList);
    }


    public interface OnAllVideoLoadListener {
        public void onLoadFinished(Store store, ArrayList<Uri> dataList);
    }

    /*
    public interface OnFolderVideoLoadListener {
        public void onLoadFinished(Uri uri, ArrayList<Uri> dataList);
    }
    */
    private class LoadTask extends AsyncTask<File, Integer, ArrayList<Uri>> {

        private Store mStore;
        private OnAllVideoLoadListener mLoadListener;

        public LoadTask(Store store, OnAllVideoLoadListener listener) {
            this.mStore = store;
            this.mLoadListener = listener;
        }

        @Override
        protected ArrayList<Uri> doInBackground(File... params) {
            Log.d(TAG, "doInBackground...");
            ArrayList<Uri> entries = new ArrayList<>();
            iteratorFile(params[0], entries);
            return entries;
        }

        @Override
        protected void onPostExecute(ArrayList<Uri> result) {
            Log.d(TAG, "onPostExecute "+result.size());
            if (!isCancelled()) {
                mData.put(mStore.getDirectory().getPath(), result);
                if (mLoadListener != null) {
                    mData.put(mStore.getDirectory().getPath(), result);
                    mLoadListener.onLoadFinished(mStore, result);
                } else {
                    IDataChangedListener listener = mListeners.get(mStore.getDirectory().getPath());
                    if (listener != null) {
                        listener.onDataChanged(mStore, result);
                    }
                }
            }
        }

        /**
         * 遍历文件夹中的视频文件
         *
         * @param file
         * @param entries
         */
        private void iteratorFile(File file, ArrayList<Uri> entries) {
            if (file.isDirectory()) {
                File[] fileList = file.listFiles(mMusicFileFilter);
                if (fileList != null && fileList.length > 0) {
                    for (File f: fileList) {
                        Uri fileUri = Uri.fromFile(f);
                        entries.add(fileUri);
                    }
                }
                // TODO merge with media file.
                fileList = file.listFiles(mDirFileFilter);
                if (fileList != null) {
                    for (File f : fileList) {
                        iteratorFile(f, entries);
                    }
                }
            }
        }

        private FileFilter mDirFileFilter = new FileFilter() {
            public boolean accept(File file) {
                String externalStoragePath=Environment.getExternalStorageDirectory().getPath();
                if (!mScanExclude.contains(file.getPath())&&externalStoragePath.equals(mStore.getDirectory().getPath()) && file.getPath().equals(externalStoragePath + "/sdcard")) {
                    return false;
                }else {
                    return true;
                }
            }
        };

    }

    private FileFilter mMusicFileFilter = new FileFilter() {
        public boolean accept(File file) {
            return file != null && MusicUtil.isMusicFile(file);
        }
    };

}