package io.github.chenfei0928.content.sp.saver

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import io.github.chenfei0928.content.sp.registerOnSharedPreferenceChangeListener
import io.github.chenfei0928.preference.sp.SpSaverFieldAccessor
import io.github.chenfei0928.util.Log
import kotlin.reflect.KProperty

private const val TAG = "KW_AbsSpSaverWatcher"

/**
 * 监听sp的变化并通知任何字段变化
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-05 16:12
 */
fun <SpSaver : AbsSpSaver<SpSaver>> SpSaver.registerOnSharedPreferenceChangeListener(
    owner: LifecycleOwner,
    @MainThread callback: (key: KProperty<*>) -> Unit,
) {
    val spSaver = this
    AbsSpSaver.getSp(this).registerOnSharedPreferenceChangeListener(owner) { key ->
        if (key == null) {
            // Android R以上时 clear sp，会回调null，R以下时clear时不会回调
            spSaver.dataStore
                .spSaverPropertyDelegateFields
                .forEach {
                    callback(it.property)
                }
        } else {
            // 根据key获取其对应的AbsSpSaver字段
            val property = spSaver.dataStore
                .spSaverPropertyDelegateFields
                .filterIsInstance<SpSaverFieldAccessor.Impl.SpSaverPropertyDelegateField<*, *>>()
                .find {
                    it.outDelegate.obtainDefaultKey(it.property) == key
                }
                ?.property
            // 找得到属性，回调通知该字段被更改
            if (property == null) {
                Log.d(TAG, buildString {
                    append("registerOnSharedPreferenceChangeListener: ")
                    append("cannot found property of the key($key) in class ")
                    append(spSaver.javaClass.simpleName)
                })
            } else {
                callback(property)
            }
        }
    }
}
