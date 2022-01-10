package io.github.chenfei0928.util

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import io.github.chenfei0928.collection.arrayOfNotNull
import java.util.*

/**
 * [Github地址](https://github.com/lgvalle/Material-Animations)
 * Created by MrFeng on 2017/7/25.
 */
fun Activity.startActivityWithSceneTransition(intent: Intent, vararg sharedElements: View?) {
    startActivityWithTransition(this,
        { i, bundle -> ActivityCompat.startActivity(this, i, bundle) },
        intent,
        arrayOfNotNull(*sharedElements),
        { ActivityOptionsCompat.makeSceneTransitionAnimation(this, *it) })
}

fun Fragment.startActivityWithSceneTransition(intent: Intent, vararg sharedElements: View?) {
    startActivityWithTransition(requireActivity(),
        { i, bundle -> startActivity(i, bundle) },
        intent,
        arrayOfNotNull(*sharedElements),
        { ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), *it) })
}

private inline fun startActivityWithTransition(
    context: Activity,
    startActivityWithBundle: (Intent, Bundle?) -> Unit,
    intent: Intent,
    sharedElements: Array<View>,
    transition: (Array<Pair<View, String>>) -> ActivityOptionsCompat
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
        // 如果是省电模式，不执行动画
        || PowerSaveUtil.isInPowerSaveMode(context) || sharedElements.isEmpty()
    ) {
        startActivityWithBundle(intent, null)
    } else {
        val options = context.createActivityOption(
            true, *sharedElements, transition = transition
        )
        startActivityWithBundle(intent, options.toBundle())
    }
}

fun Fragment.createActivityOptionWithSceneTransition(vararg sharedElements: View?) =
    requireActivity().createActivityOptionWithSceneTransition(*sharedElements)

fun Activity.createActivityOptionWithSceneTransition(vararg sharedElements: View?) =
    createActivityOption(true, *sharedElements) {
        ActivityOptionsCompat.makeSceneTransitionAnimation(this, *it)
    }

private inline fun Activity.createActivityOption(
    includeStatusBar: Boolean = true,
    vararg sharedElements: View?,
    transition: (Array<Pair<View, String>>) -> ActivityOptionsCompat
): ActivityOptionsCompat {
    val fillTransitionView = fillTransitionView(includeStatusBar, *sharedElements)
    return transition(fillTransitionView)
}

private fun Activity.fillTransitionView(
    includeStatusBar: Boolean = true,
    vararg sharedElements: View?
): Array<Pair<View, String>> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
        // 如果是省电模式，不执行动画
        || PowerSaveUtil.isInPowerSaveMode(this) || sharedElements.isEmpty()
    ) {
        return arrayOf()
    }
    // 收集View的transitionName
    val list = ArrayList<Pair<View, String>>(sharedElements.size)
    for (sharedElement in sharedElements) {
        sharedElement.transitionToPair(list)
    }
    // 追加系统UI
    return createSafeTransitionParticipants(this, includeStatusBar, list)
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
private fun createSafeTransitionParticipants(
    activity: Activity, includeStatusBar: Boolean, otherParticipants: List<Pair<View, String>>
): Array<Pair<View, String>> {
    // Avoid system UI glitches as described here:
    // https://plus.google.com/+AlexLockwood/posts/RPtwZ5nNebb
    val decor = activity.window.decorView
    val statusBar =
        if (includeStatusBar) decor.findViewById<View?>(android.R.id.statusBarBackground) else null
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
    if (this?.transitionName?.isNotEmpty() == true) {
        collection.add(Pair(this, this.transitionName))
    }
}
