plugins {
    `kotlin-dsl`
}

dependencies {
    // https://github.com/JetBrains/kotlin
    val kotlinVersion = "1.5.31"
    // AndroidGradlePlugin版本，建议与IDE版本同步
    // https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/maven-metadata.xml
    val agpVersion = "7.2.0-alpha06"

    implementation(kotlin("stdlib-jdk7", kotlinVersion))
    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation(kotlin("gradle-plugin-api", kotlinVersion))
    implementation(kotlin("android-extensions", kotlinVersion))
    implementation(gradleApi())

    implementation(localGroovy())

    // https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/maven-metadata.xml
    implementation("com.android.tools.build:gradle:$agpVersion")
    implementation("com.android.tools.build:gradle-api:$agpVersion")

    // 代码工具类库插件
    // 反射
    // https://github.com/jOOQ/jOOR
    implementation("org.jooq:joor:0.9.13")

    // 参与编译打包流程，但不参与代码编写
    // 增加 protobuf-gradle-plugin 插件
    // https://github.com/google/protobuf-gradle-plugin
    implementation("com.google.protobuf:protobuf-gradle-plugin:0.8.18")

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
}
