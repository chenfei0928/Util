plugins {
    id("application")
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
}

application {
    mainClass.set("io.github0928.script.MyClass")
}

dependencies {
    // https://git-scm.com/book/zh/v2/%E9%99%84%E5%BD%95-B%3A-%E5%9C%A8%E4%BD%A0%E7%9A%84%E5%BA%94%E7%94%A8%E4%B8%AD%E5%B5%8C%E5%85%A5-Git-JGit
    // https://mvnrepository.com/artifact/org.eclipse.jgit/org.eclipse.jgit
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.0.0.202409031743-r")

    // https://github.com/Kotlin/kotlinx.coroutines
    val coroutinesVersion = "1.8.1"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    // https://github.com/ajalt/clikt
    implementation("com.github.ajalt.clikt:clikt:5.0.1")
    implementation("com.github.ajalt.clikt:clikt-markdown:5.0.1")
}
