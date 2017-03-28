package com.example.zhuyixin.myvideoplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class StoreManager {
    private static final String TAG ="TAG "+  StoreManager.class.getSimpleName();
    
    private static StoreManager sStoreManager = null;
    
    public static final String STORE_NAME           = "VIDEO";
    public static final String KEY_STORE_TYPE       = "KEY_STORE_TYPE";
    public static final String KEY_SCREEN_MODE      = "KEY_SCREEN_MODE";
    public static final String KEY_CUR_URI          = "KEY_CUR_URI";
    public static final String KEY_DUR_POSITION     = "KEY_DUR_POSITION";
    public static final String KEY_ALL_VIDEO        = "KEY_ALL_VIDEO";
    
    private Context mContext;
    private SharedPreferences mPref;
    private ArrayList<Store> mStoreList = new ArrayList<Store>();
    private ArrayList<IStoreChangedListener> mListeners = new ArrayList<IStoreChangedListener>();

    /**
     * 获取StoreManager的实例。
     * 向存储器列表mStoreList添加hd。
     * 注册监听器 存储卡拔插。
     * @param context
     * @return
     */
    public static synchronized StoreManager getInstance(Context context) {
        if (sStoreManager == null) {
            sStoreManager = new StoreManager(context);
        }
        return sStoreManager;
    }

    /**
     * 向存储器列表mStoreList添加 ExternalStorage
     * 监听存储器拔插
     * @param context
     */
    private StoreManager(Context context) {
        mContext = context;
        mPref = context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE);
        mStoreList.addAll(listAvailableStorage(context));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addDataScheme("file");
        mContext.getApplicationContext().registerReceiver(mReceiver, intentFilter);
    }

    /**
     * 利用反射获取启动前已经挂载的存储设备
     * @param context
     * @return
     */
    public  ArrayList<MediaStoreBase> listAvailableStorage(Context context) {
        ArrayList<MediaStoreBase> stores = new ArrayList<>();
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Class<?>[] paramClasses = {};
            Method getVolumeList = StorageManager.class.getMethod("getVolumeList", paramClasses);
            getVolumeList.setAccessible(true);
            Object[] params = {};
            Object[] invokes = (Object[]) getVolumeList.invoke(storageManager, params);
            if (invokes != null) {

                for (int i = 0; i < invokes.length; i++) {
                    Object obj = invokes[i];
                    Method getPath = obj.getClass().getMethod("getPath", new Class[0]);
                    String path = (String) getPath.invoke(obj, new Object[0]);
                    Log.d(TAG, "listAvailableStorage: " + path);
                    String state = null;
                    try {
                        Method getVolumeState = StorageManager.class.getMethod("getVolumeState", String.class);
                        state = (String) getVolumeState.invoke(storageManager,path);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (Environment.MEDIA_MOUNTED.equals(state)) {
                        File file = new File(path);
                        Uri uri=Uri.fromFile(file);
                        MediaStoreBase info = new MediaStoreBase(uri, file, true);
                        stores.add(info);
                    }
                }
            }
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        stores.trimToSize();
        return stores;
    }
    /**
     * 接受存储卡拔插广播后 调用MediaModel的updateData()
     * 触发IStoreChangedListener，即让mListener链表中的所有IStoreChangeListener对象都调用onStoreChanged()
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            boolean mounted = Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction());
            Uri storageVolume = intent.getData();
            Log.d(TAG, "onReceive: " + intent.getAction());
            Log.d(TAG, "onReceive: " + storageVolume);
            Store store = new MediaStoreBase(storageVolume, new File(storageVolume.getPath()), mounted);
            if (mounted){
                mStoreList.add(store);
            }else {
                if (mStoreList.contains(store)) {
                    mStoreList.remove(store);
                }
            }
            for(IStoreChangedListener listener : mListeners) {
                listener.onStoreChanged(storageVolume, mounted);
            }
            MediaModel.getInstance().updateData(getMediaStore(storageVolume), storageVolume, mounted);
        }
    };

    public interface IStoreChangedListener {
        public void onStoreChanged(Uri storageVolume, boolean mounted);
    }

    
    public void registerStoreChangedListener(IStoreChangedListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        } else {
            Log.d(TAG, "register same listener, ignore it");
        }
    }
    
    public void unregisterStoreChangedListener(IStoreChangedListener listener) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        } else {
            Log.d(TAG, "unregister does not existed listener");
        }
    }
    
    public ArrayList<Store> getStoreList() {
        return mStoreList;
    }
    

    
    public Store getMediaStore(Uri fileUri) {
        String filePath = fileUri.getPath();
        for(Store store : mStoreList) {
            if(filePath.startsWith(store.getDirectory().getPath())) {
                return store;
            }
        }
        return null;
    }




    
}
