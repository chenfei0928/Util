@file:JvmName("TypesCf0928Util")

package androidx.viewpager2.adapter

import com.drakeet.multitype.Type
import com.drakeet.multitype.Types

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2023-03-13 16:21
 */
inline fun Types.forEach(block: (Type<*>) -> Unit) {
    for (i in 0 until size) {
        block(getType<Any>(i))
    }
}
