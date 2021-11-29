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
        applicationId = "com.chenfei.util"
        minSdk = 14
        targetSdk = 30
        versionName = "1.0"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
}
