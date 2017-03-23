package com.example.zhuyixin.mymusic;

import android.graphics.Bitmap;
import android.net.Uri;

public class MusicItem {

    private Uri fileUri;
    private String fileName;
    private Uri songUri;
    private String title;
    private String artist;
    private String album;
    private Bitmap image;
    private long playedTime;
    MusicItem(Uri songUri, String title, String fileName,Uri fileUri, String artist,String album, long playedTime,Bitmap image) {
        this.title = title;
        this.fileName=fileName;
        this.fileUri=fileUri;
        this.artist=artist;
        this.songUri = songUri;
        this.playedTime = playedTime;
        this.album=album;
        this.image=image;
    }

    public String getTitle() {
        return title;
    }

    public String getFileName() {
        return fileName;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    @Override
    public boolean equals(Object o) {
        MusicItem another = (MusicItem) o;

        return another.songUri.equals(this.songUri);
    }
}