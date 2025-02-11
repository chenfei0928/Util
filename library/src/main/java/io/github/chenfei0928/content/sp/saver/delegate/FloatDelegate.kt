package io.github.chenfei0928.content.sp.saver.delegate

import android.content.SharedPreferences
import com.tencent.mmkv.MMKV
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.util.DependencyChecker

class FloatDelegate<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor>
constructor(
    key: String? = null, defaultValue: Float = 0f,
    private val expireDurationInSecond: Int = MMKV.ExpireNever,
) : AbsSpAccessDefaultValueDelegate<SpSaver, Sp, Ed, Float>(
    key, PreferenceType.Native.FLOAT, defaultValue
) {
    override fun getValueImpl(sp: Sp, key: String): Float =
        sp.getFloat(key, defaultValue)

    override fun putValue(editor: Ed, key: String, value: Float) {
        if (expireDurationInSecond <= MMKV.ExpireNever || !DependencyChecker.MMKV() || editor !is MMKV) {
            editor.putFloat(key, value)
        } else {
            editor.putFloat(key, value, expireDurationInSecond)
        }
    }
}
