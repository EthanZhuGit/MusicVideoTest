package com.example.zhuyixin.mymusic;

import android.net.Uri;

import java.io.File;

public interface Store {

    public Uri getUri();
//    public int getStorageType();
    public File getDirectory();
    public boolean mounted();
    
}
