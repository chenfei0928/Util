package io.github.chenfei0928

import org.gradle.api.JavaVersion

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2022-07-05 19:01
 */
object Contract {
    //<editor-fold desc="AGP或BuildSrc中一些确定的约定">
    /**
     * 需要开启混淆的编译类型
     */
    internal val minifyBuildTypes = arrayOf("release", "prerelease", "qatest")

    /**
     * 编译输出目录名
     */
    internal const val outputs = "outputs"

    /**
     * 需要导入/应用的混淆映射表的父目录名
     * （不同的flavors/buildType以其 variantName 作为目录名）
     */
    internal const val mappingFileSaveDirName = "tinker"

    /**
     * 打入渠道的文件的输出文件夹名
     *
     * 渠道配置目录名，以flavor名创建文件，内配置要打入的渠道名列表
     */
    internal const val CHANNELS_APK_OUTPUT_DIR_NAME = "channels"

    /**
     * Apk打包任务名前缀，打包任务名命名规则为
     *
     * ```assemble[DimensionedFlavorName][BuildType]```
     */
    internal const val ASSEMBLE_TASK_PREFIX = "assemble"

    internal const val DIMENSIONED_FLAVOR_AND_BUILD_TYPE = "(\\w+)"

    /**
     * 混淆文件目录
     */
    internal const val PROGUARD_FILES_DIR = "proguard-rules"

    /**
     * [编译系统对应的JDK版本](https://developer.android.com/build/jdks?hl=zh-cn)
     */
    internal val JAVA_VERSION = JavaVersion.VERSION_17
    //</editor-fold>

    //<editor-fold desc="工程Module的sdkVersion等配置">
    internal const val minSdkVersion = 21
    internal const val targetSdkVersion = 35
    internal const val compileSdkVersion = 35

    const val flavorDimensions_campaignName = "campaignName"
    //</editor-fold>
}
