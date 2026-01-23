package io.github.chenfei0928.preference.sp

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    /**
     * 根据指定 [property] 获取其对应值更新的 [LiveData]
     *
     * @param V 值类型，与 [property] 对应
     * @param property 字段的 [KProperty] 对象，用于获取其对应的值与委托信息。
     * @return
     */
    fun <V> getPropertyLiveData(property: KProperty<V>): LiveData<V> {
        return if (enableFieldObservable) {
            val spSaverObservable = findFieldOrNullByProperty(property)
                ?.observable
                ?: SpValueObservable.find(getDelegateOrByReflect(property))
                ?: throw IllegalArgumentException(
                    "没有找到 SpValueObservable，已经为它包装了 dataStore ？$property"
                )
            object : MutableLiveData<V>(spSaverObservable.getValue(saver, property)), (V) -> Unit {
                override fun invoke(p1: V) = postValue(p1)
                override fun onActive() {
                    super.onActive()
                    spSaverObservable.observeForever(this)
                }

                override fun onInactive() {
                    super.onInactive()
                    spSaverObservable.removeObserver(this)
                }
            }
        } else {
            val delegate = getDelegateOrByReflect(property)
            object : LifecycleBindOnSharedPreferenceChangeListener.SpValueLiveData<V>(sp) {
                override val filterKey: String = delegate.getLocalStorageKey(property)
                override fun valueGetter(): V = delegate.getValue(saver, property)
            }
        }
    }

    /**
     * 获取指定字段的 [ILiveListener] 对象，用于监听其值更新。
     *
     * @param V 值类型，与 [property] 对应
     * @param property 字段的 [KProperty] 对象，用于获取其对应的值与委托信息。
     * @return
     */
    fun <V> getPropertyObservable(property: KProperty<V>): ILiveListener<(V) -> Unit> {
        return if (enableFieldObservable) {
            findFieldOrNullByProperty(property)
                ?.observable
                ?: SpValueObservable.find(getDelegateOrByReflect(property))
                ?: throw IllegalArgumentException(
                    "没有找到 SpValueObservable，已经为它包装了 dataStore ？$property"
                )
        } else {
            val delegate = getDelegateOrByReflect(property)
            SpFieldChangeLiveListeners(this, property, delegate)
        }
    }

    private class SpFieldChangeLiveListeners<SpSaver : AbsSpSaver<SpSaver, *, *>, V>(
        private val observer: SpSaverFieldObserver<SpSaver>,
        private val property: KProperty<V>,
        private val delegate: AbsSpSaver.Delegate<SpSaver, V>,
    ) : LiveListeners<(V) -> Unit>(), LifecycleBindOnSharedPreferenceChangeListener {
        override val filterKey: String = delegate.getLocalStorageKey(property)

        override fun onChangedOrClear(sharedPreferences: SharedPreferences, key: String?) {
            val newValue = delegate.getValue(observer.saver, property)
            forEach { it(newValue as V) }
        }

        override fun onActive() {
            super.onActive()
            observer.sp.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onInactive() {
            super.onInactive()
            observer.sp.unregisterOnSharedPreferenceChangeListener(this)
        }
    }

    /**
     * 任何属性更新时都会回调，回调时传入字段和其新值。
     */
    val anyPropertyChangeCallback: ILiveListener<(Pair<SpSaverFieldAccessor.Field<SpSaver, *>, *>) -> Unit>
        get() = if (enableFieldObservable) {
            privateAnyPropertySetCallback
        } else {
            spChangeListeners
        }

    private val privateAnyPropertySetCallback =
        MediatorLiveListeners<(Pair<SpSaverFieldAccessor.Field<SpSaver, *>, *>) -> Unit>()
    private val spChangeListeners: LiveListeners<(Pair<SpSaverFieldAccessor.Field<SpSaver, *>, *>) -> Unit> =
        SpChangeLiveListeners(this)

    private class SpChangeLiveListeners<SpSaver : AbsSpSaver<SpSaver, *, *>>(
        private val observer: SpSaverFieldObserver<SpSaver>,
        override val filterKey: String? = null,
    ) : LiveListeners<(Pair<SpSaverFieldAccessor.Field<SpSaver, *>, *>) -> Unit>(),
        LifecycleBindOnSharedPreferenceChangeListener {

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

        fun onChangedOrClear(field: SpSaverFieldAccessor.Field<SpSaver, *>) {
            val callbackValue = field to field.get(observer.saver)
            forEach { it(callbackValue) }
        }

        override fun onActive() {
            super.onActive()
            observer.sp.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onInactive() {
            super.onInactive()
            observer.sp.unregisterOnSharedPreferenceChangeListener(this)
        }
    }

    companion object {
        private const val TAG = "Ut_SpSaverFieldObserver"
    }
}
