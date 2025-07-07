package io.github.chenfei0928.demo

import android.app.Application
import io.github.chenfei0928.base.UtilInitializer
import io.github.chenfei0928.util.DependencyChecker

/**
 * @author chenf()
 * @date 2025-02-24 18:44
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        UtilInitializer.sdkDependency = DependencyChecker.UserInput(
            material = false,
            guavaListenableFuture = false,
            guava = false,
            androidXListenableFuture = false,
            flexBox = false,
            protobufFull = true,
            protobufLite = true,
            gson = true,
            mmkv = true,
        )
    }
}
