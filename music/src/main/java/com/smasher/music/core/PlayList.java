package com.smasher.music.core;


import android.util.Log;

import com.smasher.music.entity.MediaInfo;
import com.smasher.music.listener.PlayerListener;
import com.smasher.music.util.AudioUtil;

/**
 * 播放列表 支持单曲、列表、随机、循环、不循环
 *
 * @author xxx
 */
public class PlayList {
    private static final String TAG = "PlayList";


    // play mode
    public static final int PLAY_MODE_ONESHOT = 10;// ??????锛??寰??锛?
    public static final int PLAY_MODE_ONESHOT_REPEAT = 11;// ???寰??
    public static final int PLAY_MODE_LIST = 12;// ??〃椤哄?
    public static final int PLAY_MODE_LIST_REPEAT = 13;// ??〃寰??
    public static final int PLAY_MODE_LIST_SHUFFLE = 14;// ??〃???锛??寰??锛?
    public static final int PLAY_MODE_LIST_SHUFFLE_REPEAT = 15;// ??〃寰?????


    private int mPlayListLen = 0;
    private MediaInfo[] mPlayList = null;
    private int mPlayPos = -1;
    private int mLastPos = -1; // 播放的上一个歌曲。

    private int[] mShuffleList = null;

    private boolean mShuffle = false; // ..不随机/随机
    private boolean mRepeat = true; // ..不重复/重复
    private boolean mOneShot = false; // ..单曲/列表

    // //..当替换playlist，而被删除的歌曲正在播放的时候的备份SongInfo。

    // NotifyChange的回调
    private PlayerListener listener = null;

    public void setOnNotifyChangeListener(PlayerListener listener) {
        this.listener = listener;
    }

    private void notifyChange(int what) {
        if (this.listener != null) {
            listener.notifyEvent(what, 0, null);
        }
    }

    public void playPos(int pos) {
        if (pos < 0 || pos >= mPlayListLen) {
            return;
        }

        if (mShuffle) {
            // 找到正在播放的点。
            for (int i = 0; i < mPlayListLen; i++) {
                if (mShuffleList[i] == pos) {
                    setPlayPos(i);
                    break;
                }
            }
        } else {
            setPlayPos(pos);
        }
    }

    /**
     * 设置当前播放点，并保存Last位置。这个代码很危险，用的要小心，因为是无视是否随机模式的。
     */
    public void setPlayPos(int pos) {
        mLastPos = mPlayPos;
        mPlayPos = pos;
    }

    public int getPlayPos() {
        return mPlayPos;
    }

    public boolean getRepeatMode() {
        return mRepeat;
    }

    public void setRepeatMode(boolean repeatmode) {
        mRepeat = repeatmode;
    }

    public boolean getShuffleMode() {
        return mShuffle;
    }

    public void setShuffleMode(boolean shuffle) {
        // 对于单首歌曲不存在随机模式。
        if (mOneShot && !mShuffle) {
            return;
        }

        if (shuffle == mShuffle) {
            return;
        }

        if (shuffle) {
            boolean needFindPos = currentPosValid();
            int oldIndex = getCurrIndex();

            mShuffle = true;

            buildShuffleList();

            // 恢复到正在播放的点。
            if (needFindPos) {
                for (int i = 0; i < mPlayListLen; i++) {
                    if (mShuffleList[i] == oldIndex) {
                        setPlayPos(i);
                        break;
                    }
                }
            }
        } else {
            if (currentPosValid()) {
                // 其实并没有改变mPlayPos。
                // 顺序往下走罗
                mPlayPos = mShuffleList[mPlayPos];
            }
        }

        mShuffle = shuffle;
    }

    public boolean getOnShotMode() {
        return mOneShot;
    }

    public void setOneShotMode(boolean oneShotMode) {
        mOneShot = oneShotMode;
    }

    public int getCurrIndex() {
        if (currentPosValid()) {
            return mShuffle ? mShuffleList[mPlayPos] : mPlayPos;
        } else {
            return -1;
        }
    }

    public int getListSize() {
        return mPlayListLen;
    }

    /**
     * 清除Playlist。
     */
    public void clear() {
        mPlayPos = mLastPos = 0;
        mPlayListLen = 0;
        // mBackPlayingSong = null;
    }

    // public String toString() {
    // String info = new String();
    // info += "{";
    // for (int i = 0; i < mPlayListLen; i++) {
    // if (mPlayList[i] != null)
    // info += mPlayList[i].getName();
    // else
    // info += "null";
    // info += "/";
    // }
    // info += "}";
    // return info;
    // }

    public MediaInfo getCurrent() {
        // MLog.i(QQMusicServiceHelper.TAG,
        // "[QQPlayerService]getCurrent mPlayPos="+mPlayPos+" length="+mPlayListLen+" getCurrIndex()="+getCurrIndex());
        /*
         * if(mBackPlayingSong != null) return mBackPlayingSong;
         */

        if (currentPosValid()) {
            return mPlayList[getCurrIndex()];
        } else {
            return null;
        }
    }

    public MediaInfo getNext() {
        if (mPlayPos + 1 >= 0 && mPlayPos + 1 < mPlayListLen) {
            return mPlayList.length > mPlayPos + 1 ? mPlayList[mPlayPos + 1] : null;
        } else {
            return null;
        }
    }

    public MediaInfo getPre() {
        if (mPlayPos - 1 >= 0 && mPlayPos - 1 < mPlayListLen) {
            return mPlayList.length > mPlayPos - 1 ? mPlayList[mPlayPos - 1] : null;
        } else {
            return null;
        }
    }

    public boolean currentPosValid() {
        Log.d(TAG, "currentPosValid: mPlayListLen=" + mPlayListLen + " mPlayPos=" + mPlayPos);
        return (mPlayPos >= 0 && mPlayPos < mPlayListLen);
    }

    /**
     * 函数作用：往后移动当前播放mPlayPos，获取方法按照mShuffle、mRepeat 和 mOneShot。 bEnd
     * 是否是播放结束结束需要下一首 单曲循环下 播放结束后还是播放该首
     */
    public void moveToNext(boolean bEnd) {
        /*
         * if(mBackPlayingSong != null){ mBackPlayingSong = null; mPlayPos =
         * mPlayListLen -1;//因为移动到下一个，所以直接定位为最后一个，汗 }
         */

        if (!currentPosValid()) {
            return;
        }

        // 保存下last
        mLastPos = mPlayPos;
        if (mOneShot) {
            // 单曲播放
            if (!mRepeat) {
                mPlayPos = -1;
            } else {
                // 单曲重复模式,跟列表重复一样要上下选歌
                if (!bEnd) {
                    mPlayPos++;
                    if (mPlayPos >= mPlayListLen) {
                        mPlayPos = 0;
                    }
                }
            }
        } else {
            // 列表播放
            mPlayPos++;
            if (mPlayPos >= mPlayListLen) {
                if (mRepeat) {
                    if (mShuffle) {
                        // 重新构建随机播放列表
                        buildShuffleList();
                    }
                    mPlayPos = 0;
                } else {
                    mPlayPos = 0;
                }
            }
        }

    }

    /**
     * 函数作用：往前移动当前播放mPlayPos，获取方法按照mShuffle、mRepeat 和 mOneShot。
     */
    public void moveToPre() {


        /*
         * if(mBackPlayingSong != null){ mBackPlayingSong = null; mPlayPos =
         * 1;//因为移动到上一个，所以直接定位为第二个 }
         */

        if (!currentPosValid()) {
            return;
        }

        // 保存下last
        mLastPos = mPlayPos;
        if (mOneShot) {// 单曲播放
            if (!mRepeat) {
                mPlayPos = -1;
            } else { // 单曲重复模式,跟列表重复一样要上下选歌
                mPlayPos--;
                if (mPlayPos < 0) {
                    mPlayPos = mPlayListLen - 1;
                }
            }
        } else {// 列表播放
            mPlayPos--;
            if (mPlayPos < 0) {
                if (mRepeat) {
                    if (mShuffle) {
                        // 重新构建随机播放列表
                        buildShuffleList();
                    }
                    mPlayPos = mPlayListLen - 1;
                } else {
                    mPlayPos = -1;
                }
            }
        }

    }

    private void buildShuffleList() {
        mShuffleList = AudioUtil.randomList(mPlayListLen);
    }

    /**
     * 扩展mPlayList，并拷贝之前的数据在mPlayList中。
     */
    private void ensurePlayListCapacity(int size) {

        if (mPlayList == null || size > mPlayList.length) {
            MediaInfo[] newlist = new MediaInfo[size];
            int len = mPlayListLen;
            for (int i = 0; i < len; i++) {
                newlist[i] = mPlayList[i];
            }
            mPlayList = newlist;
        }
    }

    public MediaInfo[] getList() {
        synchronized (this) {

            return mPlayList;
        }
    }

    /**
     * 重置list,并找到当前正在播放的点，如果找不到，则直接选择第一首歌曲（如果是随机模式，则选择随机列表中的第一首歌曲）
     */
    public boolean replaceList(MediaInfo[] list) {

        boolean gotonext = false;

        if (list == null) {
            mPlayListLen = 0;
            mPlayList = null;
            notifyChange(PlayerListener.PLAY_EVENT_PLAYLIST_CHANGED);
            gotonext = true;
            return gotonext;
        }

        int listlength = list.length;
        boolean newlist = true;
        if (mPlayListLen == listlength) {
            newlist = false;
            // 如果相同，则不做任何处理
            for (int i = 0; i < listlength; i++) {
                if (list[i] != mPlayList[i]) {
                    newlist = true;
                    break;
                }
            }
        }


        if (newlist) {
            // 保留下正在播放的那个
            MediaInfo back = beginBack();

            addToPlayList(list, -1);
            // 直接覆盖掉
            if (mShuffle) {
                buildShuffleList();
            }

            gotonext = endBack(back);

            notifyChange(PlayerListener.PLAY_EVENT_PLAYLIST_CHANGED);
        }
        return gotonext;
    }

    // 注意：这2个函数需要成对调用，beginBack的返回值为endBack的参数，endBack的返回值为是否需要调用 gotonext
    private MediaInfo beginBack() {
        MediaInfo back = null;
        if (currentPosValid()) {
            back = getCurrent();

        }

        return back;
    }

    private boolean endBack(MediaInfo back) {
        boolean gotonext = false;
        setPlayPos(0);

        if (back != null) {
            gotonext = true;

            // 恢复正在播放的那个。
            for (int i = 0; i < mPlayListLen; i++) {
                if (back.equals(mPlayList[i])) {
                    gotonext = false;
                    playPos(i);
                }
            }
            back = null;

            if (gotonext) {
                //暂时注释掉，因为qq听书没有repeat模式
                //gotonext = gotonext && !mOneShot && mRepeat;
                gotonext = gotonext && !mOneShot;
            }
        }
        return gotonext;
    }

    /*
     * insert the list of songs at the specified position in the playlist
     */
    public void addToPlayList(MediaInfo[] list, int position) {


        int addlen = list.length;
        if (position < 0) { // overwrite
            mPlayListLen = 0;
            position = 0;
        }
        ensurePlayListCapacity(mPlayListLen + addlen);
        if (position > mPlayListLen) {
            position = mPlayListLen;
        }

        // move part of list after insertion point
        int tailsize = mPlayListLen - position;
        for (int i = tailsize; i > 0; i--) {
            mPlayList[position + i] = mPlayList[position + i - addlen];
        }

        // copy list into playlist
        for (int i = 0; i < addlen; i++) {
            mPlayList[position + i] = list[i];
        }
        mPlayListLen += addlen;

        for (int i = mPlayListLen; i < mPlayList.length; i++) {
            mPlayList[i] = null;
        }
        if (mPlayPos >= position) {
            mPlayPos++;
        }
        if (mShuffle) {
            buildShuffleList();
        }
    }

    public boolean erase(int index) {
        if (index < 0 || index > mPlayListLen) {
            return false;
        }

        boolean gotonext = false;


        synchronized (this) {
            // 保留下正在播放的那个
            MediaInfo back = beginBack();

            int num = mPlayListLen - index - 1;
            for (int i = 0; i < num; i++) {
                mPlayList[index + i] = mPlayList[index + i + 1];
            }

            mPlayListLen -= 1;

            if (mShuffle) {
                buildShuffleList();
            }

            gotonext = endBack(back);

            notifyChange(PlayerListener.PLAY_EVENT_PLAYLIST_CHANGED);

            return gotonext;
        }
    }

    public int getSongInfo(MediaInfo song) {
        int index = -1;
        synchronized (this) {
            if (mPlayList != null && mPlayList.length > 0) {
                for (int i = 0; i < mPlayList.length; i++) {
                    if (song.equals(mPlayList[i])) {
                        index = i;
                        break;
                    }
                }
            }
        }
        return index;
    }
}
