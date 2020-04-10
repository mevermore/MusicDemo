package com.smasher.music.helper;

import com.smasher.music.core.PlayList;

/**
 * @author matao
 * @date 2019/5/30
 */
public class PlayModeHelper {
    PlayList mPlayList;

    public PlayModeHelper(PlayList playList) {
        mPlayList = playList;
    }


   public void changeMode(int playMode){
        switch (playMode) {
            case PlayList.PLAY_MODE_ONESHOT:
                mPlayList.setOneShotMode(true);
                mPlayList.setRepeatMode(false);
                mPlayList.setShuffleMode(false);
                break;
            case PlayList.PLAY_MODE_ONESHOT_REPEAT:
                mPlayList.setOneShotMode(true);
                mPlayList.setRepeatMode(true);
                mPlayList.setShuffleMode(false);
                break;
            case PlayList.PLAY_MODE_LIST:
                mPlayList.setOneShotMode(false);
                mPlayList.setRepeatMode(false);
                mPlayList.setShuffleMode(false);
                break;
            case PlayList.PLAY_MODE_LIST_REPEAT:
                mPlayList.setOneShotMode(false);
                mPlayList.setRepeatMode(true);
                mPlayList.setShuffleMode(false);
                break;
            case PlayList.PLAY_MODE_LIST_SHUFFLE:
                mPlayList.setOneShotMode(false);
                mPlayList.setRepeatMode(false);
                mPlayList.setShuffleMode(true);
                break;
            case PlayList.PLAY_MODE_LIST_SHUFFLE_REPEAT:
                mPlayList.setOneShotMode(false);
                mPlayList.setRepeatMode(true);
                mPlayList.setShuffleMode(true);
                break;
            default:
                break;
        }
    }

}
