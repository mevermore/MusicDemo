package com.smasher.music.helper;

import android.content.Context;
import android.media.AudioManager;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.smasher.music.IMusicService;
import com.smasher.music.core.PlayList;

/**
 * @author matao
 * @date 2019/5/30
 */
public class TelephonyHelper extends PhoneStateListener {

    /**
     * 手机状态监听器：来电话、来短信事件
     */
    private boolean mResumeAfterCall = false;

    private Context mContext;

    private TelephonyManager mTelephonyManager;
    private AudioManager mAudioManager;
    private IMusicService mPlayer;
    private PlayList mPlayList;

    public TelephonyHelper(Context context, IMusicService iMusicService, PlayList playList) {
        mContext = context;
        mPlayer = iMusicService;
        mPlayList = playList;
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }


    public void setListener() {
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(this, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }


    @Override
    public void onCallStateChanged(int state, String phoneNumber) {
        boolean isPlaying = false;
        boolean valid = false;
        try {
            isPlaying = mPlayer.isPlayingOnTheSurface();
            valid = mPlayList.currentPosValid();
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                // 响铃，来电话了
                // 先判断是否需要响铃，如果需要，则pause，否则不需要pause.
                int ringVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
                if (ringVolume > 0) {
                    mResumeAfterCall = (isPlaying || mResumeAfterCall) && (valid);
//                    pauseLogic(false);
                }
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                // 电话活跃中
                mResumeAfterCall = (isPlaying || mResumeAfterCall) && (valid);
//                pauseLogic(false);
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                // 挂断了
                if (mResumeAfterCall) {
//                    resumeLogic();
                    mResumeAfterCall = false;
                }
                break;
            default:
                break;
        }
    }
}
