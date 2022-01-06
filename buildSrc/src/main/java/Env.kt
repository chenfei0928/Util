import org.gradle.StartParameter
import org.gradle.TaskExecutionRequest
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.jetbrains.kotlin.gradle.utils.loadPropertyFromResources
import org.joor.Reflect

object Env {
    internal val logger: Logger = Logging.getLogger(this::class.java)
    private var impl = EnvImpl(false)

    fun reload(startParam: StartParameter) {
        // 从启动任务中寻找是否含有Release/QaTest编译的task
        val startTaskRequests: List<TaskExecutionRequest> = startParam.taskRequests
        containsReleaseBuild = startTaskRequests.flatMap { it.args as List<String> }.find {
            it.contains("Qatest") || it.contains("Release")
        } != null
        // 创建impl
        impl = EnvImpl(containsReleaseBuild)
    }

    internal var containsReleaseBuild: Boolean = false
        private set

    internal val agp: String
        get() = impl.agp
    internal val vcsCommitId: String
        get() = impl.vcsCommitId
    internal val vcsVersionCode: Int
        get() = impl.vcsVersionCode

    internal const val targetSdkVersion = 29
    internal const val compileSdkVersion = 31

    private class EnvImpl(
        containsReleaseBuild: Boolean
    ) {
        /**
         * [com.android.Version.ANDROID_GRADLE_PLUGIN_VERSION]
         * [com.android.build.gradle.internal.profile.AnalyticsUtil.getProductDetails().version]
         * 来自 com.android.tools:common 依赖定义
         */
        val agp: String = Reflect.onClass("com.android.Version")
            .field("ANDROID_GRADLE_PLUGIN_VERSION")
            .get()

        val vcsCommitId: String by lazy {
            // 不是Release编译不更新版本号
//            if (!containsReleaseBuild) return@lazy "-"
            val l = System.currentTimeMillis()
            // 以commit数量从大到小排序
            val commitId = Runtime.getRuntime()
                .exec("git rev-parse --short HEAD")
                .readTrimmedText()
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
                    .readTrimmedText()
                    .toInt()
            } catch (e: Throwable) {
                logger.error("vcsVersionCode: ", e)
                0
            }
            logger.quiet("VCS Version Code: ${ver}, time cost ${System.currentTimeMillis() - l} ms.")
            ver
        }

        private fun Process.readTrimmedText(): String {
            return inputStream.reader().readText().trim()
        }
    }
}
