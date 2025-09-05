package io.github.chenfei0928.app.callback

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import io.github.chenfei0928.content.IntentFilterBroadcastReceiver
import io.github.chenfei0928.os.getParcelableExtraCompat

/**
 * @author chenfei()
 * @date 2022-10-19 18:53
 */
object ActivityInfoReceiver : IntentFilterBroadcastReceiver(
    ProcessActivityLifecycleCallbackSender.ACTION
) {
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
        intent.setExtrasClassLoader(ActivityInfo::class.java.classLoader)
        val activityInfo: ActivityInfo = intent.getParcelableExtraCompat(
            ProcessActivityLifecycleCallbackSender.ACTIVITY_INFO
        ) ?: return

        lifecycleLiveData.value = activityInfo
    }

    override fun register(context: Context) {
        super.register(context)
        (context.applicationContext as? Application)
            ?.registerActivityLifecycleCallbacks(ProcessActivityLifecycleCallbackSender())
    }
}
