package io.github.chenfei0928.content.sp.saver

import android.content.SharedPreferences
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
    : SpCommit {
    protected abstract val sp: Sp
    protected abstract val editor: Ed

    //<editor-fold desc="字段委托" defaultstatus="collapsed">
    interface AbsSpDelegate<SpSaver : AbsSpSaver<SpSaver, *, *>, T> :
        ReadWriteProperty<SpSaver, T> {
        // 被序列化后的数据的类型
        val spValueType: PreferenceType
        fun obtainDefaultKey(property: KProperty<*>): String
    }

    abstract class AbsSpDelegateImpl<SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
            Sp : SharedPreferences,
            Ed : SharedPreferences.Editor,
            T>(
        // 被序列化后的数据的类型
        override val spValueType: PreferenceType,
    ) : AbsSpDelegate<SpSaver, T> {

        final override fun getValue(thisRef: SpSaver, property: KProperty<*>): T {
            val key = obtainDefaultKey(property)
            // 允许子类处理key不存在时返回默认值
            return getValue(thisRef.sp, key)
        }

        internal abstract fun getValue(sp: Sp, key: String): T

        final override fun setValue(thisRef: SpSaver, property: KProperty<*>, value: T) {
            val key = obtainDefaultKey(property)
            val editor = thisRef.editor
            // put null时直接remove掉key，交由子类处理时均是nonnull
            if (value == null) {
                editor.remove(key)
            } else {
                putValue(editor, key, value)
            }
        }

        internal abstract fun putValue(editor: Ed, key: String, value: T & Any)
    }
    //</editor-fold>

    val dataStore: SpSaverPreferenceDataStore<SpSaver> by lazy {
        @Suppress("UNCHECKED_CAST")
        SpSaverPreferenceDataStore<SpSaver>(this as SpSaver)
    }

    override fun toString(): String = dataStore.toPropertyString()

    inline fun <T> dataStore(
        block: () -> AbsSpDelegateImpl<SpSaver, Sp, Ed, T>
    ): PropertyDelegateProvider<SpSaver, ReadWriteProperty<SpSaver, T>> =
        DataStoreDelegateStoreProvider(block())

    companion object {
        @JvmStatic
        protected inline fun AbsSpSaver<*, *, *>.edit(
            commit: Boolean = false, action: SharedPreferences.Editor.() -> Unit
        ) {
            val editor = editor
            action(editor)
            if (commit) {
                commit()
            } else {
                apply()
            }
        }

        fun getSp(spSaver: AbsSpSaver<*, *, *>): SharedPreferences = spSaver.sp
    }
}
