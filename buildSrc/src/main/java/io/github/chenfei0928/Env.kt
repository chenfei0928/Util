package io.github.chenfei0928

import io.github.chenfei0928.data.ProtobufType
import io.github.chenfei0928.util.RuntimeExecProperty
import io.github.chenfei0928.util.replaceFirstCharToUppercase
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.joor.Reflect
import java.time.Instant
import java.util.Date

object Env {
    internal val logger: Logger = Logging.getLogger(this::class.java)

    private lateinit var impl: EnvImpl

    /**
     * 当编译/工程载入时调用，查找启动任务参数中是否有需要混淆的task，并在后续流程中处理。
     * buildSrc中创建的有关编译的task，需要在task名中包含其flavors和buildType名。
     *
     * @param gradle Gradle环境实例
     * @param protobufType 工程中所用protobuf编译目标类型
     */
    fun reload(gradle: Gradle, protobufType: ProtobufType) {
        // 从启动任务中寻找是否含有Release/QaTest编译的task
        val containsReleaseBuild: Boolean = gradle.startParameter.taskRequests
            .flatMap { it.args }
            .any { startTaskRequestArg ->
                Contract.minifyBuildTypes.any { minifyBuildType ->
                    startTaskRequestArg.contains(minifyBuildType.replaceFirstCharToUppercase())
                }
            }
        // 创建impl
        impl = EnvImpl(containsReleaseBuild, protobufType)
        logger.lifecycle("Env环境初始化：$containsReleaseBuild, ${Instant.now()}")
    }

    val isWindows: Boolean = System.getProperty("os.name").startsWith("Windows")

    /**
     * 此次存在正式编译，或非开发目标编译（如qaTest）
     * 开发阶段编译使用debug编译
     */
    internal val containsReleaseBuild: Boolean
        get() = impl.containsReleaseBuild
    internal val protobufType: ProtobufType
        get() = impl.protobufType
    internal val launchTimestamp: Date
        get() = impl.launchTimestamp
    internal val agpVersion: String
        get() = impl.agpVersion
    val vcsCommitId: String
        get() = impl.vcsCommitId
    internal val vcsVersionCode: Int
        get() = impl.vcsVersionCode
    internal val vscBranchName: String
        get() = impl.vscBranchName

    //<editor-fold defaultstate="collapsed" desc="读取工程目录信息和运行时信息的实现，以便使用">
    private class EnvImpl(
        val containsReleaseBuild: Boolean,
        val protobufType: ProtobufType,
    ) {
        val launchTimestamp = Date()

        /**
         * [com.android.Version.ANDROID_GRADLE_PLUGIN_VERSION]
         *
         * [com.android.build.gradle.internal.profile.AnalyticsUtil.getProductDetails().version]
         *
         * 来自 com.android.tools:common 依赖定义
         */
        val agpVersion: String by lazy {
            Reflect.onClass("com.android.Version")
                .field("ANDROID_GRADLE_PLUGIN_VERSION")
                .get()
        }
        val vcsCommitId: String by RuntimeExecProperty(
            "git rev-parse --short HEAD"
        )
        val vcsVersionCode: Int by RuntimeExecProperty(
            "git rev-list HEAD --count"
        )
        val vscBranchName: String by RuntimeExecProperty(
            "git symbolic-ref --short -q HEAD"
        )
    }
    //</editor-fold>
}
