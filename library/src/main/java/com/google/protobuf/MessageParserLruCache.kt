package com.google.protobuf

import androidx.collection.LruCache

/**
 * @author chenf()
 * @date 2025-02-28 11:37
 */
object MessageParserLruCache : LruCache<String, Parser<out MessageLite>>(64) {
    override fun create(key: String): Parser<out MessageLite> {
        @Suppress("UNCHECKED_CAST")
        val messageType = Class.forName(key) as Class<out MessageLite>
        return messageType.protobufParserForType
    }

    @Suppress("UNCHECKED_CAST")
    fun <M : MessageLite> getParser(className: String): Parser<M> =
        get(className) as Parser<M>
}
