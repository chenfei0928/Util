package io.github.chenfei0928.preference

import io.github.chenfei0928.preference.base.BasePreferenceDataStore
import io.github.chenfei0928.preference.base.FieldAccessor
import io.github.chenfei0928.preference.base.FieldAccessorHelper

/**
 * @author chenf()
 * @date 2024-12-26 18:30
 */
class LocalStoragePreferenceDataStore<T : Any>(
    private val storage: Storage<T>,
    private val writeNow: Boolean = true,
    private val fieldAccessor: FieldAccessorHelper<T> = FieldAccessorHelper.Impl(true),
) : BasePreferenceDataStore<T>(fieldAccessor), FieldAccessorHelper<T> by fieldAccessor {

    override fun <V> FieldAccessor.Field<T, V>.set(value: V) {
        storage.write(setValue(storage.getCacheOrLoad(), value), writeNow)
    }

    override fun <V> FieldAccessor.Field<T, V>.get(): V =
        getValue(storage.getCacheOrLoad())

    interface Storage<T : Any> {
        fun getCacheOrLoad(): T
        fun write(value: T, writeNow: Boolean = true)
    }
}
