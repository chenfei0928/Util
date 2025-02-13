package io.github.chenfei0928.content.sp.saver

import android.content.SharedPreferences
import io.github.chenfei0928.preference.sp.SpSaverFieldAccessor
import io.github.chenfei0928.preference.sp.SpSaverPreferenceDataStore
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
    interface Delegate<SpSaver : AbsSpSaver<SpSaver, *, *>, T> : ReadWriteProperty<SpSaver, T> {
        // 被序列化后的数据的类型
        val spValueType: PreferenceType
        fun obtainDefaultKey(property: KProperty<*>): String
    }

    interface Decorate<SpSaver : AbsSpSaver<SpSaver, *, *>, T> {
        val saver: Delegate<SpSaver, T>
    }

    interface DefaultValue<T> {
        val defaultValue: T
    }

    interface AbsSpDelegate<SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
            Sp : SharedPreferences,
            Ed : SharedPreferences.Editor,
            T> : Delegate<SpSaver, T> {
        val SpSaver.sp: Sp
            get() = this.sp
        val SpSaver.editor: Ed
            get() = this.editor
    }
    //</editor-fold>

    val dataStore: SpSaverPreferenceDataStore<SpSaver> by lazy {
        @Suppress("UNCHECKED_CAST")
        SpSaverPreferenceDataStore<SpSaver>(this as SpSaver)
    }

    override fun toString(): String = dataStore.toPropertyString()

    /**
     * 为解决委托字段声明后缓存的编译器类型检查无法进行类型推导问题，
     * 添加此方法用于让使用处可以只构建委托，由此方法进行缓存
     */
    protected inline fun <T> dataStore(
        block: () -> Delegate<SpSaver, T>
    ): PropertyDelegateProvider<SpSaver, ReadWriteProperty<SpSaver, T>> =
        DataStoreDelegateStoreProvider(enableFieldObservable, block())

    internal open fun onPropertyAdded(field: SpSaverFieldAccessor.Field<SpSaver, *>) {
        // noop
    }

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

        fun <Sp : SharedPreferences> getSp(spSaver: AbsSpSaver<*, Sp, *>): Sp = spSaver.sp
    }
}
