package com.smasher.music.core;

import android.content.Context;

import com.smasher.music.constant.PlayerState;
import com.smasher.music.entity.MediaInfo;
import com.smasher.music.listener.PlayerListener;

/**
 * Created on 2019/5/29.
 *
 * @author moyu
 */
public class MediaPlayerProxy {
    private static final String TAG = "MediaPlayerProxy";
    private CorePlayer mPlayer = null;
    private PlayerListener mListener;
    private int mRealDuration;


    public MediaPlayerProxy(PlayerListener listener) {
        mListener = listener;
    }


    public boolean initPlay(Context context, final MediaInfo info, int duration) {
        boolean initOk = false;
        mRealDuration = duration;
        try {
            if (mPlayer != null) {
                mPlayer.onStop();
            }
            mPlayer = new MusicPlayer(context, info);
            mPlayer.setListener(mListener);
            initOk = mPlayer.onPrepare();
        } catch (Exception e) {
            e.printStackTrace();
            initOk = false;
        }
        return initOk;
    }


    public void close() {
        if (mPlayer != null) {
            mPlayer.onStop();
            mPlayer = null;
        }
    }

    public void play() {
        if (mPlayer != null) {
            mPlayer.onPlay();
        }
    }

    public void stop() {
        if (mPlayer != null) {
            mPlayer.onStop();
        }
    }

    public void pausing() {
        if (mPlayer != null) {
            mPlayer.onPausing();
        }
    }

    public void pause() {
        if (mPlayer != null) {
            mPlayer.onPause();
        }
    }

    public void shutDownPausing() {
        if (mPlayer != null) {
            mPlayer.onShutDownPausing();
        }
    }

    public void resume() {
        if (mPlayer != null) {
            mPlayer.onResume();
        }
    }

    public PlayerState getPlayState() {
        if (mPlayer != null) {
            return mPlayer.getPlayState();
        }
        return PlayerState.PLAY_STATE_CLOSE;
    }

    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        }
        return false;
    }

    public long getDuration() {
        if (mPlayer != null) {
            long duration = mPlayer.getDuration();
            int realDuration = (mRealDuration + 1) * 1000;
            if (realDuration > duration) {
                return mRealDuration * 1000;
            } else {
                return mPlayer.getDuration();
            }
        }
        return 0;
    }

    public long getCurrTime() {
        if (mPlayer != null) {
            return mPlayer.getCurrTime();
        }
        return 0;
    }

    public int getBufferPercent() {
        if (mPlayer != null) {
            return mPlayer.getBufferPercent();
        }
        return 0;
    }

    public long seek(int pos) {
        if (mPlayer != null) {
            return mPlayer.seek(pos);
        }
        return 0;
    }

    public long getBufferLen() {
        if (mPlayer != null) {
            return mPlayer.getBufferLen();
        }
        return 0;
    }

    public long getTotalLen() {
        if (mPlayer != null) {
            return mPlayer.getTotalLen();
        }
        return 0;
    }

    public void setVolume(float vol) {
        if (mPlayer != null) {
            mPlayer.setVolume(vol);
        }
    }


    public void setPlayState(PlayerState state) {
        mPlayer.setState(state);
    }


    private interface IPathSuccessListener {
        void onSuccess();
    }
}
