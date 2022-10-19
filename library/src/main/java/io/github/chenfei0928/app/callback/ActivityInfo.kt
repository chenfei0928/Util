package io.github.chenfei0928.app.callback

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
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
    @JvmField val activityClassName: String,
    @JvmField val event: Lifecycle.Event,
) : Parcelable {

    fun toIntent() = Intent(ACTION).apply {
        `package` = packageName
        putExtra(ACTIVITY_INFO, this@ActivityInfo)
    }

    companion object {
        internal const val ACTIVITY_INFO = "activityInfo"
        internal const val ACTION =
            "io.github.chenfei0928.util.MULTI_PROCESS_ACTIVITY_LIFECYCLE_CALLBACK"
    }
}

object ActivityInfoReceiver : IntentFilterBroadcastReceiver(ActivityInfo.ACTION) {
    private val lifecycleLiveData = MutableLiveData<ActivityInfo>()

    fun observe(owner: LifecycleOwner, observer: Observer<ActivityInfo>) =
        lifecycleLiveData.observe(owner, observer)

    fun observeForever(observer: Observer<ActivityInfo>) =
        lifecycleLiveData.observeForever(observer)

    fun removeObserver(observer: Observer<ActivityInfo>) =
        lifecycleLiveData.removeObserver(observer)

    fun removeObservers(owner: LifecycleOwner) =
        lifecycleLiveData.removeObservers(owner)

    override fun onReceive(context: Context, intent: Intent) {
        val activityInfo =
            intent.getParcelableExtra<ActivityInfo>(ActivityInfo.ACTIVITY_INFO) ?: return

        lifecycleLiveData.value = activityInfo
    }

    override fun register(context: Context) {
        super.register(context)
        context.applicationContext
            .asType<Application>()
            ?.registerActivityLifecycleCallbacks(ProcessActivityLifecycleCallbackSender())
    }
}
