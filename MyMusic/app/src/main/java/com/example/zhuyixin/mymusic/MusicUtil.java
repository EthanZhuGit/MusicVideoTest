package com.example.zhuyixin.mymusic;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class MusicUtil {
    private static final String TAG = "MusicUtil";


    private static StringBuilder sFormatBuilder = new StringBuilder();
    private static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
    private static final Object[] sTimeArgs = new Object[5];

    public static MusicItem queryMusicFromContentProvider(Context context,Uri fileUri) {
        Log.d(TAG, "queryMusicFromContentProvider: ");
        MusicItem item=null;
        String path=fileUri.getPath();
        Log.d(TAG, "queryMusicFromContentProvider: " + path);
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        Log.d(TAG, "queryMusicFromContentProvider: " + fileName);
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(path);
        Log.d(TAG, "queryMusicFromContentProvider: " + uri);
        String[] searchKey = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM
        };
        String where = MediaStore.Audio.Media.DATA+"=?";

        Uri musicUri;
        String title;
        String album;
        String artist;
        Bitmap bitmap = null;
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, searchKey, where, new String[]{path}, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                musicUri = Uri.withAppendedPath(uri, String.valueOf(id));

                title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));

                int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                Uri albumUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);

                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));

                Log.d(TAG, "queryMusicFromContentProvider: " + id);
                Log.d(TAG, "queryMusicFromContentProvider: " + musicUri.toString());
                Log.d(TAG, "queryMusicFromContentProvider: " + title);
                Log.d(TAG, "queryMusicFromContentProvider: " + albumId);
                Log.d(TAG, "queryMusicFromContentProvider: " + albumUri);
                Log.d(TAG, "queryMusicFromContentProvider: " + artist);
                Log.d(TAG, "queryMusicFromContentProvider: " + album);


                try {
                    InputStream in = resolver.openInputStream(albumUri);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    bitmap = BitmapFactory.decodeStream(in, null, options);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                item = new MusicItem(musicUri,title,fileName,fileUri,artist,album,0,bitmap);
            }
            cursor.close();
        }else {
            musicUri = null;
            title = "";
            artist = "未知歌手";
            album = "未知专辑";
            item = new MusicItem(musicUri,title,fileName,fileUri,artist,album,0,bitmap);
        }
        return item ;
    }


    public static boolean isVideoFile(File file) {
        String name=file.getName().toLowerCase();
        String type = name.substring(name.lastIndexOf(".") + 1);
        return type.equals("mp3") || type.equals("ape") || type.equals("flac");
    }

    public static String makeTimeString(Context context, long secs) {
        String durationformat = context
                .getString(secs < 3600 ? R.string.durationformatshort
                        : R.string.durationformatlong);

        sFormatBuilder.setLength(0);

        final Object[] timeArgs = sTimeArgs;
        timeArgs[0] = secs / 3600;
        timeArgs[1] = secs / 60;
        timeArgs[2] = (secs / 60) % 60;
        timeArgs[3] = secs;
        timeArgs[4] = secs % 60;

        return sFormatter.format(durationformat, timeArgs).toString();
    }
}
