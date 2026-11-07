plugins {
    java
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

group = "io.github.chenfei0928"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin") {
    compilerOptions {
        freeCompilerArgs.add("-Xskip-metadata-version-check")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

dependencies {
    // https://github.com/JetBrains/kotlin
    implementation(libs.kotlin.stdlib.jdk7)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.serialization.plugin)
    // org.jetbrains.kotlin.plugin.compose
    // https://plugins.gradle.org/plugin/org.jetbrains.kotlin.plugin.compose
    implementation(libs.compose.compiler.gradle.plugin)
    // Kotlin Symbol Processing 符号处理器编译器插件，需伴随Kotlin版本一同升级
    // https://github.com/google/ksp
    implementation(libs.ksp.symbol.processing.gradle.plugin)
    implementation(gradleApi())

    implementation(localGroovy())

    // AndroidGradlePlugin版本，建议与IDE版本同步
    // https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/maven-metadata.xml
    implementation(libs.agp.gradle)
    implementation(libs.agp.gradle.api)

    // 代码工具类库插件
    // GreenDao数据库
    // https://github.com/greenrobot/greenDAO
    implementation(libs.greendao.gradle.plugin)
    // 反射
    // https://github.com/jOOQ/jOOR
    implementation(libs.joor)
    // ErrorProne
    // https://github.com/tbroyer/gradle-errorprone-plugin
    // https://github.com/google/error-prone
    implementation(libs.gradle.errorprone.plugin)
    // ARouter
    // https://github.com/alibaba/ARouter/blob/master/README_CN.md
    // https://github.com/jadepeakpoet/ARouter
    implementation(libs.arouter.register)
    // https://github.com/didi/DRouter#%E4%BD%BF%E7%94%A8%E6%96%87%E6%A1%A3
    implementation(libs.drouter.plugin)

    // 参与编译打包流程，但不参与代码编写
    // https://github.com/iwhys/sdk-editor-plugin
    implementation(libs.sdk.editor.plugin)
    // 字符串混淆工具，用于加密应用内appKey
    // https://github.com/MichaelRocks/paranoid
    implementation(libs.paranoid.gradle.plugin)
    // 使用 mavenLocal aar 加速编译
    // https://github.com/trycatchx/RocketXPlugin
    implementation(libs.rocketx.plugin)
    // DEX控制流混淆
    // https://github.com/CodingGay/BlackObfuscator-ASPlugin
    implementation(libs.black.obfuscator.plugin)
    // 增加 protobuf-gradle-plugin 插件
    // https://github.com/google/protobuf-gradle-plugin
    implementation(libs.protobuf.gradle.plugin)
    // 美团打包工具
    // https://github.com/Meituan-Dianping/walle
    implementation(libs.walle.plugin)
    // https://github.com/google/guava
    implementation(libs.guava.jre)
    // https://github.com/didi/booster
    // https://juejin.cn/post/7504839725611728896
    implementation(libs.booster.gradle.plugin)
    // 腾讯Bugly混淆表上传工具
    // https://bugly.qq.com/docs/utility-tools/plugin-gradle-bugly/
    // https://repo1.maven.org/maven2/com/tencent/bugly/symtabfileuploader/
//    implementation("com.tencent.bugly:symtabfileuploader:2.2.1")
    // 腾讯补丁包工具
    // https://github.com/Tencent/tinker
    implementation(libs.tinker.patch.gradle.plugin)
    implementation(libs.tinker) {
        exclude(group = "com.google.guava")
    }
    // 操作系统和硬件架构判断工具，tinker依赖
    // https://github.com/google/osdetector-gradle-plugin
    implementation(libs.osdetector.gradle.plugin)
    // 360插件化
    // https://github.com/Qihoo360/RePlugin/blob/dev/README_CN.md
    implementation(libs.replugin.host.gradle)
    implementation(libs.replugin.plugin.gradle)
    // 腾讯Shadow插件化
    // https://github.com/Tencent/Shadow
    implementation(libs.shadow.plugin)

    // https://github.com/bintray/gradle-bintray-plugin
    implementation(libs.bintray.gradle.plugin)
}

repositories {
    // 阿里云镜像
    // https://help.aliyun.com/document_detail/102512.html
    maven("https://maven.aliyun.com/repository/central") {
        name = "aliMirrors-central"
    }
    maven("https://maven.aliyun.com/repository/gradle-plugin") {
        name = "aliMirrors-gradlePlugin"
    }
    maven("https://maven.aliyun.com/repository/google") {
        name = "aliMirrors-google"
    }
    // 原生镜像
    mavenLocal()
    mavenCentral()
    google()
    gradlePluginPortal()
    // DEX控制流混淆、SdkEditor
    maven("https://jitpack.io") {
        name = "jetpack"
    }
    // DidiBoost
    // OPTIONAL If you want to use SNAPSHOT version, sonatype repository is required.
    maven("https://oss.sonatype.org/content/repositories/public") {
        name = "sonatype"
    }
    exclusiveContent {
        forRepository {
            // 360 RePlugin 仓库
            maven("http://maven.geelib.360.cn/nexus/repository/replugin/") {
                isAllowInsecureProtocol = true
                name = "360"
            }
        }
        filter {
//            includeGroup("com.qihoo360.replugin")
            includeModule("com.qihoo360.replugin", "replugin-host-gradle")
        }
    }
}