package io.github.chenfei0928.content.sp.saver

import android.content.Context
import android.content.SharedPreferences
import io.github.chenfei0928.concurrent.updateAndGetCompat
import java.util.concurrent.atomic.AtomicReference

/**
 * 提供[SharedPreferences]保存、编辑器获取的实现
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-07-14 16:58
 */
open class BaseSpSaver<SpSaver : BaseSpSaver<SpSaver>>(
    final override val sp: SharedPreferences,
) : AbsSpSaver<SpSaver, SharedPreferences, SharedPreferences.Editor>(false) {
    private var spAutoApply: SpSaverAutoApply? = null

    constructor(
        context: Context, name: String, mode: Int = Context.MODE_PRIVATE,
    ) : this(context.getSharedPreferences(name, mode))

    private val editorAtomicReference = AtomicReference<SharedPreferences.Editor?>()

    @get:Synchronized
    final override val editor: SharedPreferences.Editor
        get() = editorAtomicReference.get() ?: (editorAtomicReference.updateAndGetCompat {
            it ?: sp.edit()
        }!!.apply {
            spAutoApply?.autoSave()
        })

    fun setEnableAutoApply(enable: Boolean) {
        spAutoApply = if (enable) {
            SpSaverAutoApply(this)
        } else {
            null
        }
    }

    final override fun getSpAll(): Map<String, *> = sp.all

    final override fun clear() {
        editor.clear()
    }

    @Synchronized
    final override fun commit(): Boolean {
        val editor = editorAtomicReference.get()
            ?: return false
        val result = editor.commit()
        val compareAndSet = editorAtomicReference.compareAndSet(editor, null)
        return result && compareAndSet
    }

    @Synchronized
    final override fun apply() {
        val editor = editorAtomicReference.get()
            ?: return
        editor.apply()
        editorAtomicReference.compareAndSet(editor, null)
    }
}
