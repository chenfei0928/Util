package io.github.chenfei0928.content.sp.saver

import com.tencent.mmkv.MMKV

/**
 * [MMKV] 的结构化key-value存储支持类
 *
 * 由于其创建不需要 [android.content.Context] 参数，所以子类可以安全的创建为单例类使用
 *
 * @author chenf()
 * @date 2025-02-10 17:53
 */
abstract class BaseMmkvSaver<SpSaver : BaseMmkvSaver<SpSaver>>(
    mmkv: MMKV,
) : AbsSpSaver<SpSaver, MMKV, MMKV>() {
    override val sp: MMKV = mmkv
    override val editor: MMKV = mmkv

    constructor(
        mmapID: String,
        mode: Int = MMKV.SINGLE_PROCESS_MODE,
        cryptKey: String? = null,
        rootPath: String? = null,
        expectedCapacity: Long = MMKV.ExpireNever.toLong()
    ) : this(MMKV.mmkvWithID(mmapID, mode, cryptKey, rootPath, expectedCapacity))

    override fun getSpAll(): Map<String, *> {
        return dataStore.spSaverPropertyDelegateFields.associate {
            @Suppress("UNCHECKED_CAST")
            it.pdsKey to it.get(this as SpSaver)
        }
    }

    override fun clear() {
        editor.clear()
    }

    override fun commit(): Boolean {
        return true
    }

    override fun apply() {
        // noop
    }
}
