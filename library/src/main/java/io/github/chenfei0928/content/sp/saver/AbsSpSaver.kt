package io.github.chenfei0928.content.sp.saver

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.AbsSpSaver.Companion.edit
import io.github.chenfei0928.preference.sp.SpSaverFieldAccessor
import io.github.chenfei0928.preference.sp.SpSaverFieldObserver
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 对提供[SharedPreferences]字段存取框架进行支持，子类通过属性委托来快速实现对sp中字段的存取功能。
 * 但在对字段值进行修改时，其不会被立刻写入，需要手动调用[commit]或[apply]进行保存，或使用[edit]来进行编辑。
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-15 15:34
 */
abstract class AbsSpSaver
<SpSaver : AbsSpSaver<SpSaver, Sp, Ed>, Sp : SharedPreferences, Ed : SharedPreferences.Editor>
constructor(
    protected val enableFieldObservable: Boolean
) : SpCommit {
    protected abstract val sp: Sp
    protected abstract val editor: Ed

    //<editor-fold desc="字段委托" defaultstatus="collapsed">
    interface Delegate<SpSaver : AbsSpSaver<SpSaver, *, *>, V> : ReadWriteProperty<SpSaver, V> {
        // 被序列化后的数据的类型
        val spValueType: PreferenceType<V & Any>
        fun getLocalStorageKey(property: KProperty<*>): String
    }

    /**
     * 装饰器，用于在字段委托上附加额外的功能。例如：字段值变更监听、字段值类型转换等。
     *
     * @param SpSaver 子类类型
     * @param V 输入输出的字段类型
     */
    interface Decorate<SpSaver : AbsSpSaver<SpSaver, *, *>, V> {
        val saver: Delegate<SpSaver, V>

        companion object {
            inline fun <reified D : Delegate<SpSaver, V>, SpSaver : AbsSpSaver<SpSaver, *, *>, V> findByType(
                outDelegate: Delegate<SpSaver, V>
            ): D? {
                var delegate: Delegate<SpSaver, V>? = outDelegate
                while (delegate != null && delegate !is D) {
                    delegate = if (delegate is Decorate<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        delegate.saver as Delegate<SpSaver, V>
                    } else null
                }
                return delegate
            }
        }
    }

    interface DefaultValue<V> {
        val defaultValue: V
    }

    interface AbsSpDelegate<SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
            Sp : SharedPreferences,
            Ed : SharedPreferences.Editor,
            V> : Delegate<SpSaver, V> {
        val SpSaver.sp: Sp
            get() = this.sp
        val SpSaver.editor: Ed
            get() = this.editor
    }
    //</editor-fold>

    val fieldAccessorCache: SpSaverFieldObserver<SpSaver> by lazy {
        @Suppress("UNCHECKED_CAST")
        SpSaverFieldObserver<SpSaver>(
            saver = this as SpSaver,
            sp = sp,
            enableFieldObservable = enableFieldObservable
        )
    }

    //<editor-fold desc="提供通用SpCommit的默认实现" defaultstatus="collapsed">
    override fun contains(key: String): Boolean = sp.contains(key)
    override fun contains(property: KProperty<*>): Boolean =
        sp.contains(fieldAccessorCache.findFieldByPropertyOrThrow(property).localStorageKey)

    final override fun remove(key: String) {
        editor.remove(key)
        // 查找存储值为该key的字段
        fieldAccessorCache.spSaverPropertyDelegateFields.filter { it.localStorageKey == key }
            .forEach(::onFieldValueRemoved)
    }

    final override fun remove(property: KProperty<*>) {
        val field = fieldAccessorCache.findFieldByPropertyOrThrow(property)
        editor.remove(field.localStorageKey)
        onFieldValueRemoved(field)
    }

    /**
     * 当一个字段的值被移除时，会调用此方法。
     *
     * @param field 被移除的字段信息。
     */
    protected open fun onFieldValueRemoved(field: SpSaverFieldAccessor.Field<SpSaver, *>) {
        // noop
    }

    override fun clear() {
        editor.clear()
    }

    override fun toString(): String = fieldAccessorCache.toSpSaverPropertyString()
    //</editor-fold>

    /**
     * 为解决委托字段声明后缓存的编译器类型检查无法进行类型推导问题，
     * 添加此方法用于让使用处可以只构建委托，由此方法进行缓存
     *
     * @param findSpAccessorDelegateIfStruct 传入 `true` 时会查找 spAccessor 委托信息。
     * 如果这个字段不用于 Preference 显示（较少会有需要 Preference 显示的结构体数据，
     * 因为 [androidx.preference.Preference] 的内容需要符合特定格式），
     * 传入 `false` 以减少方法执行时间消耗。
     */
    protected inline fun <V> dataStore(
        findSpAccessorDelegateIfStruct: Boolean = false,
        block: () -> Delegate<SpSaver, V>
    ): PropertyDelegateProvider<SpSaver, ReadWriteProperty<SpSaver, V>> =
        DataStoreDelegateStoreProvider(
            enableFieldObservable, findSpAccessorDelegateIfStruct, block()
        )

    companion object {
        @JvmStatic
        protected inline fun <Ed : SharedPreferences.Editor> AbsSpSaver<*, *, Ed>.edit(
            commit: Boolean = false, action: Ed.() -> Unit
        ) {
            val editor = editor
            action(editor)
            if (commit) {
                commit()
            } else {
                apply()
            }
        }
    }
}
