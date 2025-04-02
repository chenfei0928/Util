package io.github.chenfei0928.demo.storage

import android.content.Context
import io.github.chenfei0928.demo.bean.JsonBean
import io.github.chenfei0928.preference.LocalStorageFieldAccessorCache
import io.github.chenfei0928.repository.local.LocalFileStorage
import io.github.chenfei0928.repository.local.serializer.KtxsJsonSerializer

/**
 * @author chenf()
 * @date 2024-12-31 14:42
 */
object JsonLocalFileStorage : LocalFileStorage<JsonBean>(
    serializer = KtxsJsonSerializer(JsonBean()),
    fileName = "json.json",
) {
    fun storage(context: Context): LocalStorageFieldAccessorCache.Storage<JsonBean> =
        object : LocalStorageFieldAccessorCache.Storage<JsonBean> {
            override fun get(): JsonBean {
                return getCacheOrLoad(context)
            }

            override fun set(value: JsonBean) {
                write(context, value, false)
            }
        }
}
