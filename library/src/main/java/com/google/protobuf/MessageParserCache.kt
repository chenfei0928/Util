package com.google.protobuf

import androidx.collection.ArrayMap

/**
 * @author chenf()
 * @date 2025-02-28 11:37
 */
object MessageParserCache {
    private val cache = ArrayMap<String, Parser<out MessageLite>>()

    @Suppress("UNCHECKED_CAST")
    fun <M : MessageLite> getParser(className: String): Parser<M> = cache.getOrPut(className) {
        @Suppress("UNCHECKED_CAST")
        val messageType = Class.forName(className) as Class<out MessageLite>
        messageType.protobufParserForType
    } as Parser<M>
}
