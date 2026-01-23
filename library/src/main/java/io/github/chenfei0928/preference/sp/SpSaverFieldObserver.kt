package io.github.chenfei0928.preference.sp

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.LifecycleBindOnSharedPreferenceChangeListener
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.convert.SpValueObservable
import io.github.chenfei0928.lifecycle.ILiveListener
import io.github.chenfei0928.lifecycle.LiveListeners
import io.github.chenfei0928.lifecycle.MediatorLiveListeners
import io.github.chenfei0928.util.Log
import kotlin.reflect.KProperty

/**
 * @author chenf()
 * @date 2026-01-23 14:49
 */
class SpSaverFieldObserver<SpSaver : AbsSpSaver<SpSaver, *, *>>(
    saver: SpSaver,
    fieldAccessor: SpSaverFieldAccessor<SpSaver> = SpSaverFieldAccessor.Impl(saver),
    private val sp: SharedPreferences,
    private val enableFieldObservable: Boolean,
) : SpSaverFieldAccessorCache<SpSaver>(saver, fieldAccessor) {

    internal fun onPropertyAdded(field: SpSaverFieldAccessor.Field<SpSaver, *>) {
        if (!enableFieldObservable) {
            return
        }
        val observable = field.observable
            ?: return
        privateAnyPropertySetCallback.addSource(observable) { newValue ->
            val callbackValue = field to newValue
            privateAnyPropertySetCallback.forEach { it(callbackValue) }
        }
    }

    fun <V> getPropertyObservable(property: KProperty<V>): ILiveListener<(V) -> Unit> {
        return if (enableFieldObservable) {
            findFieldOrNullByProperty(property)
                ?.observable
                ?: SpValueObservable.find(saver.fieldAccessorCache.getDelegateOrByReflect(property))
                ?: throw IllegalArgumentException(
                    "没有找到 SpValueObservable，已经为它包装了 dataStore ？$property"
                )
        } else {
            val delegate = getDelegateOrByReflect(property)
            SpChangeLiveListeners.NewValue(this, delegate.getLocalStorageKey(property))
        }
    }

    private val spChangeListeners: LiveListeners<(Pair<SpSaverFieldAccessor.Field<SpSaver, *>, *>) -> Unit> =
        SpChangeLiveListeners.AllProperty(this)
    private val privateAnyPropertySetCallback =
        MediatorLiveListeners<(Pair<SpSaverFieldAccessor.Field<SpSaver, *>, *>) -> Unit>()

    val anyPropertySetCallback: ILiveListener<(Pair<SpSaverFieldAccessor.Field<SpSaver, *>, *>) -> Unit>
        get() = if (enableFieldObservable) {
            privateAnyPropertySetCallback
        } else {
            spChangeListeners
        }

    private abstract class SpChangeLiveListeners<SpSaver : AbsSpSaver<SpSaver, *, *>, Observer>(
        protected val observer: SpSaverFieldObserver<SpSaver>,
        override val filterKey: String? = null,
    ) : LiveListeners<Observer>(), LifecycleBindOnSharedPreferenceChangeListener {

        override fun onChangedOrClear(sharedPreferences: SharedPreferences, key: String?) {
            val fields: List<SpSaverFieldAccessor.Field<SpSaver, *>> = if (key == null) {
                // Android R以上时 clear sp，会回调null，R以下时clear时不会回调
                observer.spSaverPropertyDelegateFields
            } else {
                // 根据key获取其对应的AbsSpSaver字段
                val fields: List<SpSaverFieldAccessor.Field<SpSaver, *>> =
                    observer.spSaverPropertyDelegateFields
                        .filter { it.localStorageKey == key }
                // 找得到属性，回调通知该字段被更改
                fields.ifEmpty {
                    Log.d(TAG, buildString {
                        append("registerOnSharedPreferenceChangeListener: ")
                        append("cannot found property of the key($key) in class ")
                        append(observer.saver.javaClass.simpleName)
                    })
                    return@onChangedOrClear
                }
            }
            fields.forEach { field -> onChangedOrClear(field) }
        }

        protected abstract fun onChangedOrClear(field: SpSaverFieldAccessor.Field<SpSaver, *>)

        override fun onActive() {
            super.onActive()
            observer.sp.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onInactive() {
            super.onInactive()
            observer.sp.unregisterOnSharedPreferenceChangeListener(this)
        }

        class NewValue<SpSaver : AbsSpSaver<SpSaver, *, *>, V>(
            observer: SpSaverFieldObserver<SpSaver>,
            filterKey: String?,
        ) : SpChangeLiveListeners<SpSaver, (V) -> Unit>(observer, filterKey) {
            override fun onChangedOrClear(field: SpSaverFieldAccessor.Field<SpSaver, *>) {
                val newValue = field.get(observer.saver)
                forEach { it(newValue as V) }
            }
        }

        class AllProperty<SpSaver : AbsSpSaver<SpSaver, *, *>>(
            observer: SpSaverFieldObserver<SpSaver>,
        ) : SpChangeLiveListeners<SpSaver, (Pair<SpSaverFieldAccessor.Field<SpSaver, *>, *>) -> Unit>(
            observer, null
        ) {
            override fun onChangedOrClear(field: SpSaverFieldAccessor.Field<SpSaver, *>) {
                val callbackValue = field to field.get(observer.saver)
                forEach { it(callbackValue) }
            }
        }
    }

    companion object {
        private const val TAG = "Ut_SpSaverFieldObserver"
    }
}
