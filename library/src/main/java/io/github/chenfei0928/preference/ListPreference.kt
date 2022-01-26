/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-26 11:49
 */
package io.github.chenfei0928.preference

import android.content.Context
import androidx.preference.ListPreference
import io.github.chenfei0928.collection.mapToArray

inline fun <reified E> ListPreference.bindEnum() where E : Enum<E>, E : VisibleNamed {
    bindEnum(enumValues<E>())
}

fun <E> ListPreference.bindEnum(enumValues: Array<E>) where E : Enum<E>, E : VisibleNamed {
    entries = enumValues.mapToArray { it.getVisibleName(context) }
    entryValues = enumValues.mapToArray { it.name }
}

interface VisibleNamed {
    fun getVisibleName(context: Context): CharSequence
}
