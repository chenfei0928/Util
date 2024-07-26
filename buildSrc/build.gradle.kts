plugins {
    java
    `kotlin-dsl`
}

tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin") {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + listOf("-Xskip-metadata-version-check")
    }
}

dependencies {
    // https://github.com/JetBrains/kotlin
    val kotlinVersion = "2.0.0"
    // AndroidGradlePlugin版本，建议与IDE版本同步
    // https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/maven-metadata.xml
    val agpVersion = "8.5.1"

    implementation(kotlin("stdlib-jdk7", kotlinVersion))
    implementation(kotlin("reflect", kotlinVersion))
    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation(kotlin("gradle-plugin-api", kotlinVersion))
    implementation(kotlin("android-extensions", kotlinVersion))
    implementation(kotlin("serialization", kotlinVersion))
    implementation(kotlin("compose-compiler-plugin", kotlinVersion))
    implementation(kotlin("compose-compiler-plugin-embeddable", kotlinVersion))
    // Kotlin Symbol Processing 符号处理器编译器插件，需伴随Kotlin版本一同升级
    // https://github.com/google/ksp
    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:$kotlinVersion-1.0.23")
    implementation(gradleApi())

    implementation(localGroovy())

    // https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/maven-metadata.xml
    implementation("com.android.tools.build:gradle:$agpVersion")
    implementation("com.android.tools.build:gradle-api:$agpVersion")

    // 代码工具类库插件
    // GreenDao数据库
    // https://github.com/greenrobot/greenDAO
    implementation("org.greenrobot:greendao-gradle-plugin:3.3.0")
    // 反射
    // https://github.com/jOOQ/jOOR
    implementation("org.jooq:joor:0.9.15")
    // ErrorProne
    // https://github.com/tbroyer/gradle-errorprone-plugin
    // https://github.com/google/error-prone
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:4.0.1")
    // ARouter
    // https://github.com/alibaba/ARouter/blob/master/README_CN.md
    // https://github.com/jadepeakpoet/ARouter
    implementation("com.github.jadepeakpoet.ARouter:arouter-register:1.0.3")
    // https://github.com/didi/DRouter#%E4%BD%BF%E7%94%A8%E6%96%87%E6%A1%A3
    implementation("io.github.didi:drouter-plugin:1.4.0")

    // 参与编译打包流程，但不参与代码编写
    // https://github.com/iwhys/sdk-editor-plugin
    implementation("com.github.iwhys:sdk-editor-plugin:1.1.7")
    // 字符串混淆工具，用于加密应用内appKey
    // https://github.com/MichaelRocks/paranoid
    implementation("io.michaelrocks:paranoid-gradle-plugin:0.3.7")
    // 使用 mavenLocal aar 加速编译
    // https://github.com/trycatchx/RocketXPlugin
    implementation("io.github.trycatchx:rocketx:1.1.1")
    // DEX控制流混淆
    // https://github.com/CodingGay/BlackObfuscator-ASPlugin
    implementation("com.github.CodingGay:BlackObfuscator-ASPlugin:3.9")
    // 增加 protobuf-gradle-plugin 插件
    // https://github.com/google/protobuf-gradle-plugin
    implementation("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
    // 美团打包工具
    // https://github.com/Meituan-Dianping/walle
    implementation("com.meituan.android.walle:plugin:1.1.7")
    // https://github.com/google/guava
    implementation("com.google.guava:guava:33.2.1-jre")
    // 腾讯Bugly混淆表上传工具
    // https://bugly.qq.com/docs/utility-tools/plugin-gradle-bugly/
    // https://repo1.maven.org/maven2/com/tencent/bugly/symtabfileuploader/
//    implementation("com.tencent.bugly:symtabfileuploader:2.2.1")
    // 腾讯补丁包工具
    // https://github.com/Tencent/tinker
    implementation("com.tencent.tinker:tinker-patch-gradle-plugin:1.9.14.26.3")
    implementation("com.tencent.tinker:tinker-patch-lib:1.9.14.26.3") {
        exclude(group = "com.google.guava")
    }
    // 操作系统和硬件架构判断工具，tinker依赖
    // https://github.com/google/osdetector-gradle-plugin
    implementation("com.google.gradle:osdetector-gradle-plugin:1.7.3")

    // https://github.com/bintray/gradle-bintray-plugin
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
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
    mavenCentral()
    google()
    gradlePluginPortal()
    // DEX控制流混淆、SdkEditor
    maven("https://jitpack.io") {
        name = "jetpack"
    }
}
