import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import java.io.File

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-07-30 16:18
 */
fun Project.applyApp() {
    applyCommon()
    applyKotlin()
    applyVersion()
    applyTest()

    buildSrcAndroid<com.android.build.gradle.AppExtension> {
        signingConfigs {
            register("config") {
                storeFile =
                    File(project.rootDir, project.properties["RELEASE_STORE_FILE"].toString())
                storePassword = project.properties["RELEASE_STORE_PASSWORD"].toString()
                keyAlias = project.properties["RELEASE_KEY_ALIAS"].toString()
                keyPassword = project.properties["RELEASE_KEY_PASSWORD"].toString()
            }
            getByName("debug") {
                storeFile = File(File(project.rootDir, "gradle"), "debug.keystore")
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }

        defaultConfig {
            // 启用多Dex文件支持
            multiDexEnabled = true
            // 使用矢量图
            vectorDrawables.useSupportLibrary = true
            targetSdk = Env.targetSdkVersion
        }

        /**
         * 编译类型
         * {@link com.android.build.gradle.internal.dsl.BuildType}
         */
        buildTypes {
            // release正式包
            named("release") {
                // 签名
                signingConfig = signingConfigs.getByName("config")

                postprocessing {
                    // 启用自动过滤删除无用res资源文件，依赖于 isMinifyEnabled
                    // 但不清楚keep混淆R文件是否会影响该效果
                    isRemoveUnusedResources = false
                    // 代码混淆
                    isObfuscate = true
                    isOptimizeCode = true
                    isRemoveUnusedCode = true
                    // 混淆文件
                    proguardFile("proguard-rules.pro")
                    // 添加文件夹下的混淆配置文件
                    fileTree("proguard-rules").forEach { file ->
                        if (file.path.endsWith(".pro")) {
                            proguardFile(file)
                        }
                    }
                }
            }
            // abTest灰度测试包
            named("abtest") {
                isDebuggable = true
                // abTest灰度测试添加后缀
                applicationIdSuffix = ".abtest"
                // 签名
                signingConfig = signingConfigs.getByName("debug")
            }
            // debug测试运行、包
            named("debug") {
                isDebuggable = true
                // debug添加后缀
                applicationIdSuffix = ".debug"
                // 签名
                signingConfig = signingConfigs.getByName("debug")
            }
        }

        aaptOptions {
            // 禁用cruncher, 以加速编译
            cruncherEnabled = false
            cruncherProcesses = 0
        }

        compileOptions {
            // 开启增量编译
            incremental = true
        }

        // 如果出现META-INF重复的问题 - 友盟社会化登录、分享
        // 打包时排除一些文件
        packagingOptions {
            // support支持库
            resources.excludes.add("META-INF/*.version")
            resources.excludes.add("androidsupportmultidexversion.txt")
            // Anko
            resources.excludes.add("META-INF/*.kotlin_module")
            // 小米推送的额外文件
            resources.excludes.add("miui_push_version")
            resources.excludes.add("push_version")
            // 部分sdk的混淆文件
            resources.excludes.add("META-INF/proguard/**")
        }
    }

    // DI依赖注入、Debug调试工具、运行时权限处理依赖
    dependencies {
        implementation(DepsAndroidx.multidex.core)

        // RxJava 响应式编程
        implementation(Deps.rx.android)
        implementation(Deps.rx.core)
        // Rx生命周期，用于协调Activity生命周期变化取消订阅
        implementation(Deps.rx.lifecycle)

        // 网络
        implementation(Deps.network.okhttp)
        implementation(Deps.network.retrofit.core)
        implementation(Deps.network.retrofit.rxjava2)
        implementation(Deps.network.retrofit.gson)
        // Glide 图片加载库
        implementation(Deps.glide.core)
    }
}

fun Project.applyAppAfter() {
    buildSrcAndroid<com.android.build.gradle.AppExtension> {
        // Debug时禁用Multi APK
        if (!Env.containsReleaseBuild) {
            splits.abi.isEnable = false
            splits.density.isEnable = false
        }

        // Debug时不编译不必要的资源
        if (!Env.containsReleaseBuild) {
            productFlavors.forEach {
                it.resourceConfigurations.addAll(
                    arrayOf("zh", "zh-rCN", "xxhdpi")
                )
            }
        }
    }
}
