package io.github.chenfei0928.util

import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.reflect.TypeToken
import io.github.chenfei0928.base.UtilInitializer

/**
 * 用于返回各个依赖库是否已经添加依赖，从而进行进一步优化处理流程
 */
interface DependencyChecker {
    // Google Material Design 控件库 com.google.android.material:material
    val material: Boolean

    // com.google.guava:listenablefuture
    val guavaListenableFuture: Boolean

    // Google Guava 工具库 https://github.com/google/guava
    val guava: Boolean

    // androidx.concurrent:concurrent-futures-ktx
    val androidXListenableFuture: Boolean

    // https://github.com/google/flexbox-layout
    val flexBox: Boolean

    // https://github.com/protocolbuffers/protobuf
    val protobufFull: Boolean

    // https://github.com/protocolbuffers/protobuf
    val protobufLite: Boolean

    // https://github.com/google/gson
    val gson: Boolean

    //  https://github.com/Tencent/MMKV
    val mmkv: Boolean

    companion object : DependencyChecker {
        override val material: Boolean get() = UtilInitializer.sdkDependency.material
        override val guavaListenableFuture: Boolean get() = UtilInitializer.sdkDependency.guavaListenableFuture
        override val guava: Boolean get() = UtilInitializer.sdkDependency.guava
        override val androidXListenableFuture: Boolean get() = UtilInitializer.sdkDependency.androidXListenableFuture
        override val flexBox: Boolean get() = UtilInitializer.sdkDependency.flexBox
        override val protobufFull: Boolean get() = UtilInitializer.sdkDependency.protobufFull
        override val protobufLite: Boolean get() = UtilInitializer.sdkDependency.protobufLite
        override val gson: Boolean get() = UtilInitializer.sdkDependency.gson
        override val mmkv: Boolean get() = UtilInitializer.sdkDependency.mmkv
    }

    /**
     * 由用户传入各个依赖项是否已添加
     */
    data class UserInput(
        override val material: Boolean,
        override val guavaListenableFuture: Boolean,
        override val guava: Boolean,
        override val androidXListenableFuture: Boolean,
        override val flexBox: Boolean,
        override val protobufFull: Boolean,
        override val protobufLite: Boolean,
        override val gson: Boolean,
        override val mmkv: Boolean,
    ) : DependencyChecker

    /**
     * 用于处理部分函数中对部分库有额外处理逻辑的判断逻辑
     *
     * 对 `try class.toString catch NoClassDefFoundError`
     * 与 `try Class.forName catch ClassNotFoundException`
     * 进行性能测试：
     *
     *  * [MATERIAL] 获取 [BottomSheetDialog]
     *  * [GUAVA_LISTENABLE_FUTURE] 获取 [ListenableFuture]
     *  * [ANDROID_X_LISTENABLE_FUTURE] 获取 `ListenableFutureKt`
     *  * [FLEXBOX] 获取 `FlexboxLayoutManager`
     *
     * 的测试结果：
     *
     *  * 类存在，方案1耗时2.0-2.5ms
     *  * 类不存在，方案1耗时16-19ms
     *  * 类存在，方案2耗时21-27ms
     *  * 类不存在，方案2耗时7-8ms
     *
     * 为优化性能：
     *
     *  * 对于大概率存在的类，使用方案1进行判断
     *  * 对于大概率不存在的类，使用方案2进行判断
     *
     * @author chenf()
     * @date 2024-11-29 14:16
     */
    enum class ByReflectLazy : Lazy<Boolean> {
        //<editor-fold desc="使用反射或直接引入类饼try-catch来判断对应的类是否已经依赖" defaultstatus="collapsed">
        /**
         * 判断Material依赖是否被引入了，调用一下它的方法避免class引用被编译器优化掉
         */
        MATERIAL {
            override fun initValue(): Boolean = try {
                // material 库存在大量标准控件，大概率存在，使用方案1加载
                BottomSheetDialog::class.java.toString()
                true
            } catch (_: NoClassDefFoundError) {
                false
            }
        },

        /**
         * Guava Listenable 依赖可以判断 [ListenableFuture] 类是否存在，它的`await`方法在另一个deps中，
         * 需要额外判断 `ListenableFutureKt` 类是否存在，并调用一下它们的方法避免编译器优化将class优化掉。
         *
         * GuavaListenable 与 Guava 库不是同一个库
         */
        GUAVA_LISTENABLE_FUTURE {
            override fun initValue(): Boolean = try {
                // guava listenerFuture 被androidx core依赖，大概率存在，使用方案1加载
                ListenableFuture::class.java.toString()
                true
            } catch (_: NoClassDefFoundError) {
                false
            }
        },

        GUAVA {
            override fun initValue(): Boolean = try {
                // guava库大概率不存在，使用方案2加载
                Class.forName("com.google.common.reflect.TypeToken")
                true
            } catch (_: ClassNotFoundException) {
                false
            }
        },

        /**
         * Guava依赖可以判断 [ListenableFuture] 是否存在，它的 [androidx.concurrent.futures.await] 方法在另一个deps中，
         * 需要额外判断 `ListenableFutureKt` 类是否存在，并调用一下它们的方法避免编译器优化将class优化掉。
         *
         * GuavaListenable 与 Guava 库不是同一个库
         */
        ANDROID_X_LISTENABLE_FUTURE {
            override fun initValue(): Boolean = if (GUAVA_LISTENABLE_FUTURE._value == false) {
                false
            } else try {
                // androidx concurrentFuturesKtx 没有被当前库其它依赖依赖，大概率不存在，使用方案2加载
                Class.forName("androidx.concurrent.futures.ListenableFutureKt")
                true
            } catch (_: ClassNotFoundException) {
                false
            }
        },

        /**
         * 判断FlexBox依赖是否被引入了，调用一下它的方法避免class引用被编译器优化掉
         */
        FLEXBOX {
            override fun initValue(): Boolean {
                try {
                    // flexbox库大概率不存在，使用方案2加载
                    Class.forName("com.google.android.flexbox.FlexboxLayoutManager")
                    return true
                } catch (_: ClassNotFoundException) {
                    return false
                }
            }
        },
        PROTOBUF_FULL {
            override fun initValue(): Boolean = if (PROTOBUF_LITE._value == false) {
                false
            } else try {
                // protobuf库大概率不存在，使用方案2加载
                Class.forName("com.google.protobuf.Message")
                true
            } catch (_: ClassNotFoundException) {
                false
            }
        },
        PROTOBUF_LITE {
            override fun initValue(): Boolean = if (PROTOBUF_FULL._value == true) {
                // 如果完整库有被引入，lite就一定包含在内
                true
            } else try {
                // protobufLite库大概率不存在，使用方案2加载
                // lite库会keep这个类 https://github.com/protocolbuffers/protobuf/blob/main/java/lite/proguard.pgcfg
                Class.forName("com.google.protobuf.GeneratedMessageLite")
                true
            } catch (_: ClassNotFoundException) {
                false
            }
        },
        GSON {
            override fun initValue(): Boolean = try {
                // gson 轻量级常用，大概率会存在，使用方案1加载
                TypeToken::class.java.toString()
                true
            } catch (_: NoClassDefFoundError) {
                false
            }
        },
        MMKV {
            override fun initValue(): Boolean = try {
                // mmkv库大概率不存在，使用方案2加载
                Class.forName("com.tencent.mmkv.MMKV")
                true
            } catch (_: ClassNotFoundException) {
                false
            }
        };

        @Volatile
        private var _value: Boolean? = null

        protected abstract fun initValue(): Boolean

        override val value: Boolean
            get() = _value ?: synchronized(this) {
                _value ?: run {
                    val v = initValue()
                    _value = v
                    v
                }
            }

        override fun isInitialized(): Boolean {
            return _value != null
        }
        //</editor-fold>

        companion object : DependencyChecker {
            override val material: Boolean by MATERIAL
            override val guavaListenableFuture: Boolean by GUAVA_LISTENABLE_FUTURE
            override val guava: Boolean by GUAVA
            override val androidXListenableFuture: Boolean by ANDROID_X_LISTENABLE_FUTURE
            override val flexBox: Boolean by FLEXBOX
            override val protobufFull: Boolean by PROTOBUF_FULL
            override val protobufLite: Boolean by PROTOBUF_LITE
            override val gson: Boolean by GSON
            override val mmkv: Boolean by MMKV
        }
    }
}
