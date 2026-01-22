package io.github.chenfei0928.preference.base

import android.content.Context

/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-26 11:49
 */
fun interface VisibleNamed {
    fun getVisibleName(context: Context): CharSequence
}
