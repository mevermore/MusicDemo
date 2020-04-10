package com.smasher.music.core;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.PowerManager;

import com.smasher.music.constant.PlayerState;
import com.smasher.music.entity.MediaInfo;
import com.smasher.music.listener.PlayerListener;

/**
 * @author moyu
 */
public abstract class CorePlayer extends Thread implements
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    MediaPlayer mPlayer;
    Context mContext;
    PlayerState mPlayState = PlayerState.PLAY_STATE_PLAY;
    boolean mIsInitialized;

    private MediaInfo mMediaInfo;
    private PlayerListener mListener;

    public CorePlayer(Context context, MediaInfo mediaInfo) {

        mContext = context;
        mMediaInfo = mediaInfo;

        mPlayer = new MediaPlayer();
        mPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnCompletionListener(this);
    }


    public void setListener(PlayerListener listener) {
        mListener = listener;
    }


    protected final void notifyEvent(int what, int subWhat, Object ex) {
        if (mListener != null) {
            mListener.notifyEvent(what, subWhat, ex);
        }

    }

    protected void setVolume(float vol) {
        try {
            mPlayer.setVolume(vol, vol);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected boolean isInitialized() {
        return mIsInitialized;
    }

    protected void setState(PlayerState state) {
        mPlayState = state;
    }

    protected PlayerState getPlayState() {
        return mPlayState;
    }

    protected abstract boolean onPrepare();

    protected abstract void onPlay();

    protected abstract void onPause();

    protected abstract void onPausing();

    protected abstract void onResume();

    protected abstract void onShutDownPausing();

    protected abstract void onStop();

    protected abstract long getDuration();

    protected abstract long getCurrTime();

    protected abstract long seek(int pos);

    protected abstract long getBufferLen();

    protected abstract long getTotalLen();

    protected abstract boolean isPlaying();

    protected abstract int getBufferPercent();

    protected abstract void onCompletionLogic(MediaPlayer mp);


    @Override
    public void onCompletion(MediaPlayer mp) {
        onCompletionLogic(mp);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                notifyEvent(PlayerListener.PLAY_EVENT_ERROR, 0, mMediaInfo);
                return true;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                break;
            default:
                break;
        }
        return false;
    }
}
