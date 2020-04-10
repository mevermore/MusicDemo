// IMusicService.aidl
package com.smasher.music;

// Declare any non-default types here with import statements
import com.smasher.music.entity.MediaInfo;

interface IMusicService {
            boolean isPlayingOnTheSurface();
            void stop();
            void pause(boolean fromNoti);
            void play();
            void prev();
            void next();
            void resume();
            void becomingNoisy();
            long getDuration();
            long getCurrTime();
            long seek(long pos);
            int getPlayState();
            long getBufferLength();
            long getTotalLength();

            void setPlayMode(int playMode);
            int getPlayMode();

            MediaInfo getCurrent();
            MediaInfo getNextSong();
            MediaInfo getPreSong();

            MediaInfo[] getList();
            void setList(in MediaInfo[] list, in Bundle key);
            void setListByMediaInfo(in MediaInfo[] list, in Bundle key, in MediaInfo mediaInfo);
            int size();


            int getCurPos();
            void playPos(int pos);
            void add(in MediaInfo[] list, int index);
            void erase(int pos);
            void eraseMediaInfo(in MediaInfo mediaInfo);
            int getBufferPercent();

            void exit();
}
