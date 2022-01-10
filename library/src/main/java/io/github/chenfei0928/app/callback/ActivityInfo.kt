package io.github.chenfei0928.app.callback

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.lifecycle.Lifecycle
import io.github.chenfei0928.content.IntentFilterBroadcastReceiver
import io.github.chenfei0928.reflect.asType
import kotlinx.parcelize.Parcelize

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-01-13 16:06
 */
@Parcelize
data class ActivityInfo(
    @JvmField val processName: String,
    @JvmField val packageName: String,
    @JvmField val activityClassName: String
) : Parcelable {

    fun toIntent(event: Lifecycle.Event) = Intent(ACTION).apply {
        `package` = packageName
        putExtra(EVENT, event)
        putExtra(ACTIVITY_INFO, this@ActivityInfo)
    }

    companion object {
        internal const val EVENT = "event"
        internal const val ACTIVITY_INFO = "activityInfo"
        internal const val ACTION = "io.github.chenfei0928.util.MULTI_PROCESS_ACTIVITY_LIFECYCLE_CALLBACK"
    }
}

interface MultiProcessActivityLifecycleCallback {
    fun onActivityEvent(activityInfo: ActivityInfo, event: Lifecycle.Event)
}

object ActivityInfoReceiver : IntentFilterBroadcastReceiver(ActivityInfo.ACTION) {
    private val lifecycleCallback: MutableSet<MultiProcessActivityLifecycleCallback> =
        mutableSetOf()

    fun registerCallback(callback: MultiProcessActivityLifecycleCallback) =
        lifecycleCallback.add(callback)

    fun unregisterCallback(callback: MultiProcessActivityLifecycleCallback) =
        lifecycleCallback.remove(callback)

    override fun onReceive(context: Context, intent: Intent) {
        val event: Lifecycle.Event =
            intent.getSerializableExtra(ActivityInfo.EVENT) as? Lifecycle.Event ?: return
        val activityInfo =
            intent.getParcelableExtra<ActivityInfo>(ActivityInfo.ACTIVITY_INFO) ?: return

        lifecycleCallback.forEach {
            it.onActivityEvent(activityInfo, event)
        }
    }

    override fun register(context: Context) {
        super.register(context)
        context.applicationContext
            .asType<Application>()
            ?.registerActivityLifecycleCallbacks(ProcessActivityLifecycleCallbackSender())
    }
}
