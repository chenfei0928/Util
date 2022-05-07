package io.github.chenfei0928.base

import android.app.Application
import android.content.Context
import androidx.startup.Initializer

/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-04 13:59
 */
class ContextProvider : Initializer<Unit> {

    override fun create(context: Context) {
        val appContext = context.applicationContext as Application
        ContextProvider.context = appContext
        appContext.registerActivityLifecycleCallbacks(ActivityLifecycleCallback)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }

    companion object {
        @JvmStatic
        lateinit var context: Application
            private set
    }
}
