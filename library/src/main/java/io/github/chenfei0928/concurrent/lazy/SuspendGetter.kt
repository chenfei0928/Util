package io.github.chenfei0928.concurrent.lazy

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 以挂起方式获取并缓存数据
 *
 * @param T 获取到后的数据类型
 * @property context 执行内部获取数据的挂起函数[initializer]时附加的协程上下文
 * @property initializer 初始化获取数据的代码块
 * @constructor 构造器，创建[SuspendGetter]实例
 *
 * @author chenfei()
 * @date 2022-08-22 15:10
 */
class SuspendGetter<T>(
    private val context: CoroutineContext = EmptyCoroutineContext,
    private val initializer: suspend CoroutineScope.() -> T
) {
    // 原子化获取值的引用，用于避免多线程访问并发冲突
    private val atomicReference = AtomicReference<IGetter<T>>(
        IGetter.DeferredGetter(context, initializer)
    )

    /**
     * 重置值以下次获取时重新执行初始化[initializer]
     */
    fun reset() {
        atomicReference.set(IGetter.DeferredGetter(context, initializer))
    }

    /**
     * 判断结果值是否已初始化完成直接可用
     *
     * @return 如果已初始化完成，返回true
     */
    fun isInitialized(): Boolean = atomicReference.get() is IGetter.Object

    /**
     * 获取值，如果值已可用将会直接返回，否则会挂起并执行[initializer]代码块成功后返回结果
     *
     * @return 获取到的数据
     */
    tailrec suspend fun get(): T = when (val mayBeDeferred = atomicReference.get()) {
        is IGetter.Object<T> -> {
            // 已获取到的值，直接返回结果
            mayBeDeferred.obj
        }

        is IGetter.DeferredGetter<T> -> {
            // 未获取到
            val next = try {
                // 执行获取值的代码块，挂起并等待结果
                mayBeDeferred.invoke()
            } catch (e: Throwable) {
                // 获取值时出现了任何异常，重置执行状态并向上抛出异常
                reset()
                throw e
            }
            // 尝试更新获取结果到缓存中
            if (atomicReference.compareAndSet(mayBeDeferred, IGetter.Object(next))) {
                // 更新获取结果成功，直接返回结果
                next
            } else {
                // 更新结果失败（缓存中的值可能已经被更新），重新执行该方法以获取最新数据
                get()
            }
        }
    }

    /**
     * 使用自定义逻辑更新本地值
     *
     * @param block 更新值所用回调
     */
    suspend fun update(block: (T) -> T) {
        do {
            // 检查以确认缓存中的值是已获取完毕的
            get()
            val mayBeDeferred = atomicReference.get()
            // 如果缓存中的值是已获取完毕的，执行更新缓存值的回调并更新到缓存中，如果成功则函数结束，否则重试
        } while (mayBeDeferred !is IGetter.Object ||
            !atomicReference.compareAndSet(mayBeDeferred, IGetter.Object(block(mayBeDeferred.obj)))
        )
    }

    /**
     * 获取值的接口
     *
     * @param T 获取到后的数据类型
     */
    private sealed interface IGetter<T> {
        /**
         * 延期获取，未获取到，需要挂起以等待获取数据
         *
         * @param T 获取到后的数据类型
         * @constructor 构造器，创建一个[DeferredGetter]实例
         *
         * @property context 执行内部获取数据的挂起函数[initializer]时附加的协程上下文
         * @property initializer 初始化获取数据的代码块
         */
        class DeferredGetter<T>(
            context: CoroutineContext,
            initializer: suspend CoroutineScope.() -> T
        ) : IGetter<T>, suspend () -> T {
            // 延期获取代码的执行者实例
            private val deferred = CoroutineScope(context).async(
                start = CoroutineStart.LAZY,
                block = initializer
            )

            /**
             * 执行以获取结果
             *
             * @return 获取到的数据结果
             */
            override suspend fun invoke(): T {
                return deferred.await()
            }
        }

        /**
         * 已获取到的值，可以直接使用
         *
         * @param T 获取到后的数据类型
         * @property obj 获取到后的数据
         * @constructor 构造器，创建一个[Object]实例
         */
        class Object<T>(
            val obj: T
        ) : IGetter<T>
    }
}
