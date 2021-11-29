import org.gradle.api.Project

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-07-30 17:55
 */
fun Project.applyNetApiUrl() {
    buildSrcAndroid<com.android.build.gradle.BaseExtension> {
        buildTypes {
            fun com.android.build.api.dsl.BuildType.buildConfigFields(
                webSocketUrl: String,
                oldApiUrl: String,
                mobileWebUrl: String,

                dynamicConfig: Boolean,
                launcherActivityEnable: Boolean,
                guideActivityEveryTime: Boolean,
                bannerVideoAutoPlay: Boolean,
                sdkAdEnable: Boolean,
                leakCanaryEnable: Boolean,

                lessAnim: Boolean
            ) {
                buildConfigField("String", "webSocketUrl", "\"$webSocketUrl\"")
                buildConfigField("String", "oldApiUrl", "\"$oldApiUrl\"")
                buildConfigField("String", "mobileWebUrl", "\"$mobileWebUrl\"")

                buildConfigField("boolean", "dynamicConfig", "$dynamicConfig")
                buildConfigField("boolean", "launcherActivityEnable", "$launcherActivityEnable")
                buildConfigField("boolean", "guideActivityEveryTime", "$guideActivityEveryTime")
                buildConfigField("boolean", "bannerVideoAutoPlay", "$bannerVideoAutoPlay")
                buildConfigField("boolean", "sdkAdEnable", "$sdkAdEnable")
                buildConfigField("boolean", "leakCanaryEnable", "$leakCanaryEnable")

                buildConfigField("boolean", "lessAnim", "$lessAnim")
            }

            getByName("release").buildConfigFields(
                webSocketUrl = "wss://wss.yiketalks.com:8282",
                oldApiUrl = "https://api.yiketalks.com/",
                mobileWebUrl = "https://m.yiketalks.com/",

                dynamicConfig = false,
                launcherActivityEnable = true,
                guideActivityEveryTime = false,
                bannerVideoAutoPlay = true,
                sdkAdEnable = true,
                leakCanaryEnable = false,

                lessAnim = false,
            )
            getByName("abtest").buildConfigFields(
                webSocketUrl = "wss://wss.yiketalks.net:8282",
                oldApiUrl = "http://dev-api.yiketalks.net/",
                mobileWebUrl = "http://huidu.yiketalks.com/",

                dynamicConfig = false,
                launcherActivityEnable = true,
                guideActivityEveryTime = false,
                bannerVideoAutoPlay = true,
                sdkAdEnable = false,
                leakCanaryEnable = false,

                lessAnim = false,
            )
            getByName("debug").buildConfigFields(
                webSocketUrl = "wss://wss.yiketalks.net:8282",
                oldApiUrl = "http://dev-api.yiketalks.net/",
                mobileWebUrl = "https://m.yiketalks.net/",

                dynamicConfig = true,
                launcherActivityEnable = false,
                guideActivityEveryTime = false,
                bannerVideoAutoPlay = false,
                sdkAdEnable = false,
                leakCanaryEnable = true,

                lessAnim = true,
            )
        }
    }
}
