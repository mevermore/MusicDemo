package com.smasher.music.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;


public class MediaButtonIntentReceiver extends BroadcastReceiver {
    private static final String TAG = MediaButtonIntentReceiver.class
            .getSimpleName();
    private static final int MSG_LONGPRESS_TIMEOUT = 1;
    private static final int LONG_PRESS_DELAY = 1000;

    private static final boolean LONG_PRESS_START_ENABLED = false;

    private static long mLastClickTime = 0;

    private static boolean mDown = false;
    private static boolean mLaunched = false;
    private boolean abortBroadcast = false;


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, intent.getPackage() + "," + intent.getAction() + "," +
                intent.getDataString());
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        String intentAction = intent.getAction();
        String t = Intent.ACTION_HEADSET_PLUG;

        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intentAction)) {
//			if (AudioServiceHelper.sService != null) {
//
//				try {
//					if (AudioServiceHelper.sService.getPlayState() == PlayerState.PLAY_STATE_PLAY) {
//						AudioServiceHelper.sService.becomingnoisy();
//					}
//				} catch (RemoteException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//
//			// Intent i = new Intent(context, QQPlayerService.class);
//			// i.setAction(BroadcastAction.ACTION_SERVICE_BY_CMD);
//			// i.putExtra(BroadcastAction.ACTION_SERVICE_KEY_CMD,
//			// BroadcastAction.ACTION_SERVICE_CMD_PAUSE);
//			// context.startService(i);
        } else if ((event != null) && (event.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK)) {
//			try {
//				if (event.getAction() == KeyEvent.ACTION_UP) {
//					if (AudioServiceHelper.sService != null) {
//						if (AudioServiceHelper.sService.isPlayingOnTheSurface() || AudioServiceHelper.sService.getPlayState() == PlayerState.PLAY_STATE_CONNECTING || AudioServiceHelper.sService.getPlayState() == PlayerState.PLAY_STATE_BUFFERING) {
//							AudioServiceHelper.sService.pause(false);
//						} else {
//							if (AudioServiceHelper.sService.getPlayState() == PlayerState.PLAY_STATE_PAUSE || AudioServiceHelper.sService.getPlayState() == PlayerState.PLAY_STATE_PAUSEING) {
//								AudioServiceHelper.sService.resume();
//							} else {
//								AudioServiceHelper.sService.play();
//							}
//						}
//					}
//				}
//			} catch (RemoteException ignore) {
//
//			}
        }
    }
}
