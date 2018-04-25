package com.chenfei.library.util

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.view.View
import com.chenfei.library.R

/**
 * 参考至：https://github.com/lgvalle/Material-Animations
 * 并有部分修改
 * Created by MrFeng on 2017/7/25.
 */
fun Activity.startActivityWithSceneTransition(intent: Intent, vararg sharedElements: View?) {
    startActivityWithTransition(this, intent, filterNull(sharedElements)
            , ActivityOptionsCompat::makeSceneTransitionAnimation)
}

private inline fun startActivityWithTransition(
        activity: Activity, intent: Intent, sharedElements: Array<View>,
        transition: (Activity, Array<Pair<View, String>>) -> ActivityOptionsCompat) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
            || PowerSaveUtil.isIsInPowerSaveMode(activity)                // 如果是省电模式，不执行动画
            || sharedElements.isEmpty()) {
        activity.startActivity(intent)
    } else {
        // 收集View的transitionName
        val list = ArrayList<Pair<View, String>>(sharedElements.size)
        for (sharedElement in sharedElements) {
            sharedElement.transitionToPair(list)
        }
        // 追加系统UI
        val sharedElementPairs = createSafeTransitionParticipants(activity, true, list)
        // 构建启动选项并启动
        val options = transition(activity, sharedElementPairs)
        ActivityCompat.startActivity(activity, intent, options.toBundle())
    }
}

/**
 * 过滤集合中的空字段
 */
private fun filterNull(views: Array<out View?>?): Array<View> {
    // 判空
    if (views == null)
        return arrayOf()
    @Suppress("UNCHECKED_CAST")
    if (views.isEmpty())
        return views as Array<View>
    // 检查有效数量
    val count = views.count { it != null }
    // 与原始集合数量比对
    @Suppress("UNCHECKED_CAST")
    if (count == views.size)
        return views as Array<View>
    if (count == 0)
        return arrayOf()
    return views.filterNotNull()
            .toTypedArray()
}

/**
 * Create the transition participants required during a activity transition while
 * avoiding glitches with the system UI.
 *
 * @param activity         The activity used as start for the transition.
 * @param includeStatusBar If false, the status bar will not be added as the transition
 * participant.
 * @return All transition participants.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
private fun createSafeTransitionParticipants(activity: Activity,
                                             includeStatusBar: Boolean,
                                             otherParticipants: List<Pair<View, String>>)
        : Array<Pair<View, String>> {
    // Avoid system UI glitches as described here:
    // https://plus.google.com/+AlexLockwood/posts/RPtwZ5nNebb
    val decor = activity.window.decorView
    val statusBar = if (includeStatusBar) decor.findViewById<View?>(android.R.id.statusBarBackground) else null
    val navBar = decor.findViewById<View?>(android.R.id.navigationBarBackground)
    val toolbar = activity.findViewById<View?>(R.id.toolbar)

    val list = ArrayList<Pair<View, String>>(otherParticipants.size + 3)
    list.addAll(otherParticipants)
    statusBar.transitionToPair(list)
    navBar.transitionToPair(list)
    toolbar.transitionToPair(list)
    return list.toTypedArray()
}

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
private fun View?.transitionToPair(collection: MutableCollection<Pair<View, String>>) {
    if (this?.transitionName?.isNotEmpty() == true)
        collection.add(Pair(this, this.transitionName))
}
