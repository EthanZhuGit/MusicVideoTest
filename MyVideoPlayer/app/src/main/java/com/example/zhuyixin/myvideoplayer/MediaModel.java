package com.example.zhuyixin.myvideoplayer;

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
    //private ListFileTask mListTask;
    private ArrayList<String> mScanExclude = new ArrayList<String>();
    private HashMap<String, IDataChangedListener> mListeners = new HashMap<String, IDataChangedListener>();

    private HashMap<String, ArrayList<VideoEntry>> mData = new HashMap<String, ArrayList<VideoEntry>>();

    private MediaModel() {
        loadExcludeConfig();
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

    public ArrayList<VideoEntry> startAllVideoLoader(Store store, OnAllVideoLoadListener listener) {
        ArrayList<VideoEntry> list = mData.get(store.getDirectory().getPath());
        if (list != null) {
            // get from cache.
            return list;
        } else {
            mLoadTask = new LoadTask(store, listener);
            mLoadTask.execute(store.getDirectory());
            return null;
        }
    }

    /*
    public void startFolderVideoLoader(Uri uri, OnFolderVideoLoadListener listener) {
        mListTask = new ListFileTask(uri, listener);
        mListTask.execute(uri);
    }
    */

    public ArrayList<VideoEntry> getAllVideoList(Store store) {
        return mData.get(store.getDirectory().getPath());
    }

    public void resetData(Store store) {
        if (mLoadTask != null && mLoadTask.mStore.equals(store)) {
            mLoadTask.cancel(true);
        }
        mData.remove(store.getDirectory().getPath());
    }

    /**
     * 当存储器插拔后更新数据
     * @param store 存储器对象，若为插入设备，则调用loadTask读取，若为null，
     * @param storageVolume
     * @param mounted
     */
    public void updateData(Store store, Uri storageVolume, boolean mounted) {
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
        public void onDataChanaged(Store store, ArrayList<VideoEntry> dataList);
    }

    /**
     * {@link com.example.zhuyixin.myvideoplayer.MediaFragment}
     */
    public interface OnAllVideoLoadListener {
        public void onLoadFinished(Store store, ArrayList<VideoEntry> dataList);
    }

    /*
    public interface OnFolderVideoLoadListener {
        public void onLoadFinished(Uri uri, ArrayList<Uri> dataList);
    }
    */
    private class LoadTask extends AsyncTask<File, Integer, ArrayList<VideoEntry>> {

        private Store mStore;
        private OnAllVideoLoadListener mLoadListener;

        public LoadTask(Store store, OnAllVideoLoadListener listener) {
            this.mStore = store;
            this.mLoadListener = listener;
        }

        @Override
        protected ArrayList<VideoEntry> doInBackground(File... params) {
            Log.d(TAG, "doInBackground...");
            ArrayList<VideoEntry> entries = new ArrayList<VideoEntry>();
            iteratorFile(params[0], entries);
            return entries;
        }

        @Override
        protected void onPostExecute(ArrayList<VideoEntry> result) {
            Log.d(TAG, "onPostExecute");
            if (!isCancelled()) {
                mData.put(mStore.getDirectory().getPath(), result);
                if (mLoadListener != null) {
                    mData.put(mStore.getDirectory().getPath(), result);
                    mLoadListener.onLoadFinished(mStore, result);
                } else {
                    IDataChangedListener listener = mListeners.get(mStore.getDirectory().getPath());
                    if (listener != null) {
                        listener.onDataChanaged(mStore, result);
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
        private void iteratorFile(File file, ArrayList<VideoEntry> entries) {
            if (file.isDirectory()) {
                File[] fileList = file.listFiles(mVideoFileFilter);
                if (fileList != null && fileList.length > 0) {
                    VideoEntry videoEntry = new VideoEntry(file);
                    entries.add(videoEntry);
                    for (int i = 0; i < fileList.length; i++) {
                        Uri fileUri = Uri.fromFile(fileList[i]);
                        videoEntry.addVideo(fileUri);
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
//                if (file != null && file.isDirectory()
//                        && !mScanExclude.contains(file.getName())) {
//                    if (mStore.getStorageType() == Store.TYPE_HD
//                            && Environment.getExternalStorageSdcardDirectory().getPath().equals(file.getPath())) {
//                        return false;
//                    } else {
//                        return true;
//                    }
//                    return mStore.getStorageType()==Store.TYPE_HD;
//                } else {
//                    return false;
//                }
                return true;
            }
        };

    }

    /*
    private class ListFileTask extends AsyncTask<Uri, Void, ArrayList<Uri>> {

        private Uri mUri;
        private OnFolderVideoLoadListener mListener;
        
        public ListFileTask(Uri uri, OnFolderVideoLoadListener listener) {
            this.mUri = uri;
            this.mListener = listener;
        }
        
        protected ArrayList<Uri> doInBackground(Uri... uris) {
            ArrayList<Uri> listFiles = new ArrayList<Uri>();

            File f = new File(uris[0].getPath());
            File p = f.getParentFile();
            File[] fs = p.listFiles(mVideoFileFilter);
            if (fs != null) {
                for (int i = 0; i < fs.length; i++) {
                    Uri u = Uri.fromFile(fs[i]);
                    listFiles.add(u);
                }
            }
            return listFiles;
        }

        protected void onPostExecute(ArrayList<Uri> result) {
            if (!isCancelled()) {
                if (mListener != null) {
                    mListener.onLoadFinished(mUri, result);
                }
            }
        }
    }
    */

    private FileFilter mVideoFileFilter = new FileFilter() {
        public boolean accept(File file) {
            return file != null && VideoUtils.isVideoFile(file);
        }
    };

}