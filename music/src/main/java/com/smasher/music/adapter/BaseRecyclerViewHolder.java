package com.smasher.music.adapter;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



/**
 * @author matao
 * @date 2019/4/4
 */
public class BaseRecyclerViewHolder<T> extends RecyclerView.ViewHolder {


    protected T mItem;
    protected int mIndex;
    protected Context mContext;
    protected OnItemClickListener mOnItemClickListener;

    public BaseRecyclerViewHolder(@NonNull View itemView) {
        super(itemView);
        mContext = itemView.getContext();
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }


    public void setIndex(int mIndex) {
        this.mIndex = mIndex;
    }


    public void setItem(T mItem) {
        this.mItem = mItem;
    }


    public void bindView() {
        itemView.setOnClickListener(itemListener);
    }


    public String getString(int res) {
        return mContext.getString(res);
    }



    private View.OnClickListener itemListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onClick(v, mIndex);
            }
        }
    };

}
