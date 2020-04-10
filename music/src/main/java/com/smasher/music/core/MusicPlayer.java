package com.smasher.music.core;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.smasher.music.constant.PlayerState;
import com.smasher.music.entity.MediaInfo;
import com.smasher.music.helper.AudioFocusHelper;
import com.smasher.music.listener.PlayerListener;

import java.io.IOException;

/**
 * @author matao
 * @date 2019/5/25
 */
public class MusicPlayer extends CorePlayer {

    private static final String TAG = "AudioPlayer";

    private String mPlayUri;

    public MusicPlayer(Context context, MediaInfo mediaInfo) {
        super(context, mediaInfo);
        mPlayUri = mediaInfo.getUrl();
    }


    public MusicPlayer(Context context, MediaInfo mediaInfo, String path) {
        super(context, mediaInfo);
        if (path == null) {
            mPlayUri = mediaInfo.getUrl();
        } else {
            mPlayUri = path;
        }
    }

    @Override
    protected boolean onPrepare() {
        return prepare();
    }


    @Override
    protected void onPlay() {
        if (mPlayer != null) {
            mPlayer.start();
            setState(PlayerState.PLAY_STATE_PLAY);
        }

    }

    @Override
    protected void onPause() {
        if (mPlayer != null && mIsInitialized) {
            mPlayer.pause();
            setState(PlayerState.PLAY_STATE_PAUSE);
        }
    }

    @Override
    protected void onPausing() {
        if (mPlayer != null) {
            setState(PlayerState.PLAY_STATE_PAUSING);
        }

    }

    @Override
    protected void onResume() {
        if (mPlayer != null) {
            mPlayer.start();
            setState(PlayerState.PLAY_STATE_PLAY);
        }
    }

    @Override
    protected void onShutDownPausing() {
        if (mPlayer != null) {
            setState(PlayerState.PLAY_STATE_PLAY);
        }
    }

    @Override
    protected void onStop() {
        mPlayState = PlayerState.PLAY_STATE_STOP;
        if (mIsInitialized) {
            mPlayer.release();
            mPlayer = null;
            mIsInitialized = false;
        }
        mContext = null;
    }

    @Override
    protected long getDuration() {
        if (mPlayer != null) {
            return mPlayer.getDuration();
        }
        return 0;
    }

    @Override
    protected long getCurrTime() {
        if (mPlayer != null) {
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    protected long seek(int pos) {
        if (mPlayer != null) {
            mPlayer.seekTo(pos);
            return pos;
        }
        return 0;
    }

    @Override
    protected long getBufferLen() {
        return 100;
    }

    @Override
    protected long getTotalLen() {
        return 100;
    }

    @Override
    protected boolean isPlaying() {
        return mPlayState == PlayerState.PLAY_STATE_PLAY;
    }

    @Override
    protected int getBufferPercent() {
        return 100;
    }

    @Override
    protected void onCompletionLogic(MediaPlayer mediaPlayer) {
        notifyEvent(PlayerListener.PLAY_EVENT_END, 0, null);
    }


    private boolean prepare() {
        try {
            if (mPlayer == null) {
                return false;
            }

            mPlayer.reset();

            AudioFocusHelper.getInstance().setAudioStreamType(mPlayer);
            mPlayer.setDataSource(mPlayUri);
            mPlayer.prepare();
            mIsInitialized = true;
        } catch (IOException e) {
            mIsInitialized = false;
            e.printStackTrace();
        }

        return mIsInitialized;
    }

}
