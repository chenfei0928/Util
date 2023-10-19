package io.github.chenfei0928.reflect

import java.lang.invoke.MethodHandles
import java.lang.reflect.Proxy

/**
 * 创建内部使用集合来保存监听器的监听器代理
 * 传入一个集合来保存监听器实现，将返回该监听器的一个代理实例
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-09-21 14:11
 */
object ListenersProxy {

    inline fun <reified T : Any> newListenersProxy(
        listenerImpls: MutableCollection<T> = arrayListOf()
    ) = newListenersProxy(T::class.java, listenerImpls)

    fun <T : Any> newListenersProxy(
        clazz: Class<T>, listenerImpls: MutableCollection<T> = arrayListOf()
    ): T = Proxy.newProxyInstance(
        clazz.classLoader, arrayOf(clazz, MutableCollection::class.java)
    ) { proxy, method, args ->
        // If the method is a method from Object then defer to normal invocation.
        if (method.declaringClass == Any::class.java) {
            return@newProxyInstance method.safeInvoke(this, args)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && method.isDefault) {
            // Because the service interface might not be public, we need to use a MethodHandle lookup
            // that ignores the visibility of the declaringClass.
            val constructor = MethodHandles.Lookup::class.java.getDeclaredConstructor(
                Class::class.java, Int::class.javaPrimitiveType
            )
            constructor.isAccessible = true
            return@newProxyInstance constructor
                .newInstance(method.declaringClass, -1 /* trusted */)
                .unreflectSpecial(method, method.declaringClass)
                .bindTo(proxy)
                .run {
                    if (args == null) {
                        invokeWithArguments()
                    } else {
                        invokeWithArguments(*args)
                    }
                }
        }
        // 如果该方法是集合接口定义的方法，对监听器集合进行操作
        if (method.declaringClass == MutableCollection::class.java) {
            return@newProxyInstance method.safeInvoke(listenerImpls, args)
        } else {
            // 否则执行监听器
            var rst: Any? = null
            listenerImpls.forEach {
                rst = method.safeInvoke(it, args)
            }
            return@newProxyInstance rst
        }
    } as T

    fun <T : Any> newEmptyListener(
        clazz: Class<T>
    ): T = Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz)) { _, _, _ -> } as T

    /**
     * 将多个监听器的回调封装为一个监听器
     */
    fun <T : Any> newListenersProxy(clazz: Class<T>, vararg listeners: T): T {
        val find = listeners.find { it is MutableCollection<*> } ?: newListenersProxy(clazz)
        val mutableCollection = find as MutableCollection<T>
        for (listener in listeners) {
            if (find !== listener) {
                mutableCollection.add(listener)
            }
        }
        return find
    }

    inline fun <reified T : Any> newImplByGetter(
        getter: Lazy<T>
    ): T = newImplByGetter(T::class.java) { getter.value }

    inline fun <reified T : Any> newImplByGetter(
        crossinline getter: () -> T
    ): T = newImplByGetter(T::class.java, getter)

    /**
     * 创建一个懒获取的代理实现，该代理对象可能不会实时获取到(通过一段代码来find或创建)或为懒加载
     */
    inline fun <T : Any> newImplByGetter(
        clazz: Class<T>, crossinline getter: () -> T
    ): T = Proxy.newProxyInstance(
        clazz.classLoader, arrayOf(clazz)
    ) { _, method, args ->
        return@newProxyInstance method.safeInvoke(getter(), args)
    } as T
}
