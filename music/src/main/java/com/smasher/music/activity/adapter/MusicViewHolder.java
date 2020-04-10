package com.smasher.music.activity.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.smasher.music.R;
import com.smasher.music.adapter.BaseRecyclerViewHolder;
import com.smasher.music.entity.MediaInfo;

/**
 * @author matao
 * @date 2019/5/24
 */
public class MusicViewHolder extends BaseRecyclerViewHolder<MediaInfo> {

    ImageView mMusicIcon;
    TextView mMusicName;

    public MusicViewHolder(@NonNull View itemView) {
        super(itemView);
        mMusicIcon = itemView.findViewById(R.id.music_icon);
        mMusicName = itemView.findViewById(R.id.music_name);
    }


    @Override
    public void bindView() {
        super.bindView();

        if (mItem != null) {
            mMusicName.setText(mItem.getTitle());
        }
    }
}
