package io.github.chenfei0928.widget.recyclerview.adapter;

import androidx.recyclerview.widget.RecyclerView;
import io.github.chenfei0928.widget.recyclerview.adapter.zhy.WrapperAdapter;

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
