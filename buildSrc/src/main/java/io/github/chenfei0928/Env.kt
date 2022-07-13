package io.github.chenfei0928

import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.joor.Reflect
import java.util.*

object Env {
    internal val logger: Logger = Logging.getLogger(this::class.java)

    private lateinit var impl: EnvImpl

    /**
     * 当编译/工程载入时调用，查找启动任务参数中是否有需要混淆的task，并在后续流程中处理。
     * buildSrc中创建的有关编译的task，需要在task名中包含其flavors和buildType名。
     *
     * @param gradle Gradle环境实例
     */
    fun reload(gradle: Gradle) {
        // 从启动任务中寻找是否含有Release/QaTest编译的task
        val containsReleaseBuild: Boolean = gradle.startParameter.taskRequests
            .flatMap { it.args }
            .find { startTaskRequestArg ->
                Contract.minifyBuildTypes.find { minifyBuildType ->
                    startTaskRequestArg.contains(minifyBuildType.capitalize(Locale.ROOT))
                } != null
            } != null
        // 创建impl
        impl = EnvImpl(containsReleaseBuild)
        logger.lifecycle("Env环境初始化：$containsReleaseBuild")
    }

    /**
     * 此次存在正式编译，或非开发目标编译（如qaTest）
     * 开发阶段编译使用debug编译
     */
    internal val containsReleaseBuild: Boolean
        get() = impl.containsReleaseBuild
    internal val agpVersion: String
        get() = impl.agpVersion
    internal val vcsCommitId: String
        get() = impl.vcsCommitId
    internal val vcsVersionCode: Int
        get() = impl.vcsVersionCode

    //<editor-fold defaultstate="collapsed" desc="读取工程目录信息和运行时信息的实现，以便使用">
    private class EnvImpl(
        val containsReleaseBuild: Boolean
    ) {

        /**
         * [com.android.Version.ANDROID_GRADLE_PLUGIN_VERSION]
         * [com.android.build.gradle.internal.profile.AnalyticsUtil.getProductDetails().version]
         * 来自 com.android.tools:common 依赖定义
         */
        val agpVersion: String = Reflect.onClass("com.android.Version")
            .field("ANDROID_GRADLE_PLUGIN_VERSION")
            .get()

        val vcsCommitId: String by lazy {
            // 不是Release编译不更新版本号
            if (!containsReleaseBuild) return@lazy "-"
            val l = System.currentTimeMillis()
            // 以commit数量从大到小排序
            val commitId = Runtime.getRuntime()
                .exec("git rev-parse --short HEAD")
                .readText()
                .trim()
            logger.quiet("VCS Commit Id: ${commitId}, time cost ${System.currentTimeMillis() - l} ms.")
            commitId
        }

        val vcsVersionCode: Int by lazy {
            // 不是Release编译不更新版本号
            if (!containsReleaseBuild) return@lazy 100000
            val l = System.currentTimeMillis()
            // 以commit数量从大到小排序
            val ver = try {
                Runtime
                    .getRuntime()
                    .exec("git rev-list HEAD --count")
                    .readText()
                    .trim()
                    .toInt()
            } catch (e: Throwable) {
                logger.error("vcsVersionCode: ", e)
                0
            }
            logger.quiet("VCS Version Code: ${ver}, time cost ${System.currentTimeMillis() - l} ms.")
            ver
        }

        private fun Process.readText(): String {
            return inputStream.reader().readText()
        }
    }
    //</editor-fold>
}
