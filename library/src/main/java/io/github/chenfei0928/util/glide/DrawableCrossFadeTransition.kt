package io.github.chenfei0928.util.glide

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.view.Gravity
import com.bumptech.glide.request.transition.Transition

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-09 15:50
 */
class DrawableCrossFadeTransition(
    val duration: Int,
    private val isCrossFadeEnabled: Boolean
) : Transition<Drawable> {

    override fun transition(current: Drawable, adapter: Transition.ViewAdapter): Boolean {
        var previous = adapter.currentDrawable
        if (previous == null) {
            previous = ColorDrawable(Color.TRANSPARENT)
        }
        val transitionDrawable = TransitionDrawable(arrayOf(previous, current))
        transitionDrawable.isCrossFadeEnabled = isCrossFadeEnabled
        transitionDrawable.startTransition(duration)
        // 修复在alpha淡入淡出动画时，图片被拉伸问题
        // https://www.jianshu.com/p/485a9d13e6a9
        // https://stackoverflow.com/questions/32235413/glide-load-drawable-but-dont-scale-placeholder
        // https://github.com/TWiStErRob/glide-support/tree/master/src/glide3/java/com/bumptech/glide/supportapp/stackoverflow/_32235413_crossfade_placeholder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            transitionDrawable.setLayerGravity(0, Gravity.CENTER)
            transitionDrawable.setLayerGravity(1, Gravity.CENTER)
        }
        adapter.setDrawable(transitionDrawable)
        return true
    }
}
