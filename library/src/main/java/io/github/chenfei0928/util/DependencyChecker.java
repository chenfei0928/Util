package io.github.chenfei0928.util;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.common.util.concurrent.ListenableFuture;

import androidx.concurrent.futures.ListenableFutureKt;

/**
 * 用于处理部分函数中对部分库有额外处理逻辑的判断逻辑
 * <p>
 * 对 {@code try class.toString catch NoClassDefFoundError}
 * 与 {@code try Class.forName catch ClassNotFoundException}
 * 进行性能测试：
 * <ul>
 *     <li>{@link material} 获取{@link BottomSheetDialog}</li>
 *     <li>{@link androidXListenableFuture} 获取{@link ListenableFuture}与{@link ListenableFutureKt}</li>
 *     <li>{@link flexbox} 获取{@link FlexboxLayoutManager}</li>
 * </ul>
 * 的测试结果：
 * <ul>
 *     <li>类存在，方案1耗时2.0-2.5ms</li>
 *     <li>类不存在，方案1耗时16-19ms</li>
 *     <li>类存在，方案2耗时21-27ms</li>
 *     <li>类不存在，方案2耗时7-8ms</li>
 * </ul>
 * <p>
 * 为优化性能：
 * <ul>
 *     <li>对于大概率存在的类，使用方案1进行判断</li>
 *     <li>对于大概率不存在的类，使用方案2进行判断</li>
 * </ul>
 *
 * @author chenf()
 * @date 2024-11-29 14:16
 */
public class DependencyChecker {
    /**
     * 判断Material依赖是否被引入了，调用一下它的方法避免class引用被编译器优化掉
     */
    public static final boolean material = hasMaterialDep();

    private static boolean hasMaterialDep() {
        try {
            // material 库存在大量标准控件，大概率存在，使用方案1加载
            com.google.android.material.bottomsheet.BottomSheetDialog.class.toString();
            return true;
        } catch (NoClassDefFoundError ignore) {
            return false;
        }
    }

    /**
     * Guava依赖可以判断{@link ListenableFuture}是否存在，它的{@code await}方法在另一个deps中，
     * 需要额外判断 {@link ListenableFutureKt} 类是否存在，并调用一下它们的方法避免编译器优化将class优化掉。
     */
    public static final boolean guava = hasGuavaDep();

    private static boolean hasGuavaDep() {
        try {
            // guava listenerFuture 被androidx core依赖，大概率存在，使用方案1加载
            com.google.common.util.concurrent.ListenableFuture.class.toString();
            return true;
        } catch (NoClassDefFoundError ignore) {
            return false;
        }
    }

    /**
     * Guava依赖可以判断{@link ListenableFuture}是否存在，它的{@code await}方法在另一个deps中，
     * 需要额外判断 {@link ListenableFutureKt} 类是否存在，并调用一下它们的方法避免编译器优化将class优化掉。
     */
    public static final boolean androidXListenableFuture = guava && hasAndroidXListenableFutureDep();

    private static boolean hasAndroidXListenableFutureDep() {
        try {
            // androidx concurrentFuturesKtx 没有被当前库其它依赖依赖，大概率不存在，使用方案2加载
            Class.forName("androidx.concurrent.futures.ListenableFutureKt");
            return true;
        } catch (NoClassDefFoundError | ClassNotFoundException ignore) {
            return false;
        }
    }

    /**
     * 判断FlexBox依赖是否被引入了，调用一下它的方法避免class引用被编译器优化掉
     */
    public static final boolean flexbox = hasFlexboxDep();

    private static boolean hasFlexboxDep() {
        try {
            // flexbox库大概率不存在，使用方案2加载
            Class.forName("com.google.android.flexbox.FlexboxLayoutManager");
            return true;
        } catch (ClassNotFoundException ignore) {
            return false;
        }
    }
}
