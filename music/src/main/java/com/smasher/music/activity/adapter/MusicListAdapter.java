package com.smasher.music.activity.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.smasher.music.R;
import com.smasher.music.adapter.BaseRecyclerViewAdapter;
import com.smasher.music.entity.MediaInfo;

/**
 * @author matao
 * @date 2019/5/24
 */
public class MusicListAdapter extends BaseRecyclerViewAdapter<MediaInfo, MusicViewHolder> {

    public MusicListAdapter(Context context) {
        super(context);
    }

    @Override
    public MusicViewHolder onCreateDefineViewHolder(@NonNull ViewGroup viewGroup, int type) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_music, viewGroup, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }
}
