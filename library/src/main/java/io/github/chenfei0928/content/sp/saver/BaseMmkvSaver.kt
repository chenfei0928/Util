package io.github.chenfei0928.content.sp.saver

import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.convert.SpValueObservable
import io.github.chenfei0928.lifecycle.ILiveListener
import io.github.chenfei0928.lifecycle.MediatorLiveListeners
import io.github.chenfei0928.preference.sp.SpSaverFieldAccessor
import kotlin.reflect.KProperty

/**
 * [MMKV] 的结构化key-value存储支持类
 *
 * 由于其创建不需要 [android.content.Context] 参数，所以子类可以安全的创建为单例类使用
 *
 * @author chenf()
 * @date 2025-02-10 17:53
 */
open class BaseMmkvSaver<SpSaver : BaseMmkvSaver<SpSaver>>(
    mmkv: MMKV,
    enableFieldObservable: Boolean = false
) : AbsSpSaver<SpSaver, MMKV, MMKV>(enableFieldObservable) {
    override val sp: MMKV = mmkv
    override val editor: MMKV = mmkv

    constructor(
        mmapID: String,
        mode: Int = MMKV.SINGLE_PROCESS_MODE,
        cryptKey: String? = null,
        rootPath: String? = null,
        expectedCapacity: Long = MMKV.ExpireNever.toLong(),
        enableFieldObservable: Boolean = false
    ) : this(
        MMKV.mmkvWithID(mmapID, mode, cryptKey, rootPath, expectedCapacity), enableFieldObservable
    )

    override fun getSpAll(): Map<String, *> {
        return dataStore.spSaverPropertyDelegateFields.associate {
            @Suppress("UNCHECKED_CAST")
            it.pdsKey to it.get(this as SpSaver)
        }
    }

    override fun onFieldValueRemoved(field: SpSaverFieldAccessor.Field<SpSaver, *>) {
        super.onFieldValueRemoved(field)
        if (!enableFieldObservable)
            return
        field.observable?.onLocalStorageChange(this as SpSaver, field.property)
    }

    override fun clear() {
        super.clear()
        if (!enableFieldObservable) {
            return
        }
        dataStore.spSaverPropertyDelegateFields.forEach { field ->
            field.observable?.onLocalStorageChange(this as SpSaver, field.property)
        }
    }

    override fun commit(): Boolean {
        return true
    }

    override fun apply() {
        // noop
    }

    //<editor-fold desc="提供监听字段值变更支持，使用要设置 enableFieldObservable 为 true" defaultstatus="collapsed">
    override fun onPropertyAdded(field: SpSaverFieldAccessor.Field<SpSaver, *>) {
        super.onPropertyAdded(field)
        if (!enableFieldObservable) {
            return
        }
        val observable = field.observable
            ?: return
        privateAnyPropertySetCallback.addSource(observable) {
            val callbackValue = field to it
            privateAnyPropertySetCallback.forEach { it(callbackValue) }
        }
    }

    fun <V> getPropertyObservable(property: KProperty<V>): ILiveListener<(V) -> Unit> {
        require(enableFieldObservable) {
            "to use getPropertyObservable, you must set enableFieldObservable to true.\n" +
                    "使用 getPropertyObservable 要设置 enableFieldObservable 为 true"
        }
        return dataStore.findFieldOrNullByProperty(property)
            ?.observable
            ?: SpValueObservable.find(dataStore.getDelegateByReflect(property))
            ?: throw IllegalArgumentException(
                "没有找到 SpValueObservable，已经为它包装了 dataStore ？$property"
            )
    }

    private val privateAnyPropertySetCallback
            : MediatorLiveListeners<(Pair<SpSaverFieldAccessor.Field<SpSaver, *>, *>) -> Unit> =
        MediatorLiveListeners()

    val anyPropertySetCallback: ILiveListener<(Pair<SpSaverFieldAccessor.Field<SpSaver, *>, *>) -> Unit>
        get() {
            require(enableFieldObservable) {
                "to use anyPropertySetCallback, you must set enableFieldObservable to true.\n" +
                        "使用 anyPropertySetCallback 要设置 enableFieldObservable 为 true"
            }
            return privateAnyPropertySetCallback
        }
    //</editor-fold>
}
