package com.smasher.music.helper;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import com.smasher.music.IMusicService;

/**
 * @author matao
 * @date 2019/5/25
 */
public class AudioFocusHelper implements AudioManager.OnAudioFocusChangeListener {


    private static final String TAG = "AudioFocusHelper";
    private static AudioFocusHelper INSTANCE = null;
    private boolean isInit = false;

    public static synchronized AudioFocusHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AudioFocusHelper();
        }
        return INSTANCE;
    }


    private IMusicService mPlayer;
    private AudioManager mAudioManager;
    private AudioAttributes mAudioAttributes;
    private AudioFocusRequest.Builder mFocusRequestBuilder;
    private Handler mHandler;
    private boolean mPausedByTransientLossOfFocus;


    public AudioFocusHelper() {
        mHandler = new Handler();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes.Builder builder = new AudioAttributes.Builder();
            builder.setUsage(AudioAttributes.USAGE_MEDIA);
            builder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
            mAudioAttributes = builder.build();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mFocusRequestBuilder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(mAudioAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(true);
        }
    }


    public void init(Context context, IMusicService player) {
        if (!isInit) {
            mPlayer = player;
            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            isInit = true;
        }
    }


    public void setAudioStreamType(MediaPlayer mediaPlayer) {
        if (mediaPlayer == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaPlayer.setAudioAttributes(mAudioAttributes);
        } else {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }


    public boolean requestFocus() {
        if (mAudioManager == null) {
            Log.e(TAG, "requestFocus: is not init yet");
            return false;
        }
        int focus;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mFocusRequestBuilder != null) {
            mFocusRequestBuilder.setOnAudioFocusChangeListener(this, mHandler);
            AudioFocusRequest audioFocusRequest = mFocusRequestBuilder.build();
            focus = mAudioManager.requestAudioFocus(audioFocusRequest);
        } else {
            focus = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        return focus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }


    public boolean abandonFocus() {
        if (mAudioManager == null) {
            Log.e(TAG, "requestFocus: is not init yet");
            return false;
        }
        int focus;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mFocusRequestBuilder != null) {
            mFocusRequestBuilder.setOnAudioFocusChangeListener(this, mHandler);
            AudioFocusRequest audioFocusRequest = mFocusRequestBuilder.build();
            focus = mAudioManager.abandonAudioFocusRequest(audioFocusRequest);
        } else {
            focus = mAudioManager.abandonAudioFocus(this);
        }
        return focus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }


    @Override
    public void onAudioFocusChange(int focusChange) {

        if (mPlayer == null) {
            return;
        }

        boolean isPlaying = false;
        try {
            isPlaying = mPlayer.isPlayingOnTheSurface();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                //长时间丢失焦点,当其他应用申请的焦点为AUDIOFOCUS_GAIN时，
                //会触发此回调事件，例如播放QQ音乐，网易云音乐等
                //通常需要暂停音乐播放，若没有暂停播放就会出现和其他音乐同时输出声音

                try {
                    if (isPlaying) {
                        mPausedByTransientLossOfFocus = true;
                        mPlayer.pause(false);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                //短暂性丢失焦点，当其他应用申请AUDIOFOCUS_GAIN_TRANSIENT或AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE时，
                //会触发此回调事件，例如播放短视频，拨打电话等。
                //通常需要暂停音乐播放

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                //短暂性丢失焦点并作降音处理

                try {
                    if (isPlaying) {
                        mPausedByTransientLossOfFocus = true;
                        mPlayer.pause(false);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                //当其他应用申请焦点之后又释放焦点会触发此回调
                //可重新播放音乐
                try {
                    if (!isPlaying && mPausedByTransientLossOfFocus) {
                        mPausedByTransientLossOfFocus = false;
                        mPlayer.resume();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                break;
            default:
                break;
        }
    }


}
