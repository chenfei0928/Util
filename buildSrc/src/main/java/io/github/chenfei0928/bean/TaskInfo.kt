package io.github.chenfei0928.bean

import com.android.build.api.dsl.ProductFlavor
import com.android.build.api.variant.ComponentIdentity
import com.android.build.gradle.AndroidConfig
import io.github.chenfei0928.util.replaceFirstCharToUppercase


/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2022-03-24 13:30
 */
internal data class TaskInfo(
    /**
     * 该编译任务对应的 [com.android.build.gradle.AppExtension.productFlavors] 配置。
     * 如果是全 flavor 编译，此处是 emptyList。
     * 如果其 flavor 可能会含有多个维度 [AndroidConfig.flavorDimensionList] 这里是一个list，
     * 其顺序根据 [AndroidConfig.flavorDimensionList] 的顺序排列。
     * listOf([ProductFlavor.dimension] to [ProductFlavor.getName])
     */
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
    val targetFlavorBuildTypeVariantName = if (dimensionedFlavorName.isEmpty())
        buildType else dimensionedFlavorName + buildType.replaceFirstCharToUppercase()

    fun isSameTo(variantInfo: ComponentIdentity): Boolean {
        return variantInfo.buildType == buildType && variantInfo.flavorName == dimensionedFlavorName
    }
}
