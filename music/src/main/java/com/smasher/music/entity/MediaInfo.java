package com.smasher.music.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

/**
 * @author matao
 */
public class MediaInfo implements Parcelable {

    /**
     * 编号
     */
    private long id;

    /**
     * 乐曲名
     */
    private String title;

    /**
     * 专辑名
     */
    private String album;

    /**
     * 播放时长
     */
    private int duration;

    /**
     * 文件大小
     */
    private long size;

    /**
     * 演唱者
     */
    private String artist;

    /**
     * 文件路径
     */
    private String url;

    public MediaInfo() {
    }

    public MediaInfo(String title, String artist, String url) {
        this.title = title;
        this.artist = artist;
        this.url = url;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(album);
        dest.writeString(artist);
        dest.writeString(url);
        dest.writeInt(duration);
        dest.writeLong(size);
    }

    public static final Creator<MediaInfo>
            CREATOR = new Creator<MediaInfo>() {

        @Override
        public MediaInfo[] newArray(int size) {
            return new MediaInfo[size];
        }

        @Override
        public MediaInfo createFromParcel(Parcel source) {
            MediaInfo mediaInfo = new MediaInfo();
            mediaInfo.setId(source.readLong());
            mediaInfo.setTitle(source.readString());
            mediaInfo.setAlbum(source.readString());
            mediaInfo.setArtist(source.readString());
            mediaInfo.setUrl(source.readString());
            mediaInfo.setDuration(source.readInt());
            mediaInfo.setSize(source.readLong());
            return mediaInfo;
        }
    };


    @Override
    public boolean equals(@Nullable Object obj) {

        if (obj instanceof MediaInfo) {
            return ((MediaInfo) obj).getTitle().equals(this.getTitle());
        }
        return super.equals(obj);
    }
}
