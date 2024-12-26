package io.github.chenfei0928.repository

import android.content.Context
import androidx.ads.identifier.AdvertisingIdClient
import io.github.chenfei0928.os.Debug
import io.github.chenfei0928.repository.local.LocalFileStorage
import io.github.chenfei0928.repository.local.serializer.StringSerializer
import io.github.chenfei0928.util.Log
import java.util.UUID
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * 获取应用安装时安卓系统自动分配的UUID的工具类，支持自动保存。
 * 但是并没有打算去用这个玩意，因为应用重新安装之后会导致UUID被更新（UUID唯一，但是不保证对设备的唯一）
 * 之前使用获取网卡MAC地址的方式在6.0上莫名出现了无法正确获取得问题
 * 为了审核合规，在此处不再获取AndroidId，而使用UUID，此值将会在重装应用后被更新
 *
 * @author chenf()
 * @date 2024-08-01 14:15
 */
object Installation : LocalFileStorage<String>(
    StringSerializer(), "INSTALLATION", false, true
) {
    private const val TAG = "KW_Installation"

    fun get(context: Context): String {
        return getCacheOrLoad(context).takeIf { it.isNotEmpty() }
            ?: getAdId(context).also { write(context, it, true) }
    }

    private fun getAdId(context: Context): String {
        // 优先获取广告id
        try {
            if (AdvertisingIdClient.isAdvertisingIdProviderAvailable(context)) {
                val debugMsg = "getAdId: "
                try {
                    return Debug.countTime(TAG, debugMsg) {
                        AdvertisingIdClient.getAdvertisingIdInfo(context)[300L, TimeUnit.MILLISECONDS].id
                    }
                } catch (e: ExecutionException) {
                    Log.e(TAG, debugMsg, e)
                } catch (e: InterruptedException) {
                    Log.e(TAG, debugMsg, e)
                } catch (e: TimeoutException) {
                    Log.e(TAG, debugMsg, e)
                }
            } else {
                Log.d(TAG, "getAdId: AdvertisingId 不可用")
            }
        } catch (_: Throwable) {
            Log.d(TAG, "getAdId: 不使用 Google AdvertisingId 库")
        }
        // 如果获取到的是模拟器ID，使用生成的随机id
        return UUID.randomUUID().toString()
    }
}
