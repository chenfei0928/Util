package io.github.chenfei0928.bean

import java.util.*

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2022-03-24 13:30
 */
internal data class AssembleTaskInfo(
    val dimensionedFlavorName: String,
    val buildType: String,
) {
    val targetFlavorBuildTypeVariantName =
        dimensionedFlavorName + buildType.capitalize(Locale.ROOT)
}
