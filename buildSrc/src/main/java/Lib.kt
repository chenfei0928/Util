import org.gradle.api.Project

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-07-27 18:12
 */
fun Project.applyLib(appendBuildConfig: Boolean = true) {
    applyCommon(appendBuildConfig = appendBuildConfig)

    buildSrcAndroid<com.android.build.gradle.LibraryExtension> {
        defaultConfig {
            minSdk = 15
            targetSdk = Env.targetSdkVersion

            consumerProguardFile("consumer-rules.pro")
        }
    }
}
