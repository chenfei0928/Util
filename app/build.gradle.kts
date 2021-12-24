plugins {
    id("com.android.application")
    `kotlin-android`
    `kotlin-parcelize`
}

applyApp()
applyTest()
applyKotlin()

android {
    defaultConfig {
        applicationId = "io.github.chenfei0928.util"
        minSdk = 14
        targetSdk = 30
        versionName = "1.0"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(DepsAndroidx.appcompat)
}
