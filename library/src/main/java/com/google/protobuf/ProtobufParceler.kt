package com.google.protobuf

import android.os.Parcel
import androidx.collection.LruCache
import kotlinx.parcelize.Parceler

/**
 * @author chenf()
 * @date 2023-03-28 14:27
 */
object ProtobufParceler :
    Parceler<GeneratedMessageV3?>
    by
    ProtobufV3ParcelerImpl::class.java.newInstance() as Parceler<GeneratedMessageV3?>

private class ProtobufV3ParcelerImpl<MessageType : GeneratedMessageV3> : Parceler<MessageType?> {

    private val parserCache = object :
        LruCache<String, Parser<MessageType>>(10) {

        override fun create(key: String): Parser<MessageType> {
            val messageType = Class.forName(key) as Class<MessageType>
            return messageType.getProtobufParserForType()
        }
    }

    override fun create(parcel: Parcel): MessageType? {
        val className = parcel.readString() ?: return null
        val parser = parserCache[className]!!
        return parcel.createByteArray().let(parser::parseFrom)
    }

    override fun MessageType?.write(parcel: Parcel, flags: Int) {
        if (this == null) {
            parcel.writeString(null)
        } else {
            parcel.writeString(this.javaClass.name)
            parcel.writeByteArray(this.toByteArray())
        }
    }
}
