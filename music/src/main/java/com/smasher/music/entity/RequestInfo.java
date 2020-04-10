package com.smasher.music.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author matao
 * @date 2019/5/16
 */
public class RequestInfo implements Parcelable {

    public static final String REQUEST_TAG = "Request";
    public static final String REQUEST_MEDIA = "Media";

    public static final int COMMAND_START = 0;
    public static final int COMMAND_PLAY = 1;
    public static final int COMMAND_PAUSE = 2;
    public static final int COMMAND_NEXT = 3;
    public static final int COMMAND_PREVIOUS = 4;


    @IntDef({COMMAND_START, COMMAND_PLAY, COMMAND_PAUSE, COMMAND_NEXT, COMMAND_PREVIOUS})
    @Retention(RetentionPolicy.SOURCE)
    @interface RequestType {
    }


    public RequestInfo() {
    }


    @RequestType
    private int mCommandType = 0;


    private MediaInfo mMediaInfo;


    public int getCommandType() {
        return mCommandType;
    }

    public void setCommandType(@RequestType int commandType) {
        mCommandType = commandType;
    }


    public MediaInfo getPath() {
        return mMediaInfo;
    }

    public void setPath(MediaInfo path) {
        mMediaInfo = path;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCommandType);
        dest.writeParcelable(this.mMediaInfo, flags);
    }

    protected RequestInfo(Parcel in) {
        this.mCommandType = in.readInt();
        this.mMediaInfo = in.readParcelable(MediaInfo.class.getClassLoader());
    }

    public static final Parcelable.Creator<RequestInfo> CREATOR = new Parcelable.Creator<RequestInfo>() {
        @Override
        public RequestInfo createFromParcel(Parcel source) {
            return new RequestInfo(source);
        }

        @Override
        public RequestInfo[] newArray(int size) {
            return new RequestInfo[size];
        }
    };
}
