package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import android.util.Log
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.lang.deepEquals
import io.github.chenfei0928.lang.toStr
import kotlin.reflect.KProperty

/**
 * sp存储转换器，用于将sp不支持的数据结构转换为sp支持的数据结构
 *
 * 实现受限于 Kotlin 语法的约束，[getValue] 必须要返回 nullable。子类如果有遇到返回类型为 nonnull 的情况，
 * 则需要创建工厂方法并返回值类型设置为 [AbsSpSaver.Delegate] 并将泛型设置为 nonnull，
 * 在这其中可能会需要进行 `@Suppress("UNCHECKED_CAST")`。
 *
 * 子类可能会创建一些以 invoke 为名的工厂方法，这些工厂方法的返回值类型与类的类型一致，不允许返回 nonnull；
 * 如果是 reified inline 方法是为了减少传入T类型，此时需要保留其使用的原始构造器；
 * 如果是非 inline 方法是为了隐藏原始构造器，此时需要移除原始构造器；
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-09-03 13:38
 */
abstract class BaseSpConvert<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor,
        SpValueType,
        FieldType>
constructor(
    final override val saver: AbsSpSaver.Delegate<SpSaver, SpValueType>,
    final override val spValueType: PreferenceType,
) : AbsSpSaver.AbsSpDelegate<SpSaver, Sp, Ed, FieldType?>,
    AbsSpSaver.Decorate<SpSaver, SpValueType> {
    @Volatile
    private var cacheValue: Pair<SpValueType & Any, FieldType>? = null

    final override fun getLocalStorageKey(property: KProperty<*>): String {
        return saver.getLocalStorageKey(property)
    }

    @Synchronized
    override fun getValue(thisRef: SpSaver, property: KProperty<*>): FieldType? {
        return saver.getValue(thisRef, property)?.let {
            val cacheValue = cacheValue
            @Suppress("TooGenericExceptionCaught")
            if (cacheValue != null && cacheValue.first.deepEquals(it)) {
                cacheValue.second
            } else try {
                val t = onRead(it)
                this.cacheValue = it to t
                t
            } catch (e: Exception) {
                Log.e(TAG, buildString {
                    append("getValue: convert ")
                    append(property)
                    appendLine(" failed")
                    append("in ")
                    appendLine(this@BaseSpConvert)
                    append("origin is ")
                    append(it.toStr())
                }, e)
                null
            }
        } ?: if (this is AbsSpSaver.DefaultValue<*>) {
            @Suppress("UNCHECKED_CAST")
            defaultValue as FieldType
        } else null
    }

    override fun setValue(thisRef: SpSaver, property: KProperty<*>, value: FieldType?) {
        if (value == null) {
            thisRef.editor.remove(saver.getLocalStorageKey(property))
        } else {
            val t = onSave(value)
            cacheValue = t to value
            saver.setValue(thisRef, property, t)
        }
    }

    abstract fun onRead(value: SpValueType & Any): FieldType
    abstract fun onSave(value: FieldType & Any): SpValueType & Any

    override fun toString(): String {
        return "${this.javaClass.simpleName}(saver=$saver, spValueType=$spValueType)"
    }

    companion object {
        private const val TAG = "BaseSpConvert"
    }
}
