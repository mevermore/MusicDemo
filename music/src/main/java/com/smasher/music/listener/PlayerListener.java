package com.smasher.music.listener;

public interface PlayerListener {
    // play event     

    int PLAY_EVENT_END = 1;
    int PLAY_EVENT_ERROR = 2;
    int PLAY_EVENT_STATE_CHANGED = 4;
    int PLAY_EVENT_CONN_ERROR = 7;
    int PLAY_EVENT_PLAYLIST_CHANGED = 8;
    int PLAY_EVENT_UPDATE_SONG_PATH = 11;


    int PLAY_EVENT_ERROR_SUB_EVENT_NORMAL = 0;
    int PLAY_EVENT_ERROR_SUB_EVENT_NET_ERROR = 1;
    int PLAY_EVENT_ERROR_SUB_EVENT_PLAYER_INIT_ERROR = 2;

    void notifyEvent(int what, int subWhat, Object ex);
}
