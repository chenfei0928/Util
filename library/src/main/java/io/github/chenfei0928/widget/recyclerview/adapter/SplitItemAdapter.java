package io.github.chenfei0928.widget.recyclerview.adapter;

import android.util.SparseIntArray;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 分割复杂Item为多个Type的适配器
 * Created by MrFeng on 2017/2/8.
 */
public abstract class SplitItemAdapter<Bean, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    protected final List<Bean> mList;
    /**
     * 实际显示position到数据列表下标的映射、实际显示position到数据分割显示的下标映射保存
     */
    private final SparseIntArray mPositionMap = new SparseIntArray();
    private int mCount;

    protected SplitItemAdapter(List<Bean> beanList) {
        mList = beanList;
        init();
        registerAdapterDataObserver(new AdapterDataObserver<>(this));
    }

    /**
     * 获取某个Bean的实例分割为多少个item显示
     *
     * @param bean 某个实例
     * @return 分割为多少个item
     */
    protected abstract int getBeanSplitItemCount(Bean bean);

    /**
     * 获取某个Bean实例分割显示到的某个item的ViewType
     *
     * @param bean            某个实例
     * @param visiblePosition 分割显示出的下标
     * @return ViewType
     */
    protected abstract int getBeanSplitItemViewType(Bean bean, int visiblePosition);

    public void init() {
        mPositionMap.clear();
        int count = 0;
        for (int dataPosition = 0; dataPosition < mList.size(); dataPosition++) {
            int startPosition = count;
            count += getBeanSplitItemCount(mList.get(dataPosition));
            // 将实际显示下标和数据下标映射保存
            for (int visiblePosition = startPosition, visibleByDataPosition = 0;
                 visiblePosition < count; visiblePosition++, visibleByDataPosition++) {
                mPositionMap.put(visiblePosition, dataPosition);
                mPositionMap.put(visiblePosition | 0x80000000, visibleByDataPosition);
            }
        }
        mCount = count;
    }

    @Override
    public int getItemCount() {
        return mCount;
    }

    @Override
    public final int getItemViewType(int position) {
        int dataPosition = mPositionMap.get(position);
        int visibleByDataPosition = mPositionMap.get(position | 0x80000000);
        return getBeanSplitItemViewType(mList.get(dataPosition), visibleByDataPosition);
    }

    @Override
    public final void onBindViewHolder(@NonNull VH holder, int position) {
        int dataPosition = mPositionMap.get(position);
        int visibleByDataPosition = mPositionMap.get(position | 0x80000000);
        onBindViewHolderImpl(holder, mList.get(dataPosition), visibleByDataPosition);
    }

    protected abstract void onBindViewHolderImpl(VH holder, Bean bean, int visibleByDataPosition);

    private static class AdapterDataObserver<T> extends RecyclerView.AdapterDataObserver {
        private final SplitItemAdapter<T, ?> mAdapter;

        AdapterDataObserver(SplitItemAdapter<T, ?> adapter) {
            mAdapter = adapter;
        }

        @Override
        public void onChanged() {
            mAdapter.init();
        }
    }
}
