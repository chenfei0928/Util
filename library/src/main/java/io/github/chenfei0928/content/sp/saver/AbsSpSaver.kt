package io.github.chenfei0928.content.sp.saver

import android.content.SharedPreferences
import io.github.chenfei0928.preference.sp.SpSaverPreferenceDataStore
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 对提供[SharedPreferences]字段存取框架进行支持，子类通过属性委托来快速实现对sp中字段的存取功能。
 * 但在对字段值进行修改时，其不会被立刻写入，需要手动调用[commit]或[apply]进行保存，或使用[edit]来进行编辑。
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-15 15:34
 */
abstract class AbsSpSaver<SpSaver : AbsSpSaver<SpSaver>> : SpCommit {
    protected abstract val sp: SharedPreferences
    protected abstract val editor: SharedPreferences.Editor

    //<editor-fold desc="字段委托" defaultstatus="collapsed">
    abstract class AbsSpDelegate<T>(
        // 被序列化后的数据的类型
        val spValueType: PreferenceType,
    ) : ReadWriteProperty<AbsSpSaver<*>, T> {

        abstract fun obtainDefaultKey(property: KProperty<*>): String

        override fun getValue(thisRef: AbsSpSaver<*>, property: KProperty<*>): T {
            val key = obtainDefaultKey(property)
            return getValue(thisRef.sp, key)
        }

        internal abstract fun getValue(sp: SharedPreferences, key: String): T

        override fun setValue(thisRef: AbsSpSaver<*>, property: KProperty<*>, value: T) {
            val key = obtainDefaultKey(property)
            putValue(thisRef.editor, key, value)
        }

        internal abstract fun putValue(editor: SharedPreferences.Editor, key: String, value: T)
    }
    //</editor-fold>

    val dataStore: SpSaverPreferenceDataStore<SpSaver> by lazy {
        @Suppress("UNCHECKED_CAST")
        SpSaverPreferenceDataStore<SpSaver>(this as SpSaver)
    }

    override fun toString(): String = dataStore.toPropertyString()

    companion object {
        @JvmStatic
        protected inline fun AbsSpSaver<*>.edit(
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

        fun getSp(spSaver: AbsSpSaver<*>): SharedPreferences = spSaver.sp
    }
}
