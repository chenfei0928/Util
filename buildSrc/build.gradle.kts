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
    val kotlinVersion = "1.9.20"
    // AndroidGradlePlugin版本，建议与IDE版本同步
    // https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/maven-metadata.xml
    val agpVersion = "8.2.0"

    implementation(kotlin("stdlib-jdk7", kotlinVersion))
    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation(kotlin("gradle-plugin-api", kotlinVersion))
    implementation(kotlin("android-extensions", kotlinVersion))
    // Kotlin Symbol Processing 符号处理器编译器插件，需伴随Kotlin版本一同升级
    // https://github.com/google/ksp
    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:$kotlinVersion-1.0.13")
    implementation(gradleApi())

    implementation(localGroovy())

    // https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/maven-metadata.xml
    implementation("com.android.tools.build:gradle:$agpVersion")
    implementation("com.android.tools.build:gradle-api:$agpVersion")

    // 代码工具类库插件
    // 反射
    // https://github.com/jOOQ/jOOR
    implementation("org.jooq:joor:0.9.14")
    // ErrorProne
    // https://github.com/tbroyer/gradle-errorprone-plugin
    // https://github.com/google/error-prone
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:3.1.0")
    // ARouter
    // https://github.com/alibaba/ARouter/blob/master/README_CN.md
    // https://github.com/jadepeakpoet/ARouter
    implementation("com.github.jadepeakpoet.ARouter:arouter-register:1.0.3")

    // 参与编译打包流程，但不参与代码编写
    // 增加 protobuf-gradle-plugin 插件
    // https://github.com/google/protobuf-gradle-plugin
    implementation("com.google.protobuf:protobuf-gradle-plugin:0.9.4")

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
