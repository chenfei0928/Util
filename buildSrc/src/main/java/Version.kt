import org.gradle.api.Project
import java.util.*

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-07-30 16:22
 */
internal fun Project.applyVersion() {
    buildSrcAndroid<com.android.build.gradle.BaseExtension> {
        defaultConfig {
            versionCode = Env.vcsVersionCode + 640000
            // 将vcs的版本号填充到应用内标记中
            manifestPlaceholders["buildDate"] = Date().toString()
            manifestPlaceholders["vcsCommitId"] = Env.vcsCommitId
        }
    }
}
