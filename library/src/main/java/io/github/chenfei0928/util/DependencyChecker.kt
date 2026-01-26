package io.github.chenfei0928.util

import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.common.reflect.GoogleTypes
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.reflect.TypeToken
import com.google.protobuf.TextFormat
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

    // androidx.databinding:databinding-xxx
    val dataBinding: Boolean

    // https://github.com/google/flexbox-layout
    val flexBox: Boolean

    /**
     * 如果没有 protobuf 依赖，此字段返回null，否则返回其依赖信息
     *
     * https://github.com/protocolbuffers/protobuf
     */
    val protobuf: Protobuf?

    /**
     * 返回 GoogleTypes 接口，用于内部生成Type使用
     *
     * [Gson](https://github.com/google/gson)、
     * [Google Guava 工具库](https://github.com/google/guava)
     */
    val googleTypes: GoogleTypes

    //  https://github.com/Tencent/MMKV
    val mmkv: Boolean

    /**
     * Kotlin 编译器是否将 [kotlin.reflect.KProperty.returnType] 编译进二进制文件
     *
     * 为 true 时获取成本将变低
     *
     * 实测 Kt2.2 时未默认提供该功能
     */
    val ktKPropertyCompiledKType: Boolean

    /**
     * Kotlin 编译器是否将 [kotlin.reflect.KProperty0.getDelegate]、
     * [kotlin.reflect.KProperty1.getDelegate]、
     * [kotlin.reflect.KProperty2.getDelegate] 编译进二进制文件
     *
     * 为 true 时获取成本将变低
     *
     * 实测 Kt2.2 时未默认提供该功能
     */
    val ktKPropertyCompiledDelegate: Boolean

    //<editor-fold desc="Protobuf依赖信息" defaultstatus="collapsed">
    /**
     * Protobuf 依赖信息，如果没有依赖，使用处的值类型定义应该为nullable，以用于标记没有依赖Protobuf依赖的情况
     *
     * @property hasFullDependency 是否包含完整的 protobuf 依赖，即包含了 protobuf-lite 与 protobuf-java 的所有类
     * @property useTextFormatPrinter 是否使用在 Full 版 `4.28.0` 加入并成为推荐的 [TextFormat.Printer.emittingSingleLine]
     * 设置后的 [TextFormat.Printer]，替代 [TextFormat.shortDebugString] 打印 Protobuf 消息
     */
    enum class Protobuf(
        internal val hasFullDependency: Boolean,
        internal val useTextFormatPrinter: Boolean,
    ) {
        /**
         * Lite 版本依赖，不包含完整的 protobuf-java 依赖
         *
         * 同时不支持 [com.google.protobuf.toShortString] 对消息进行 toString 单行输出
         */
        LITE(false, false),

        /**
         * Full 版本依赖，包含了 protobuf-java 的所有类
         *
         * 但依赖版本低于 `4.28.0` 不支持 [TextFormat.Printer.emittingSingleLine]
         */
        FULL(true, false),

        /**
         * Full 版本依赖，并版本大于等于 `4.28.0`，包含了 protobuf-java 的所有类，
         * 并支持 [TextFormat.Printer.emittingSingleLine]
         */
        FULL_ABOVE_4_28(true, true);
    }
    //</editor-fold>

    companion object : DependencyChecker {
        override val material: Boolean get() = UtilInitializer.sdkDependency.material
        override val guavaListenableFuture: Boolean get() = UtilInitializer.sdkDependency.guavaListenableFuture
        override val guava: Boolean get() = UtilInitializer.sdkDependency.guava
        override val androidXListenableFuture: Boolean get() = UtilInitializer.sdkDependency.androidXListenableFuture
        override val dataBinding: Boolean get() = UtilInitializer.sdkDependency.dataBinding
        override val flexBox: Boolean get() = UtilInitializer.sdkDependency.flexBox
        override val protobuf: Protobuf? get() = UtilInitializer.sdkDependency.protobuf
        override val googleTypes: GoogleTypes get() = UtilInitializer.sdkDependency.googleTypes
        override val mmkv: Boolean get() = UtilInitializer.sdkDependency.mmkv
        override val ktKPropertyCompiledKType: Boolean
            get() = UtilInitializer.sdkDependency.ktKPropertyCompiledKType
        override val ktKPropertyCompiledDelegate: Boolean
            get() = UtilInitializer.sdkDependency.ktKPropertyCompiledDelegate
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
        override val dataBinding: Boolean,
        override val protobuf: Protobuf?,
        override val googleTypes: GoogleTypes,
        override val mmkv: Boolean,
        override val ktKPropertyCompiledKType: Boolean = false,
        override val ktKPropertyCompiledDelegate: Boolean = false,
    ) : DependencyChecker

    //<editor-fold desc="使用反射获取" defaultstatus="collapsed">
    /**
     * 用于处理部分函数中对部分库有额外处理逻辑的判断逻辑
     *
     * 对 `try class.toString catch NoClassDefFoundError` [reference]
     * 与 `try Class.forName catch ClassNotFoundException` [forName]
     * 进行性能测试：
     *
     *  * [ByReflectLazy.MATERIAL] 获取 [BottomSheetDialog]
     *  * [ByReflectLazy.GUAVA_LISTENABLE_FUTURE] 获取 [ListenableFuture]
     *  * [ByReflectLazy.ANDROID_X_LISTENABLE_FUTURE] 获取 `ListenableFutureKt`
     *  * [ByReflectLazy.FLEXBOX] 获取 `FlexboxLayoutManager`
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
     *  * 对于大概率存在的类，使用方案1进行判断 [reference]
     *  * 对于大概率不存在的类，使用方案2进行判断 [forName]
     *
     * @author chenf()
     * @date 2024-11-29 14:16
     */
    enum class ByReflectLazy : Lazy<Boolean> {
        //<editor-fold desc="使用反射或直接引入类，并try-catch来判断对应的类是否已经依赖" defaultstatus="collapsed">
        /**
         * 判断Material依赖是否被引入了，调用一下它的方法避免class引用被编译器优化掉
         */
        MATERIAL {
            // material 库存在大量标准控件，大概率存在，使用方案1加载
            override fun initValue(): Boolean = reference<BottomSheetDialog>()
        },

        /**
         * Guava Listenable 依赖可以判断 [ListenableFuture] 类是否存在，它的`await`方法在另一个deps中，
         * 需要额外判断 `ListenableFutureKt` 类是否存在，并调用一下它们的方法避免编译器优化将class优化掉。
         *
         * GuavaListenable 与 Guava 库不是同一个库
         */
        GUAVA_LISTENABLE_FUTURE {
            // guava listenerFuture 被androidx core依赖，大概率存在，使用方案1加载
            override fun initValue(): Boolean = reference<ListenableFuture<*>>()
        },

        GUAVA {
            // guava库大概率不存在，使用方案2加载
            override fun initValue(): Boolean = forName("com.google.common.reflect.TypeToken")
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
            } else {
                // androidx concurrentFuturesKtx 没有被当前库其它依赖依赖，大概率不存在，使用方案2加载
                forName("androidx.concurrent.futures.ListenableFutureKt")
            }
        },
        DATA_BINDING {
            // dataBinding库大概率不存在，使用方案2加载
            override fun initValue(): Boolean = forName("androidx.databinding.DataBindingUtil")
        },

        /**
         * 判断FlexBox依赖是否被引入了，调用一下它的方法避免class引用被编译器优化掉
         */
        FLEXBOX {
            // flexbox库大概率不存在，使用方案2加载
            override fun initValue(): Boolean =
                forName("com.google.android.flexbox.FlexboxLayoutManager")
        },
        PROTOBUF_FULL {
            override fun initValue(): Boolean = if (PROTOBUF_LITE._value == false) {
                false
            } else {
                // protobuf库大概率不存在，使用方案2加载
                forName("com.google.protobuf.Message")
            }
        },
        PROTOBUF_LITE {
            override fun initValue(): Boolean = if (PROTOBUF_FULL._value == true) {
                // 如果完整库有被引入，lite就一定包含在内
                true
            } else {
                // protobufLite库大概率不存在，使用方案2加载
                // lite库会keep这个类 https://github.com/protocolbuffers/protobuf/blob/main/java/lite/proguard.pgcfg
                forName("com.google.protobuf.GeneratedMessageLite")
            }
        },
        GSON {
            // gson 轻量级常用，大概率会存在，使用方案1加载
            override fun initValue(): Boolean = reference<TypeToken<*>>()
        },
        MMKV {
            // mmkv库大概率不存在，使用方案2加载
            override fun initValue(): Boolean = forName("com.tencent.mmkv.MMKV")
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

        // 大概率会存在的，使用方案1加载
        protected inline fun <reified T> reference() = try {
            T::class.java.toString()
            true
        } catch (_: NoClassDefFoundError) {
            false
        }

        // 大概率不存在的，使用方案2加载
        protected fun forName(name: String) = try {
            Class.forName(name)
            true
        } catch (_: ClassNotFoundException) {
            false
        }
        //</editor-fold>

        companion object : DependencyChecker {
            val PROTOBUF: Lazy<Protobuf?> = lazy(LazyThreadSafetyMode.NONE) {
                if (!PROTOBUF_LITE.value) null
                else if (!PROTOBUF_FULL.value) Protobuf.LITE
                else if (
                    TextFormat::shortDebugString.annotations.find { it is java.lang.Deprecated } != null
                ) Protobuf.FULL_ABOVE_4_28
                else Protobuf.FULL
            }

            val GOOGLE_TYPES: Lazy<GoogleTypes> = lazy(LazyThreadSafetyMode.NONE) {
                if (GSON.value) GoogleTypes.Gson
                else if (GUAVA.value) GoogleTypes.Guava
                else GoogleTypes.NotImpl
            }

            override val material: Boolean by MATERIAL
            override val guavaListenableFuture: Boolean by GUAVA_LISTENABLE_FUTURE
            override val guava: Boolean by GUAVA
            override val androidXListenableFuture: Boolean by ANDROID_X_LISTENABLE_FUTURE
            override val dataBinding: Boolean by DATA_BINDING
            override val flexBox: Boolean by FLEXBOX
            override val protobuf: Protobuf? by PROTOBUF
            override val googleTypes: GoogleTypes by GOOGLE_TYPES
            override val mmkv: Boolean by MMKV
            override val ktKPropertyCompiledKType: Boolean = false
            override val ktKPropertyCompiledDelegate: Boolean = false
        }
    }
    //</editor-fold>
}
