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
 *     <li>{@link DependencyChecker#MATERIAL} 获取{@link BottomSheetDialog}</li>
 *     <li>{@link DependencyChecker#GUAVA_LISTENABLE_FUTURE} 获取{@link ListenableFuture}</li>
 *     <li>{@link DependencyChecker#ANDROID_X_LISTENABLE_FUTURE} 获取{@link ListenableFutureKt}</li>
 *     <li>{@link DependencyChecker#FLEXBOX} 获取{@link FlexboxLayoutManager}</li>
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
public enum DependencyChecker {
    /**
     * 判断Material依赖是否被引入了，调用一下它的方法避免class引用被编译器优化掉
     */
    MATERIAL {
        @Override
        protected boolean initValue() {
            try {
                // material 库存在大量标准控件，大概率存在，使用方案1加载
                com.google.android.material.bottomsheet.BottomSheetDialog.class.toString();
                return true;
            } catch (NoClassDefFoundError ignore) {
                return false;
            }
        }
    },
    /**
     * Guava依赖可以判断{@link ListenableFuture}是否存在，它的{@code await}方法在另一个deps中，
     * 需要额外判断 {@link ListenableFutureKt} 类是否存在，并调用一下它们的方法避免编译器优化将class优化掉。
     */
    GUAVA_LISTENABLE_FUTURE {
        @Override
        protected boolean initValue() {
            try {
                // guava listenerFuture 被androidx core依赖，大概率存在，使用方案1加载
                com.google.common.util.concurrent.ListenableFuture.class.toString();
                return true;
            } catch (NoClassDefFoundError ignore) {
                return false;
            }
        }
    },
    /**
     * Guava依赖可以判断{@link ListenableFuture}是否存在，它的{@code await}方法在另一个deps中，
     * 需要额外判断 {@link ListenableFutureKt} 类是否存在，并调用一下它们的方法避免编译器优化将class优化掉。
     */
    ANDROID_X_LISTENABLE_FUTURE {
        @Override
        protected boolean initValue() {
            try {
                // androidx concurrentFuturesKtx 没有被当前库其它依赖依赖，大概率不存在，使用方案2加载
                Class.forName("androidx.concurrent.futures.ListenableFutureKt");
                return true;
            } catch (NoClassDefFoundError | ClassNotFoundException ignore) {
                return false;
            }
        }
    },
    /**
     * 判断FlexBox依赖是否被引入了，调用一下它的方法避免class引用被编译器优化掉
     */
    FLEXBOX {
        @Override
        protected boolean initValue() {
            try {
                // flexbox库大概率不存在，使用方案2加载
                Class.forName("com.google.android.flexbox.FlexboxLayoutManager");
                return true;
            } catch (ClassNotFoundException ignore) {
                return false;
            }
        }
    }, PROTOBUF {
        @Override
        protected boolean initValue() {
            try {
                // protobuf库大概率不存在，使用方案2加载
                Class.forName("com.google.protobuf.Message");
                return true;
            } catch (ClassNotFoundException ignore) {
                return false;
            }
        }
    },
    PROTOBUF_LITE {
        @Override
        protected boolean initValue() {
            if (PROTOBUF.invoke()) {
                // 如果完整库有被引入，lite就一定包含在内
                return true;
            }
            try {
                // protobufLite库大概率不存在，使用方案2加载
                // lite库会keep这个类 https://github.com/protocolbuffers/protobuf/blob/main/java/lite/proguard.pgcfg
                Class.forName("com.google.protobuf.GeneratedMessageLite");
                return true;
            } catch (ClassNotFoundException ignore) {
                return false;
            }
        }
    },
    GSON {
        @Override
        protected boolean initValue() {
            try {
                // gson 轻量级常用，大概率会存在，使用方案1加载
                com.google.gson.reflect.TypeToken.class.toString();
                return true;
            } catch (NoClassDefFoundError ignore) {
                return false;
            }
        }
    },
    GUAVA {
        @Override
        protected boolean initValue() {
            try {
                // guava库大概率不存在，使用方案2加载
                Class.forName("com.google.common.reflect.TypeToken");
                return true;
            } catch (ClassNotFoundException ignore) {
                return false;
            }
        }
    };

    private Boolean value = null;

    protected abstract boolean initValue();

    public boolean invoke() {
        if (value == null) {
            value = initValue();
        }
        return value;
    }
}
