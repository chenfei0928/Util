package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import androidx.annotation.IntRange
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.util.DependencyChecker

class DoubleDelegate<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor>
private constructor(
    key: String? = null,
    defaultValue: Double = 0.0,
    @all:IntRange(from = 0) private val expireDurationInSecond: Int = MMKV.ExpireNever,
) : AbsSpAccessDefaultValueDelegate<SpSaver, Sp, Ed, Double>(
    key, PreferenceType.Struct<Double>(Double::class.java), defaultValue
) {
    private val defaultValueToLong = java.lang.Double.doubleToRawLongBits(defaultValue)

    override fun getValueImpl(sp: Sp, key: String): Double =
        if (!DependencyChecker.mmkv || sp !is MMKV) {
            java.lang.Double.longBitsToDouble(sp.getLong(key, defaultValueToLong))
        } else {
            sp.decodeDouble(key, defaultValue)
        }

    override fun putValue(editor: Ed, key: String, value: Double) {
        if (!DependencyChecker.mmkv || editor !is MMKV) {
            editor.putLong(key, java.lang.Double.doubleToRawLongBits(value))
        } else if (expireDurationInSecond <= MMKV.ExpireNever) {
            editor.encode(key, value)
        } else {
            editor.encode(key, java.lang.Double.doubleToRawLongBits(value), expireDurationInSecond)
        }
    }

    companion object {
        private var defaultInstance: DoubleDelegate<*, *, *>? = null

        operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor> invoke(
            key: String? = null,
            defaultValue: Double = 0.0,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): DoubleDelegate<SpSaver, Sp, Ed> {
            return if (!key.isNullOrEmpty() || defaultValue != 0.0 || expireDurationInSecond > 0) {
                DoubleDelegate(key, defaultValue, expireDurationInSecond)
            } else {
                @Suppress("UNCHECKED_CAST")
                defaultInstance as? DoubleDelegate<SpSaver, Sp, Ed>
            } ?: DoubleDelegate<SpSaver, Sp, Ed>().also { defaultInstance = it }
        }
    }
}
