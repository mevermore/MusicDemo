package com.smasher.music.loader;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.util.Log;

import com.smasher.music.entity.MediaInfo;
import com.smasher.music.thread.IExecutor;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author matao
 * @date 2019/5/24
 */
public class MusicLoader implements Handler.Callback {

    private static final String TAG = "MusicLoader";
    private static final int COMPLETE = 0;
    private static final int RETURN_RESULT = 1;

    private static MusicLoader OUR_INSTANCE;


    public static MusicLoader getInstance(ContentResolver resolver) {
        if (OUR_INSTANCE == null) {
            mResolver = resolver;
            OUR_INSTANCE = new MusicLoader();
        }
        return OUR_INSTANCE;
    }

    private MusicLoader() {
        mHandler = new Handler(this);
    }


    /**
     * 音频库的Uri
     */
    private static Uri mAudioUri = Audio.Media.EXTERNAL_CONTENT_URI;

    private ArrayList<MediaInfo> musicList = new ArrayList<>();

    private Future<ArrayList<MediaInfo>> submitFuture;

    private Handler mHandler;

    private Callback mCallback;

    /**
     * 声明一个内容解析器对象
     */
    private static ContentResolver mResolver;

    private static String[] mMediaColumn = new String[]{
            // 编号
            MediaStore.Audio.Media._ID,
            // 乐曲名
            Audio.Media.TITLE,
            // 专辑名
            Audio.Media.ALBUM,
            // 播放时长
            Audio.Media.DURATION,
            // 文件大小
            Audio.Media.SIZE,
            // 演唱者
            Audio.Media.ARTIST,
            // 文件路径
            Audio.Media.DATA
    };


    /**
     * 获取音乐队列
     */
    public ArrayList<MediaInfo> getMusicList() {
        return musicList;
    }


    public void getMusicList(Callback callback) {
        mCallback = callback;
        getInfos();
    }


    public void getInfos() {

        submitFuture = IExecutor.getInstance().submitFuture(() -> {

            ArrayList<MediaInfo> list = null;

            Cursor cursor = null;
            try {
                // 通过内容解析器查询系统的音频库，并返回结果集的游标
                cursor = mResolver.query(mAudioUri, mMediaColumn, null, null, null);
                if (cursor == null) {
                    return null;
                }

                list = new ArrayList<>();

                while (cursor.moveToNext()) {
                    MediaInfo music = new MediaInfo();
                    music.setId(cursor.getLong(0));
                    music.setTitle(cursor.getString(1));
                    music.setAlbum(cursor.getString(2));
                    music.setDuration(cursor.getInt(3));
                    music.setSize(cursor.getLong(4));
                    music.setArtist(cursor.getString(5));
                    music.setUrl(cursor.getString(6));
                    Log.d(TAG, music.getTitle() + " " + music.getDuration());
                    list.add(music);
                }
                cursor.close();


                String foldPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();

                mHandler.sendEmptyMessage(COMPLETE);
            } catch (Exception e) {
                e.printStackTrace();
                if (cursor != null) {
                    cursor.close();
                }
            }
            return list;
        });
    }


    private void getResult() {
        mHandler.postDelayed(() -> {
            try {
                if (submitFuture.isDone()) {
                    musicList = submitFuture.get();
                }
                mHandler.sendEmptyMessage(RETURN_RESULT);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 500);
    }


    @Override
    public boolean handleMessage(Message msg) {
        int id = msg.what;
        switch (id) {
            case COMPLETE:
                getResult();
                break;
            case RETURN_RESULT:
                mCallback.onResult(musicList);
                break;
            default:
                break;
        }
        return false;
    }


    public interface Callback {
        void onResult(ArrayList<MediaInfo> list);
    }


}
