import io.github.chenfei0928.Deps
import io.github.chenfei0928.DepsAndroidx
import io.github.chenfei0928.android.applyApp
import io.github.chenfei0928.android.applyTest
import io.github.chenfei0928.compiler.applyKotlin

plugins {
    android
}

applyApp()
applyTest()
applyKotlin()

android {
    namespace = "io.github.chenfei0928.util"

    defaultConfig {
        applicationId = "io.github.chenfei0928.util"
        versionName = "1.0"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(DepsAndroidx.appcompat)
    implementation(DepsAndroidx.multidex.core)

    implementation(Deps.lib.util) {
        isChanging = "SNAPSHOT" in version.toString()
    }
}
