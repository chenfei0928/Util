package io.github.chenfei0928.base

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import io.github.chenfei0928.util.DependencyChecker

/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-04 13:59
 */
class UtilInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val appContext = context.applicationContext as Application
        UtilInitializer.context = appContext
        appContext.registerActivityLifecycleCallbacks(ActivityLifecycleCallback)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }

    companion object {
        @JvmStatic
        lateinit var context: Application
            private set

        @JvmStatic
        var sdkDependency: DependencyChecker = DependencyChecker.ByReflectLazy
            set(value) {
                require(value !is DependencyChecker.Companion) { "需要为 DependencyChecker.SdkDependency 的实例" }
                field = value
            }
    }
}
