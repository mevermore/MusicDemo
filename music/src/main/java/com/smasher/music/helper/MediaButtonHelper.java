package com.smasher.music.helper;

import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import com.smasher.music.receiver.MediaButtonIntentReceiver;


public class MediaButtonHelper {

    private static String TAG = "MediaButtonHelper";

    private AudioManager mAudioManager;
    private final String PACKAGE_NAME;
    private static boolean mIsRegisterMediaButton = false;

    public MediaButtonHelper(Context ctx) {
        mAudioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        PACKAGE_NAME = ctx.getPackageName();
    }


    public void registerMediaButtonEventReceiver() {
        try {
            Log.d(TAG, "registerMediaButtonEventReceiver");
            ComponentName cn = new ComponentName(PACKAGE_NAME, MediaButtonIntentReceiver.class.getName());
            mAudioManager.registerMediaButtonEventReceiver(cn);
            mIsRegisterMediaButton = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregisterMediaButtonEventReceiver() {
        try {
            Log.d(TAG, "unregisterMediaButtonEventReceiver");
            ComponentName cn = new ComponentName(PACKAGE_NAME, MediaButtonIntentReceiver.class.getName());
            mAudioManager.unregisterMediaButtonEventReceiver(cn);
            mIsRegisterMediaButton = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
