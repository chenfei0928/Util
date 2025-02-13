package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import androidx.annotation.IntRange
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.collection.mapToIntArray
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.content.sp.saver.delegate.StringDelegate

class IntArraySpConvert<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor>
constructor(
    saver: AbsSpSaver.Delegate<SpSaver, String?>,
    override val defaultValue: IntArray? = null,
) : BaseSpConvert<SpSaver, Sp, Ed, String?, IntArray>(
    saver, PreferenceType.NoSupportPreferenceDataStore
), AbsSpSaver.DefaultValue<IntArray?> {

    private constructor(
        key: String? = null,
        @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        defaultValue: IntArray? = null,
    ) : this(StringDelegate(key, expireDurationInSecond), defaultValue)

    override fun onRead(value: String): IntArray =
        value.split(",")
            .mapToIntArray { it.toIntOrNull() ?: -1 }

    override fun onSave(value: IntArray): String {
        return value.joinToString(",")
    }

    companion object {
        private var defaultInstance: IntArraySpConvert<*, *, *>? = null
        private var defaultNonnullInstance: IntArraySpConvert<*, *, *>? = null

        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor> nonnull(
            defaultValue: IntArray = intArrayOf(),
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, IntArray> {
            @Suppress("UNCHECKED_CAST")
            return if (defaultValue.isNotEmpty() || !key.isNullOrEmpty() || expireDurationInSecond > 0)
                IntArraySpConvert<SpSaver, Sp, Ed>(key, expireDurationInSecond)
            else {
                defaultNonnullInstance ?: IntArraySpConvert<SpSaver, Sp, Ed>(
                    key, expireDurationInSecond, defaultValue
                ).also { defaultNonnullInstance = it }
            } as AbsSpSaver.Delegate<SpSaver, IntArray>
        }

        operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor> invoke(
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, IntArray?> {
            return if (!key.isNullOrEmpty() || expireDurationInSecond > 0) {
                IntArraySpConvert<SpSaver, Sp, Ed>(key, expireDurationInSecond)
            } else {
                @Suppress("UNCHECKED_CAST")
                defaultInstance as? IntArraySpConvert<SpSaver, Sp, Ed>
                    ?: IntArraySpConvert<SpSaver, Sp, Ed>().also { defaultInstance = it }
            }
        }
    }
}
