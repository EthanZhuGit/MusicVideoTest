package com.example.zhuyixin.mymusic;

import android.net.Uri;

import java.io.File;
import java.util.HashMap;

public class MediaStoreBase implements Store{
    private Uri uri;
    //    private int type;
    private File directory;
    private boolean mounted;

    public MediaStoreBase(Uri uri, File directory, boolean mounted) {
        this.uri = uri;
//        this.type=type;
        this.directory = directory;
        this.mounted = mounted;
    }

    public Uri getUri() {
        return uri;
    }

//    public int getStorageType() {
//        return type;
//    }

    public File getDirectory() {
        return directory;
    }

    public boolean mounted() {
        return mounted;
    }

//    public String toString() {
//        if (getDirectory() == null) {
//          return String.valueOf(getStorageType());
//       } else {
//          return getDirectory().getPath();
//       }
//    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MediaStoreBase)) {
            return false;
        }
        MediaStoreBase mediaStoreBase= (MediaStoreBase) obj;
        return uri.toString().equals(mediaStoreBase.getUri().toString());
    }
}
