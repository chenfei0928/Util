package io.github.chenfei0928.widget.recyclerview.adapter;

import io.github.chenfei0928.widget.recyclerview.adapter.zhy.WrapperAdapter;

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
