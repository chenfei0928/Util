package com.chenfei.util.sp

import android.content.Context
import android.content.SharedPreferences

/**
 * 提供[SharedPreferences]保存、编辑器获取的实现
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-14 16:58
 */
open class BaseSpSaver(
    override val sp: SharedPreferences
) : AbsSpSaver() {
    private var spAutoApply: SpSaverAutoApply? = null

    constructor(
        context: Context, name: String, mode: Int = Context.MODE_PRIVATE
    ) : this(context.getSharedPreferences(name, mode))

    @Volatile
    private var _editor: SharedPreferences.Editor? = null
    override val editor: SharedPreferences.Editor
        get() {
            return _editor ?: sp
                .edit()
                .also {
                    _editor = it
                    spAutoApply?.autoSave()
                }
        }

    fun setEnableAutoApply(enable: Boolean) {
        spAutoApply = if (enable) {
            SpSaverAutoApply(this)
        } else {
            null
        }
    }

    override fun getSpAll(): Map<String, *> = sp.all

    override fun clear() {
        editor.clear()
    }

    override fun commit(): Boolean {
        val result = _editor?.commit() ?: false
        _editor = null
        return result
    }

    override fun apply() {
        _editor?.apply()
        _editor = null
    }
}
