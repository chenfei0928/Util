package com.chenfei.adapter;

import com.chenfei.adapter.zhy.WrapperAdapter;

import androidx.recyclerview.widget.RecyclerView;

public class ZhyAdapterWrapperFactory {

    public static WrapperAdapter wrap(RecyclerView.Adapter<?> adapter) {
        if (adapter instanceof WrapperAdapter) {
            return (WrapperAdapter) adapter;
        } else {
            //noinspection unchecked
            return new WrapperAdapter((RecyclerView.Adapter<RecyclerView.ViewHolder>) adapter);
        }
    }
}
