package io.github.chenfei0928.repository.local.decorator

import android.os.Build
import androidx.annotation.RequiresApi
import io.github.chenfei0928.repository.local.LocalSerializer
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * 超时保质期序列化
 *
 * @author chenfei(chenfei@gmail.com)
 * @date 2022-01-12 10:25
 */
class ExpirationDateSerializer<T : Any>(
    serializer: LocalSerializer<T>,
    private val timeoutInMillis: Long
) : BaseExpirationDateSerializer<T>(serializer) {
    constructor(
        serializer: LocalSerializer<T>, duration: Long, timeUnit: TimeUnit
    ) : this(serializer, timeUnit.toMillis(duration))

    @RequiresApi(Build.VERSION_CODES.O)
    constructor(
        serializer: LocalSerializer<T>, duration: Duration
    ) : this(serializer, duration.toMillis())

    override val generateVersionCode: Long
        get() = System.currentTimeMillis()

    override fun checkOrThrow(localSavedTimeMillis: Long) {
        val currentTimeMillis = System.currentTimeMillis()
        val localSavedTime = SimpleDateFormat.getDateInstance().format(localSavedTimeMillis)
        require(localSavedTimeMillis + timeoutInMillis >= currentTimeMillis) {
            // 抛出异常，通知对数据进行反序列化失败，交由调用处LocalFileModule删除缓存文件
            "local file's time is $localSavedTime, Data has expired.\n" +
                    "本地文件的标记时间是$localSavedTime，数据已过期"
        }
    }
}
