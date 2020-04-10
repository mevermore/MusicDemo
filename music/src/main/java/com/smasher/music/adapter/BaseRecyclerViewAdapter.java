package com.smasher.music.adapter;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author moyu
 */
public abstract class BaseRecyclerViewAdapter<T, VH extends BaseRecyclerViewHolder<T>> extends RecyclerView.Adapter<VH> {
    public static final String TAG = "Adapter";
    protected Context mContext;
    protected List<T> mList;
    protected OnItemClickListener mOnItemClickListener;


    public BaseRecyclerViewAdapter(Context context) {
        this.mContext = context;
    }


    public void setData(List<T> list) {
        if (list != null) {
            mList = list;
        } else {
            mList = new ArrayList<>();
        }
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }


    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        return onCreateDefineViewHolder(viewGroup, type);
    }


    /**
     * 创建ViewHolder
     *
     * @param viewGroup viewGroup
     * @param type      type
     * @return VH
     */
    public abstract VH onCreateDefineViewHolder(@NonNull ViewGroup viewGroup, int type);


    @Override
    public void onBindViewHolder(@NonNull VH viewHolder, int position) {
        if (mList.isEmpty() || position < 0 || position >= mList.size()) {
            return;
        }
        T item = mList.get(position);
        if (item == null) {
            Log.e(TAG, "data item is null");
            return;
        }
        viewHolder.setOnItemClickListener(mOnItemClickListener);
        viewHolder.setIndex(position);
        viewHolder.setItem(item);
        viewHolder.bindView();
    }

    @Override
    public int getItemCount() {
        if (mList == null) {
            Log.e(TAG, "you have not bind data yet!");
            return 0;
        }
        return mList.size();
    }
}
