package io.github.chenfei0928.bean

import io.github.chenfei0928.util.replaceFirstCharToUppercase


/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2022-03-24 13:30
 */
internal data class TaskInfo(
    val dimensionFlavorNames: List<Pair<String, String?>>,
    /**
     * 当前task的flavor名或空字符串（全flavor编译）
     */
    val dimensionedFlavorName: String,
    /**
     * 编译类型
     */
    val buildType: String,
    /**
     * 首字母大写的变体名
     */
    val dimensionedFlavorBuildTypeName: String,
) {
    val targetFlavorBuildTypeVariantName =
        dimensionedFlavorName + buildType.replaceFirstCharToUppercase()
}
