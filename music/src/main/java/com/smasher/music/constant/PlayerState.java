package com.smasher.music.constant;

/**
 * @author matao
 * @date 2019/5/25
 */
public enum PlayerState {


    /**
     * 状态
     */
    PLAY_STATE_PLAY(0), PLAY_STATE_PAUSE(1), PLAY_STATE_STOP(2), PLAY_STATE_CLOSE(3),
    PLAY_STATE_CONNECTING(4), PLAY_STATE_BUFFERING(5), PLAY_STATE_PAUSING(6);

    private int mState;

    PlayerState(int state) {
        mState = state;
    }

    public int getState() {
        return mState;
    }


}
