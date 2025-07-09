package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import androidx.annotation.IntRange
import androidx.collection.ArraySet
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.util.DependencyChecker

class StringSetDelegate<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor>
private constructor(
    key: String? = null,
    private val defaultEntry: String? = null,
    defaultValue: Set<String?>? = null,
    @all:IntRange(from = 0) private val expireDurationInSecond: Int = MMKV.ExpireNever,
) : AbsSpAccessDefaultValueDelegate<SpSaver, Sp, Ed, Set<String?>?>(
    key, PreferenceType.Native.STRING_SET as PreferenceType<Set<String?>>, defaultValue
) {
    override fun getValueImpl(sp: Sp, key: String): Set<String?> = if (defaultEntry == null) {
        sp.getStringSet(key, defaultValue)!!
    } else sp.getStringSet(key, defaultValue)!!.let {
        it.mapTo(ArraySet(it.size)) { e -> e ?: defaultEntry }
    }

    override fun putValue(editor: Ed, key: String, value: Set<String?>) {
        if (expireDurationInSecond <= MMKV.ExpireNever || !DependencyChecker.mmkv || editor !is MMKV) {
            editor.putStringSet(key, value)
        } else {
            editor.putStringSet(key, value, expireDurationInSecond)
        }
    }

    companion object {
        private var defaultInstance: StringSetDelegate<*, *, *>? = null
        private var defaultNonnullInstance: StringSetDelegate<*, *, *>? = null

        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor> nonNull(
            defaultValue: Set<String> = emptySet(),
            defaultEntry: String = "",
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, Set<String>> {
            @Suppress("UNCHECKED_CAST", "ComplexCondition")
            return if (defaultValue.isNotEmpty()
                || defaultEntry.isNotEmpty()
                || !key.isNullOrEmpty()
                || expireDurationInSecond > 0
            ) {
                StringSetDelegate<SpSaver, Sp, Ed>(
                    key, defaultEntry, defaultValue, expireDurationInSecond,
                )
            } else {
                defaultNonnullInstance ?: StringSetDelegate<SpSaver, Sp, Ed>(
                    key, defaultEntry, defaultValue, expireDurationInSecond,
                ).also { defaultNonnullInstance = it }
            } as AbsSpSaver.Delegate<SpSaver, Set<String>>
        }

        operator fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor> invoke(
            key: String? = null,
            @IntRange(from = 0) expireDurationInSecond: Int = MMKV.ExpireNever,
        ): AbsSpSaver.Delegate<SpSaver, Set<String?>?> {
            return if (!key.isNullOrEmpty() || expireDurationInSecond > 0) {
                StringSetDelegate(key, null, null, expireDurationInSecond)
            } else {
                @Suppress("UNCHECKED_CAST")
                defaultInstance as? StringSetDelegate<SpSaver, Sp, Ed>
            } ?: StringSetDelegate<SpSaver, Sp, Ed>().also { defaultInstance = it }
        }
    }
}
