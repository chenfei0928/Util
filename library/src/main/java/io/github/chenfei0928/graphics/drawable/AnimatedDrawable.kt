package io.github.chenfei0928.graphics.drawable

import android.animation.Animator
import android.animation.AnimatorSet
import android.graphics.Canvas
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.Property
import androidx.annotation.Keep
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import io.github.chenfei0928.util.kotlin.ANIMATE_LAYOUT_CHANGES_DEFAULT_DURATION
import io.github.chenfei0928.util.kotlin.toProperty
import kotlin.reflect.KMutableProperty1

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-11-23 13:16
 */
@Keep
class AnimatedDrawable(dr: Drawable) : DrawableWrapper(dr), Animatable, Animatable2Compat {
    var scaleX = 1f
    var scaleY = 1f
    var translateX = 0f
    var translateY = 0f

    var pivotXRel = true
    var pivotX = 0.5f
    var pivotYRel = true
    var pivotY = 0.5f
    var degrees = 0.0f

    override fun draw(canvas: Canvas) {
        val bounds = wrappedDrawable.bounds
        val w = bounds.right - bounds.left
        val h = bounds.bottom - bounds.top
        val px: Float = if (pivotXRel) w * pivotX else pivotX
        val py: Float = if (pivotYRel) h * pivotY else pivotY

        val saveCount = canvas.save()
        canvas.scale(scaleX, scaleY)
        canvas.translate(translateX, translateY)
        canvas.rotate(degrees, px + bounds.left, py + bounds.top)
        super.draw(canvas)
        canvas.restoreToCount(saveCount)
    }

    private val callbacksSet = arrayListOf<Animatable2Compat.AnimationCallback>()
    private val lis = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {
            callbacksSet.forEach {
                it.onAnimationStart(this@AnimatedDrawable)
            }
        }

        override fun onAnimationEnd(animation: Animator?) {
            callbacksSet.forEach {
                it.onAnimationEnd(this@AnimatedDrawable)
            }
        }

        override fun onAnimationCancel(animation: Animator?) {
        }

        override fun onAnimationRepeat(animation: Animator?) {
        }
    }
    val animate = AnimatorSet().apply {
        duration = ANIMATE_LAYOUT_CHANGES_DEFAULT_DURATION
        addListener(lis)
    }

    override fun start() {
        animate.start()
    }

    override fun stop() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            animate.pause()
        } else {
            animate.cancel()
        }
    }

    override fun isRunning(): Boolean {
        return animate.isRunning
    }

    override fun registerAnimationCallback(callback: Animatable2Compat.AnimationCallback) {
        callbacksSet.add(callback)
    }

    override fun unregisterAnimationCallback(callback: Animatable2Compat.AnimationCallback): Boolean {
        return callbacksSet.remove(callback)
    }

    override fun clearAnimationCallbacks() {
        callbacksSet.clear()
    }

    companion object {
        val SCALE_X = AnimatedDrawable::scaleX.toProperty()
        val SCALE_Y = AnimatedDrawable::scaleY.toProperty()
        val TRANSLATE_X = AnimatedDrawable::translateX.toProperty()
        val TRANSLATE_Y = AnimatedDrawable::translateY.toProperty()

        val PIVOT_X_REL = AnimatedDrawable::pivotXRel.toProperty()
        val PIVOT_X = AnimatedDrawable::pivotX.toProperty()
        val PIVOT_Y_REL = AnimatedDrawable::pivotYRel.toProperty()
        val PIVOT_Y = AnimatedDrawable::pivotY.toProperty()
        val DEGREES = AnimatedDrawable::degrees.toProperty()

        private fun <V> KMutableProperty1<AnimatedDrawable, V>.toProperty(): Property<AnimatedDrawable, V> =
            toProperty(AnimatedDrawable::invalidateSelf)
    }
}
