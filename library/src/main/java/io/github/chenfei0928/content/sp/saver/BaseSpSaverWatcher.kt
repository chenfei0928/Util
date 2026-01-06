package io.github.chenfei0928.content.sp.saver

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import io.github.chenfei0928.content.sp.registerOnSharedPreferenceChangeListener
import io.github.chenfei0928.preference.sp.SpSaverFieldAccessor
import io.github.chenfei0928.util.Log
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

private const val TAG = "Ut_AbsSpSaverWatcher"

/**
 * 监听sp的变化并通知任何字段变化
 *
 * 只会回调通过 [DataStoreDelegateStoreProvider.dataStore]、
 * [SpSaverFieldAccessor.property] 在 [AbsSpSaver.fieldAccessorCache] 中注册并含有委托的字段。
 *
 * 以 [DataStoreDelegateStoreProvider.dataStore] 注册的字段返回的均会回调 [KProperty1]、[KMutableProperty1] 类型，
 * 以 `property(::int)` 方式注册的字段均会回调 [KProperty0]、[KMutableProperty0] 类型，
 * 以 `property(XxxSpSaver::int)` 方式注册的字段均会回调 [KProperty1]、[KMutableProperty1] 类型，
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-05 16:12
 */
fun <SpSaver : BaseSpSaver<SpSaver>> SpSaver.registerOnSpPropertyChangeListener(
    owner: LifecycleOwner,
    @MainThread callback: (field: SpSaverFieldAccessor.Field<SpSaver, *>) -> Unit,
) {
    val spSaver = this
    AbsSpSaver.getSp(this).registerOnSharedPreferenceChangeListener(owner) { key ->
        if (key == null) {
            // Android R以上时 clear sp，会回调null，R以下时clear时不会回调
            spSaver.fieldAccessorCache
                .spSaverPropertyDelegateFields
                .forEach { callback(it) }
        } else {
            // 根据key获取其对应的AbsSpSaver字段
            val fields = spSaver.fieldAccessorCache
                .spSaverPropertyDelegateFields
                .filter { it.localStorageKey == key }
            // 找得到属性，回调通知该字段被更改
            if (fields.isEmpty()) {
                Log.d(TAG, buildString {
                    append("registerOnSharedPreferenceChangeListener: ")
                    append("cannot found property of the key($key) in class ")
                    append(spSaver.javaClass.simpleName)
                })
            } else {
                fields.forEach { callback(it) }
            }
        }
    }
}
