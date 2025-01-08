package io.github.chenfei0928.preference

import io.github.chenfei0928.preference.base.BasePreferenceDataStore
import io.github.chenfei0928.preference.base.FieldAccessor
import io.github.chenfei0928.preference.base.MutableFieldAccessor
import io.github.chenfei0928.repository.local.LocalFileStorage
import io.github.chenfei0928.repository.local.LocalFileStorage0

/**
 * 用于 [LocalFileStorage] 和 [LocalFileStorage0] 的首选项值访问
 *
 * @author chenf()
 * @date 2024-12-26 18:30
 */
@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class LocalStoragePreferenceDataStore<T : Any>(
    private val storage: Storage<T>,
    private val fieldAccessor: MutableFieldAccessor<T> = MutableFieldAccessor.Impl(true),
) : BasePreferenceDataStore<T>(fieldAccessor), MutableFieldAccessor<T> by fieldAccessor {

    override fun <V> FieldAccessor.Field<T, V>.set(value: V) {
        storage.set(setValue(storage.get(), value))
    }

    override fun <V> FieldAccessor.Field<T, V>.get(): V =
        getValue(storage.get())

    interface Storage<T : Any> {
        fun get(): T
        fun set(value: T)
    }
}
