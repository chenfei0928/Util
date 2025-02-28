package io.github.chenfei0928.repository.local.decorator

import io.github.chenfei0928.base.UtilInitializer
import io.github.chenfei0928.content.packageInfo
import io.github.chenfei0928.content.versionCodeLong
import io.github.chenfei0928.repository.local.LocalSerializer

/**
 * 弥补某些数据结构或序列化实现在跨代码版本序列化和反序列化时不兼容的问题
 * 进行版本校验，校验失败直接删除缓存文件
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-05 15:20
 */
class VersionedSerializer<T : Any>
private constructor(
    serializer: LocalSerializer<T>,
    versionCodeLong: Long,
) : BaseExpirationDateSerializer<T>(serializer) {
    override val generateVersionCode: Long = versionCodeLong

    override fun checkOrThrow(localSavedVersionCode: Long) {
        require(generateVersionCode == localSavedVersionCode) {
            // 抛出异常，通知对数据进行反序列化失败，交由调用处LocalFileModule删除缓存文件
            "current version is $generateVersionCode, local file's version is $localSavedVersionCode," +
                    " version code not match! The data structure may have been modified.\n" +
                    "当前版本是 $generateVersionCode, 本地文件的版本是 $localSavedVersionCode，版本不匹配！数据结构可能已经被修改"
        }
    }

    companion object {
        fun <T : Any> LocalSerializer<T>.versioned(
            versionCodeLong: Long = UtilInitializer.context.packageInfo.versionCodeLong
        ): LocalSerializer<T> = if (this is VersionedSerializer) {
            this
        } else {
            VersionedSerializer(this, versionCodeLong)
        }
    }
}
