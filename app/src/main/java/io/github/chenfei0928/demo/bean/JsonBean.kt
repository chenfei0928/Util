package io.github.chenfei0928.demo.bean

import android.content.Context
import android.os.Parcelable
import io.github.chenfei0928.preference.base.VisibleNamed
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * 貌似在 R8FullMode 时会缓存 copy 方法会缓存失败，所以把它们丢到了bean包下，避免被混淆
 *
 * @author chenf()
 * @date 2024-12-11 17:25
 */
@Serializable
@Parcelize
data class JsonBean(
    var int: Int = 0,
    @Transient
    @IgnoredOnParcel
    val i1: Int = 1,
    var long: Long = 0,
    val float: Float = 0f,
    var boolean: Boolean = false,
    val string: String = "",
    val inner: InnerJsonBean = InnerJsonBean(),
    val enum: JsonEnum = JsonEnum.DEFAULT,
    val enums: Set<JsonEnum> = emptySet(),
) : Parcelable {

    @Serializable
    @Parcelize
    data class InnerJsonBean(
        var boolean: Boolean = false,
    ) : Parcelable

    enum class JsonEnum : VisibleNamed {
        DEFAULT, ENUM_A, ENUM_B, ENUM_C;

        override fun getVisibleName(context: Context): CharSequence {
            return this.name
        }
    }
}
