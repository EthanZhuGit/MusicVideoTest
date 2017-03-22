package com.example.zhuyixin.myvideoplayer;

import android.net.Uri;

import java.io.File;

public interface Store {
    public Uri getUri();
    public File getDirectory();
    public boolean mounted();
    
}
