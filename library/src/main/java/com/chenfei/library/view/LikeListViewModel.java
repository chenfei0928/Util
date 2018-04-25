package com.chenfei.library.view;

import android.support.annotation.LayoutRes;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Iterator;

import androidx.core.view.ViewGroupKt;

/**
 * 为ViewGroup填充子View帮助类
 * Created by MrFeng on 2017/3/1.
 */
public class LikeListViewModel {

    public static <Bean, VG extends ViewGroup> void inject(
            @NonNull VG viewGroup, @Nullable Iterable<Bean> beanIterable, @NonNull Adapter<VG, Bean> adapter) {
        injectImpl(viewGroup, beanIterable, adapter);
    }

    public static <Bean, VG extends ViewGroup> void inject(
            @NonNull VG viewGroup, @Nullable Iterable<Bean> beanIterable, @NonNull AsyncAdapter<VG, Bean> adapter) {
        injectImpl(viewGroup, beanIterable, adapter);
    }

    public static <Bean, VG extends ViewGroup> void inject(
            @NonNull VG viewGroup, @Nullable Iterable<Bean> beanIterable, @NonNull AsyncAdapter2<VG, Bean> adapter) {
        injectImpl(viewGroup, beanIterable, adapter);
    }

    /**
     * 将bean列表设置给容器ViewGroup里
     * 为了让{@link BasicAdapter} 不对外暴露，对调用进行转发
     *
     * @param viewGroup    注入的目标ViewGroup
     * @param beanIterable 等待注入的实例列表
     * @param adapter      适配器，用于创建布局和绑定布局
     * @param <Bean>       实例类型
     * @param <VG>         目标ViewGroup类型
     */
    private static <Bean, VG extends ViewGroup> void injectImpl(
            @NonNull VG viewGroup, @Nullable Iterable<Bean> beanIterable, @NonNull BasicAdapter<VG, Bean> adapter) {
        if (beanIterable == null) {
            viewGroup.setVisibility(View.GONE);
            return;
        }
        // 对适配的数据集判空
        Iterator<Bean> beanIterator = beanIterable.iterator();
        if (!beanIterator.hasNext()) {
            viewGroup.setVisibility(View.GONE);
            return;
        }
        if (viewGroup.getChildCount() > 0) {
            // 对view和bean列表进行迭代
            Iterator<View> viewIterator = ViewGroupKt.iterator(viewGroup);
            while (viewIterator.hasNext()) {
                View next = viewIterator.next();
                // 判断当前迭代到的view
                if (!adapter.isView(next)) {
                    // 如果不是目标itemView
                    viewGroup.removeView(next);
                    viewIterator.remove();
                } else if (beanIterator.hasNext()) {
                    // 如果是目标itemView，并且有一个要适配到的bean
                    next.setVisibility(View.VISIBLE);
                    adapter.onBindView(next, beanIterator.next());
                } else {
                    // 如果没有要适配到的bean，隐藏当前行
                    next.setVisibility(View.GONE);
                }
            }
        }
        // 如果还有bean需要显示，实例化View，适配数据，并将Binding缓存
        if (beanIterator.hasNext()) {
            if (adapter instanceof Adapter) {
                // 阻塞等待创建View
                inflater(viewGroup, beanIterator, (Adapter<VG, Bean>) adapter);
            } else if (adapter instanceof AsyncAdapter) {
                // 使用布局ID
                inflater(viewGroup, beanIterator, (AsyncAdapter<VG, Bean>) adapter);
            } else if (adapter instanceof AsyncAdapter2) {
                // 不使用布局ID
                inflater(viewGroup, beanIterator, (AsyncAdapter2<VG, Bean>) adapter);
            }
        }
    }

    /**
     * 阻塞的加载布局并注入
     *
     * @param viewGroup    注入的目标ViewGroup
     * @param beanIterator 等待注入的实例迭代器
     * @param adapter      适配器，用于在主线程创建布局和绑定布局
     * @param <Bean>       实例类型
     * @param <VG>         目标ViewGroup类型
     */
    private static <Bean, VG extends ViewGroup> void inflater(
            @NonNull VG viewGroup, @NonNull Iterator<Bean> beanIterator, Adapter<VG, Bean> adapter) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        while (beanIterator.hasNext()) {
            View view = adapter.onCreateView(inflater, viewGroup);
            viewGroup.addView(view);
            adapter.onBindView(view, beanIterator.next());
        }
    }

    /**
     * 异步的的加载布局并注入（使用布局ID）
     *
     * @param viewGroup    注入的目标ViewGroup
     * @param beanIterator 等待注入的实例迭代器
     * @param adapter      适配器，用于在次线程创建视图和在主线程初始化视图、绑定视图
     * @param <Bean>       实例类型
     * @param <VG>         目标ViewGroup类型
     */
    private static <Bean, VG extends ViewGroup> void inflater(
            @NonNull VG viewGroup, @NonNull Iterator<Bean> beanIterator, AsyncAdapter<VG, Bean> adapter) {
        AsyncLayoutInflater asyncLayoutInflater = new AsyncLayoutInflater(viewGroup.getContext());
        while (beanIterator.hasNext()) {
            Bean bean = beanIterator.next();
            asyncLayoutInflater.inflate(adapter.getLayoutId(), viewGroup, view -> {
                adapter.onViewCreated(view);
                viewGroup.addView(view);
                adapter.onBindView(view, bean);
            });
        }
    }

    /**
     * 异步的加载布局并注入（不使用布局ID）
     *
     * @param viewGroup    注入的目标ViewGroup
     * @param beanIterator 等待注入的实例迭代器
     * @param adapter      适配器，用于在次线程创建视图和在主线程初始化视图、绑定视图
     * @param <Bean>       实例类型
     * @param <VG>         目标ViewGroup类型
     */
    private static <Bean, VG extends ViewGroup> void inflater(
            @NonNull VG viewGroup, @NonNull Iterator<Bean> beanIterator, AsyncAdapter2<VG, Bean> adapter) {
        AsyncLayoutInflater asyncLayoutInflater = new AsyncLayoutInflater(viewGroup.getContext());
        while (beanIterator.hasNext()) {
            Bean bean = beanIterator.next();
            asyncLayoutInflater.inflate(adapter::onCreateView, viewGroup, view -> {
                adapter.onViewCreated(view);
                viewGroup.addView(view);
                adapter.onBindView(view, bean);
            });
        }
    }

    /**
     * 基础功能的布局适配载入工具，提供判断是否为目标View和对View进行数据绑定接口
     *
     * @param <VG>   目标ViewGroup容器
     * @param <Bean> 实例类型
     */
    @SuppressWarnings("unused")
    private interface BasicAdapter<VG extends ViewGroup, Bean> {
        @MainThread
        boolean isView(@NonNull View view);

        @MainThread
        void onBindView(@NonNull View view, @NonNull Bean bean);
    }

    /**
     * 用于阻塞的注入的布局适配载入工具，提供在主线程中进行布局的创建
     *
     * @param <VG>   目标ViewGroup容器
     * @param <Bean> 实例类型
     */
    public interface Adapter<VG extends ViewGroup, Bean> extends BasicAdapter<VG, Bean> {
        @NonNull
        @MainThread
        View onCreateView(@NonNull LayoutInflater inflater, @NonNull VG parent);
    }

    /**
     * 用于异步注入的布局适配载入工具，提供在View创建完成后在主线程中进行初始化
     *
     * @param <VG>   目标ViewGroup容器
     * @param <Bean> 实例类型
     */
    private interface BasicAsyncAdapter<VG extends ViewGroup, Bean> extends BasicAdapter<VG, Bean> {
        @MainThread
        void onViewCreated(@NonNull View view);
    }

    /**
     * 用于阻塞的注入的布局适配载入工具，提供在次线程中进行布局的创建（使用布局文件）
     *
     * @param <VG>   目标ViewGroup容器
     * @param <Bean> 实例类型
     */
    public interface AsyncAdapter<VG extends ViewGroup, Bean> extends BasicAsyncAdapter<VG, Bean> {
        @LayoutRes
        @MainThread
        int getLayoutId();
    }

    /**
     * 用于阻塞的注入的布局适配载入工具，提供在次线程中进行布局的创建（不使用布局文件）
     *
     * @param <VG>   目标ViewGroup容器
     * @param <Bean> 实例类型
     */
    public interface AsyncAdapter2<VG extends ViewGroup, Bean> extends BasicAsyncAdapter<VG, Bean> {
        @NonNull
        @WorkerThread
        View onCreateView(@NonNull LayoutInflater inflater, @NonNull VG parent);
    }
}
