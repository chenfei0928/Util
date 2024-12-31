package io.github.chenfei0928.demo.storage

import android.content.Context
import io.github.chenfei0928.preference.LocalStoragePreferenceDataStore
import io.github.chenfei0928.repository.local.LocalFileStorage0
import io.github.chenfei0928.repository.local.serializer.KtxsJsonSerializer
import io.github.chenfei0928.util.MapCache

/**
 * @author chenf()
 * @date 2024-12-31 14:47
 */
class JsonLocalFileStorage0
private constructor(
    context: Context
) : LocalFileStorage0<JsonBean>(
    context, "json0.json", KtxsJsonSerializer.Companion(JsonBean())
), LocalStoragePreferenceDataStore.Storage<JsonBean> {
    override fun get(): JsonBean {
        return getCacheOrLoad()
    }

    override fun set(value: JsonBean) {
        write(value, false)
    }

    companion object {
        val Context.jsonLocalStorage0: JsonLocalFileStorage0 by MapCache.Application<JsonLocalFileStorage0> {
            JsonLocalFileStorage0(it)
        }
    }
}
