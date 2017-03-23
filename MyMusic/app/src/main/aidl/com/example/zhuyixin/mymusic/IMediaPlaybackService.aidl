// IMediaPlaybackService.aidl
package com.example.zhuyixin.mymusic;

// Declare any non-default types here with import statements

interface IMediaPlaybackService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void play();
    void seekTo(in int position);
    void pause();
    void next();
    void previous();
    boolean isPlaying();
    void getLatestInfo();
}
