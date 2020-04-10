package com.smasher.music.service;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.smasher.music.IMusicService;
import com.smasher.music.R;
import com.smasher.music.constant.BroadcastAction;
import com.smasher.music.constant.Constant;
import com.smasher.music.constant.PlayerState;
import com.smasher.music.core.MediaPlayerProxy;
import com.smasher.music.core.PlayList;
import com.smasher.music.entity.MediaInfo;
import com.smasher.music.helper.AudioFocusHelper;
import com.smasher.music.helper.MediaButtonHelper;
import com.smasher.music.helper.NotificationHelper;
import com.smasher.music.helper.PlayModeHelper;
import com.smasher.music.helper.TelephonyHelper;
import com.smasher.music.listener.PlayerListener;

/**
 * @author moyu
 */
public class MusicService extends Service implements
        Handler.Callback,
        PlayerListener {


    private static final String TAG = "MusicService";

    private Handler mHandler;
    private MediaInfo mMediaInfo;
    private static final int NOTIFY_ID = 2;

    private AudioFocusHelper mAudioFocusHelper;
    private MediaButtonHelper mMediaButtonHelper;
    private PlayModeHelper mPlayModeHelper;
    private TelephonyHelper mTelephonyHelper;
    private NotificationHelper mNotificationHelper;
    private MediaPlayerProxy mPlayer;

    private int mServiceStartId = -1;
    private int mPlayMode = -1;
    private int mPlayErrorCount = 0;
    private PlayList mPlayList = null;


    private final Object lock = new Object();

    public MusicService() {
        mPlayList = new PlayList();
        mHandler = new Handler(this);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");


        // 声明一个处理器对象
        try {
            initIfNecessary();
            startServiceFront();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Notification getNotify() {

        RemoteViews notifyMusic = new RemoteViews(getPackageName(), R.layout.notify_music);
        // 设置播放图标
        notifyMusic.setImageViewResource(R.id.iv_play, R.drawable.btn_play);
        // 设置文本文字
        notifyMusic.setTextViewText(R.id.tv_play, "暂停播放");
        // 设置已播放的时间
        notifyMusic.setTextViewText(R.id.tv_time, "00:00");
        // 设置远程视图内部的进度条属性
        notifyMusic.setProgressBar(R.id.pb_play, 100, 50, false);


        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.music_play);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constant.CHANNEL_ID);
        builder.setTicker("music")
                .setContentText("服务运行中...")
                .setContentTitle("播放器前台服务")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setLargeIcon(bitmap);

        return builder.build();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServiceStartId = startId;

        Log.d(TAG, "onStartCommand: ");
        try {

            flags = START_STICKY;
            return START_STICKY;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {


        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        }

        unregisterMediaButton();

        unregisterReceiver(mIntentReceiver);

        abandonAudioFocus();

        // 停止前台服务--参数：表示是否移除之前的通知
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }


    //region # control

    public void play() {
        mHandler.postDelayed(() -> {

            if (mMediaInfo == null) {
                mMediaInfo = mPlayList.getCurrent();
            }

            mMediaInfo = mPlayList.getCurrent();


            String mFilePath = mMediaInfo.getUrl();
            if (TextUtils.isEmpty(mFilePath)) {
                return;
            }

            requestAudioFocus();
            boolean initOk = mPlayer.initPlay(this, mMediaInfo, 0);
            if (initOk) {
                mPlayer.play();
            }
        }, 100);

    }


    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }


    public void resume() {
        if (mPlayer != null) {
            mPlayer.resume();
        }
    }


    public void pause() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }


    public void stop() {
        long duration = mPlayer.getDuration();
        mPlayer.stop();

    }

    private void next() {
        mPlayList.moveToNext(false);
        if (mPlayList.currentPosValid()) {
            play();
        } else {
            stop();
        }


    }

    private void previous() {
        mPlayList.moveToPre();
        if (mPlayList.currentPosValid()) {
            play();
        } else {
            stop();
        }
    }


    private void addToList(MediaInfo[] list, int index) {
        mPlayList.addToPlayList(list, index);
    }

    private void playPos(int pos) {
        mPlayList.playPos(pos);

        if (mPlayList.currentPosValid()) {
            if (isPlaying()) {
                fadeoutAndPlay();
            } else {
                play();
            }
        }

    }

    private int getCurPos() {
        synchronized (lock) {
            return mPlayList.getCurrIndex();
        }
    }

    private int getListSize() {
        return mPlayList.getListSize();
    }

    private void setList(MediaInfo[] list, Bundle key, MediaInfo mediaInfo) {
        mPlayList.replaceList(list);

    }

    private MediaInfo[] getList() {
        return mPlayList.getList();
    }

    private MediaInfo getPrevious() {
        return mPlayList.getPre();
    }

    private MediaInfo getNext() {
        return mPlayList.getNext();
    }

    private MediaInfo getCurrent() {
        return mPlayList.getCurrent();
    }

    private int getPlayMode() {
        return mPlayMode;
    }

    private long getTotalLength() {
        return 0;
    }

    private long getBufferLength() {
        return 0;
    }

    private int getPlayState() {
        return mPlayer.getPlayState().getState();
    }

    private PlayerState getPlayStateName() {
        return mPlayer.getPlayState();
    }


    private long seek(long pos) {
        return 0;
    }

    private long getCurrTime() {
        return 0;
    }

    private long duration() {
        return 0;
    }

    private void pauseWithBeComingNoisy() {
    }


    private int getBufferPercent() {
        return 0;
    }


    private void erease(MediaInfo mediaInfo) {

    }


    private void erease(int pos) {

    }

    private void exit() {
        stop();
        stopForeground(true);
        stopSelf(mServiceStartId);

    }
    //endregion

    //region init

    /**
     * init
     */
    private void initIfNecessary() {
        mPlayer = new MediaPlayerProxy(this);
        mPlayList.setOnNotifyChangeListener(this);
        mNotificationHelper = new NotificationHelper();
        mAudioFocusHelper = AudioFocusHelper.getInstance();
        mAudioFocusHelper.init(getApplicationContext(), mBinder);
        mMediaButtonHelper = new MediaButtonHelper(getApplicationContext());
        mPlayModeHelper = new PlayModeHelper(mPlayList);
        mTelephonyHelper = new TelephonyHelper(this, mBinder, mPlayList);
        mTelephonyHelper.setListener();
        registerMediaButton();

        initReceiver();
    }

    private void initReceiver() {

        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(BroadcastAction.ACTION_SERVICE_EXIT);
        commandFilter.addAction(BroadcastAction.ACTION_SERVICE_NEXT);
        commandFilter.addAction(BroadcastAction.ACTION_SERVICE_PREVIOUS);
        commandFilter.addAction(BroadcastAction.ACTION_SERVICE_TOGGLEPAUSE);
        commandFilter.addAction(BroadcastAction.ACTION_SERVICE_PAUSE);
        registerReceiver(mIntentReceiver, commandFilter);

    }


    private void registerMediaButton() {
        if (mMediaButtonHelper != null) {
            mMediaButtonHelper.registerMediaButtonEventReceiver();
        }
    }

    private void unregisterMediaButton() {
        if (mMediaButtonHelper != null) {
            mMediaButtonHelper.unregisterMediaButtonEventReceiver();
        }
    }

    private void requestAudioFocus() {
        if (mAudioFocusHelper != null) {
            mAudioFocusHelper.requestFocus();
        }
    }

    private void abandonAudioFocus() {
        if (mAudioFocusHelper != null) {
            mAudioFocusHelper.abandonFocus();
        }
    }


    /**
     * 广播Intent接收器
     */
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("lins", "action:" + action);
            String cmd = intent.getStringExtra(BroadcastAction.ACTION_SERVICE_KEY_CMD);
            Boolean cmdConsumYet = processCmd(action, cmd);
            if (!cmdConsumYet) {
                if (BroadcastAction.ACTION_SERVICE_EXIT.equalsIgnoreCase(action)) {
                    exit();
                }
            }
        }
    };


    //endregion


    private boolean processCmd(String action, String cmd) {
        if (cmd == null && action == null) {
            return false;
        }
        if (BroadcastAction.ACTION_SERVICE_CMD_NEXT.equals(cmd)
                || BroadcastAction.ACTION_SERVICE_NEXT.equals(action)) {
//            next(false);
            return true;
        } else if (BroadcastAction.ACTION_SERVICE_CMD_PREVIOUS.equals(cmd)
                || BroadcastAction.ACTION_SERVICE_PREVIOUS.equals(action)) {
//            next(true);
            return true;
        } else if (BroadcastAction.ACTION_SERVICE_CMD_TOGGLE_PAUSE.equals(cmd)
                || BroadcastAction.ACTION_SERVICE_TOGGLEPAUSE.equals(action)) {
            if (isPlaying()) {
//                pause(false);
            } else if (getPlayStateName() == PlayerState.PLAY_STATE_PAUSE
                    || getPlayStateName() == PlayerState.PLAY_STATE_PAUSING) {
//                resume();
            } else {
//                play(false);
            }
            return true;
        } else if (BroadcastAction.ACTION_SERVICE_CMD_PAUSE.equals(cmd)
                || BroadcastAction.ACTION_SERVICE_PAUSE.equals(action)) {
//            pauseLogic(false);
            return true;
        } else if (BroadcastAction.ACTION_SERVICE_CMD_STOP.equals(cmd)) {
//            pauseLogic(false);
//            seek(0);
//            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            nm.cancel(NotificationId.NOTIFICATION_PLAYER_ID);
            return true;
        } else if (BroadcastAction.ACTION_SERVICE_EXIT.equalsIgnoreCase(action)) {
            exit();
            return true;
        } else {
            return false;
        }
    }


    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }


    public void setPlayMode(int playMode) {
        if (mPlayMode == playMode) {
            return;
        }

        mPlayMode = playMode;
        mPlayModeHelper.changeMode(playMode);
    }

    @Override
    public void notifyEvent(int what, int subWhat, Object ex) {
        switch (what) {
            case PLAY_EVENT_END:
                endToNext();
                break;
            case PLAY_EVENT_ERROR:
                if (subWhat == PlayerListener.PLAY_EVENT_ERROR_SUB_EVENT_NET_ERROR) {
                    //sendBroadcast(BroadcastAction.ACTION_NET_ERROR);
                } else if (subWhat == PlayerListener.PLAY_EVENT_ERROR_SUB_EVENT_PLAYER_INIT_ERROR) {
                    stop();
                } else {
                    //endToNext();
                }
                break;
            case PLAY_EVENT_PLAYLIST_CHANGED:
                if (getListSize() == 0) {
                    stop();
                }
                break;
            case PLAY_EVENT_STATE_CHANGED:
                //sendBroadcast(BroadcastAction.ACTION_PLAYSTATE_CHANGED);
                break;
            default:
                break;
        }

    }

    //region private

    /**
     * 歌曲播放完以后,跳下一首。对单曲循环与next(false)不一样
     */
    public void endToNext() {
        synchronized (lock) {
            findNextAndPlay();
        }
    }

    private void findNextAndPlay() {
        // int last = mPlayList.getPlayPos();
        mPlayList.moveToNext(true);
        // int now = mPlayList.getPlayPos();
        if (mPlayList.currentPosValid()) {
            // SongInfo song = mPlayList.getCurrent();
            play();
        } else {
            stop();
        }
    }


    //endregion


    public void startServiceFront() {
        mHandler.postDelayed(() -> startForeground(NOTIFY_ID, getNotify()), 200);
    }


    private MusicService getService() {
        return this;
    }


    private IMusicService.Stub mBinder = new IMusicService.Stub() {
        @Override
        public boolean isPlayingOnTheSurface() throws RemoteException {
            return getService().isPlaying();
        }

        @Override
        public void stop() throws RemoteException {
            getService().stop();

        }

        @Override
        public void pause(boolean fromNoti) throws RemoteException {
            getService().pause();
        }

        @Override
        public void play() throws RemoteException {
            getService().play();
        }

        @Override
        public void prev() throws RemoteException {
            getService().previous();

        }

        @Override
        public void next() throws RemoteException {
            getService().next();
        }

        @Override
        public void resume() throws RemoteException {
            getService().resume();
        }

        @Override
        public void becomingNoisy() throws RemoteException {
            getService().pauseWithBeComingNoisy();
        }

        @Override
        public long getDuration() throws RemoteException {
            return getService().duration();
        }

        @Override
        public long getCurrTime() throws RemoteException {
            return getService().getCurrTime();
        }

        @Override
        public long seek(long pos) throws RemoteException {
            return getService().seek(pos);
        }

        @Override
        public int getPlayState() throws RemoteException {
            return getService().getPlayState();
        }

        @Override
        public long getBufferLength() throws RemoteException {
            return getService().getBufferLength();
        }

        @Override
        public long getTotalLength() throws RemoteException {
            return getService().getTotalLength();
        }

        @Override
        public void setPlayMode(int playMode) throws RemoteException {
            getService().setPlayMode(playMode);
        }

        @Override
        public int getPlayMode() throws RemoteException {
            return getService().getPlayMode();
        }

        @Override
        public MediaInfo getCurrent() throws RemoteException {
            return getService().getCurrent();
        }

        @Override
        public MediaInfo getNextSong() throws RemoteException {
            return getService().getNext();
        }

        @Override
        public MediaInfo getPreSong() throws RemoteException {
            return getService().getPrevious();
        }

        @Override
        public MediaInfo[] getList() throws RemoteException {
            return getService().getList();
        }

        @Override
        public void setList(MediaInfo[] list, Bundle key) throws RemoteException {
            getService().setList(list, key, null);
        }

        @Override
        public void setListByMediaInfo(MediaInfo[] list, Bundle key, MediaInfo mediaInfo) throws RemoteException {
            getService().setList(list, key, mediaInfo);
        }

        @Override
        public int size() throws RemoteException {
            return getService().getListSize();
        }

        @Override
        public int getCurPos() throws RemoteException {
            return getService().getCurPos();
        }

        @Override
        public void playPos(int pos) throws RemoteException {
            getService().playPos(pos);
        }

        @Override
        public void add(MediaInfo[] list, int index) throws RemoteException {
            getService().addToList(list, index);
        }

        @Override
        public void erase(int pos) throws RemoteException {
            getService().erease(pos);
        }

        @Override
        public void eraseMediaInfo(MediaInfo mediaInfo) throws RemoteException {
            getService().erease(mediaInfo);
        }

        @Override
        public int getBufferPercent() throws RemoteException {
            return getService().getBufferPercent();
        }

        @Override
        public void exit() throws RemoteException {
            getService().exit();
        }
    };


    // ////////////////////淡入淡出功能////////////////////////

    private static final int FADING_NOTHING = 0;
    private static final int FADING_PAUSING = 1;
    private static final int FADING_RESUMING = 2;
    private static final int FADE_EVENT_FADEIN = 11;
    private static final int FADE_EVENT_FADEOUT = 12;
    private int fadState = FADING_NOTHING;
    private float mCurrentVolume = 1.0f;

    private boolean isFading() {
        return fadState != FADING_NOTHING;
    }

    private void startFadeIn() {
        switch (fadState) {
            case FADING_NOTHING:
                mCurrentVolume = 0.0f;
                mPlayer.setVolume(mCurrentVolume);
                fadState = FADING_RESUMING;
                mFadeHandler.sendEmptyMessage(FADE_EVENT_FADEIN);
                break;
            case FADING_PAUSING:
                fadState = FADING_RESUMING;
                mFadeHandler.sendEmptyMessage(FADE_EVENT_FADEIN);
                break;
            case FADING_RESUMING:
                break;
            default:
                break;
        }
    }

    private void startFadeOut(int action) {
        switch (fadState) {
            case FADING_NOTHING:
                mCurrentVolume = 1.0f;
                mPlayer.setVolume(mCurrentVolume);
                fadState = FADING_PAUSING;
                Message msg = mFadeHandler.obtainMessage(FADE_EVENT_FADEOUT);
                msg.arg1 = action;
                mFadeHandler.sendMessage(msg);
                break;
            case FADING_PAUSING:

                break;
            case FADING_RESUMING:
                fadState = FADING_PAUSING;
                Message msg2 = mFadeHandler.obtainMessage(FADE_EVENT_FADEOUT);
                msg2.arg1 = action;
                mFadeHandler.sendMessage(msg2);
                break;
            default:
                break;
        }
    }

    /**
     * handler播放器消息
     */
    private Handler mFadeHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            synchronized (lock) {
                switch (msg.what) {
                    case FADE_EVENT_FADEIN:
                        if (fadState == FADING_RESUMING) {
                            if (mCurrentVolume < 0.0f) {
                                mCurrentVolume = 0.0f;
                            }
                            if (mCurrentVolume < 0.85f) {
                                mCurrentVolume += 0.15f;
                                mPlayer.setVolume(mCurrentVolume);
                                mFadeHandler.sendEmptyMessageDelayed(
                                        FADE_EVENT_FADEIN, 100);
                            } else {
                                if (fadState == FADING_RESUMING) {
                                    mCurrentVolume = 1.0f;
                                    mPlayer.setVolume(mCurrentVolume);
                                    fadState = FADING_NOTHING;
                                }
                            }
                        }
                        break;
                    case FADE_EVENT_FADEOUT:
                        if (fadState == FADING_PAUSING) {
                            if (mCurrentVolume > 1.0f) {
                                mCurrentVolume = 1.0f;
                            }
                            if (mCurrentVolume > 0.15f) {
                                mCurrentVolume -= 0.15f;
                                mPlayer.setVolume(mCurrentVolume);
                                Message newMsg = Message.obtain();
                                newMsg.what = msg.what;
                                newMsg.arg1 = msg.arg1;
                                mFadeHandler.sendMessageDelayed(newMsg, 100);
                            } else {
                                if (fadState == FADING_PAUSING) {
                                    mCurrentVolume = 1.0f;
                                    if (msg.arg1 == 0) {
                                        // pause
                                        pause();
                                        Log.d(TAG, "pause " + 0);
                                    } else if (msg.arg1 == -1) {
                                        // pre
                                        previous();
                                        Log.d(TAG, "previous " + 0);
                                    } else if (msg.arg1 == 1) {
                                        // next
                                        next();
                                        Log.d(TAG, "next " + 0);
                                    } else if (msg.arg1 == 2) {
                                        // play
//                                        playLogic(2);
                                        play();
                                        Log.d(TAG, "play " + 0);
                                    }
                                    fadState = FADING_NOTHING;
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
            return false;
        }
    });


    private void prevAndFadeOut() {
        startFadeOut(-1);
    }

    private void nextAndFadeOut() {
        startFadeOut(1);
    }

    private void fadeoutAndPlay() {
        startFadeOut(2);
    }

    private void resumeAndFadeIn() {
        startFadeIn();
    }

    private void pauseAndFadeOut(boolean fromNotification) {
        mPlayer.pausing();
        startFadeOut(0);
    }

}
