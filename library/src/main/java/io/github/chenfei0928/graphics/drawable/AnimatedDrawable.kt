package io.github.chenfei0928.graphics.drawable

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.Property
import androidx.annotation.Keep
import androidx.core.graphics.withScale
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import io.github.chenfei0928.reflect.KProperty1Property
import kotlin.reflect.KMutableProperty1

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-11-23 13:16
 */
@Keep
open class AnimatedDrawable(
    dr: Drawable
) : DrawableWrapper(dr), Animatable, Animatable2Compat {
    var scaleX = DEFAULT_SCALE
    var scaleY = DEFAULT_SCALE
    var translateX = DEFAULT_TRANSLATE
    var translateY = DEFAULT_TRANSLATE

    var pivotXRel = true
    var pivotX = DEFAULT_PIVOT
    var pivotYRel = true
    var pivotY = DEFAULT_PIVOT
    var degrees = DEFAULT_DEGREES

    override fun draw(canvas: Canvas) {
        val bounds = wrappedDrawable.bounds
        val w = bounds.right - bounds.left
        val h = bounds.bottom - bounds.top
        val px: Float = if (pivotXRel) w * pivotX else pivotX
        val py: Float = if (pivotYRel) h * pivotY else pivotY

        canvas.withScale(scaleX, scaleY) {
            canvas.translate(translateX, translateY)
            canvas.rotate(degrees, px + bounds.left, py + bounds.top)
            super.draw(canvas)
        }
    }

    private val callbacksSet = arrayListOf<Animatable2Compat.AnimationCallback>()
    private val lis = object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator) {
            callbacksSet.forEach {
                it.onAnimationStart(this@AnimatedDrawable)
            }
        }

        override fun onAnimationEnd(animation: Animator) {
            callbacksSet.forEach {
                it.onAnimationEnd(this@AnimatedDrawable)
            }
        }
    }
    val animate = AnimatorSet().apply {
        addListener(lis)
    }

    override fun onBoundsChange(bounds: Rect) {
        // noop
    }

    override fun start() {
        animate.start()
    }

    override fun stop() {
        animate.pause()
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
        const val DEFAULT_PIVOT = 0.5f
        const val DEFAULT_SCALE = 1f
        const val DEFAULT_TRANSLATE = 0f
        const val DEFAULT_DEGREES = 0f

        val SCALE_X = AnimatedDrawable::scaleX.toProperty()
        val SCALE_Y = AnimatedDrawable::scaleY.toProperty()
        val TRANSLATE_X = AnimatedDrawable::translateX.toProperty()
        val TRANSLATE_Y = AnimatedDrawable::translateY.toProperty()

        val PIVOT_X_REL = AnimatedDrawable::pivotXRel.toProperty()
        val PIVOT_X = AnimatedDrawable::pivotX.toProperty()
        val PIVOT_Y_REL = AnimatedDrawable::pivotYRel.toProperty()
        val PIVOT_Y = AnimatedDrawable::pivotY.toProperty()
        val DEGREES = AnimatedDrawable::degrees.toProperty()

        // 此处不直接调用 io.github.chenfei0928.reflect.toProperty 而是先构建内部类，减少内部类的产生
        private inline fun <reified V> KMutableProperty1<AnimatedDrawable, V>.toProperty()
                : Property<AnimatedDrawable, V> = DrawableProperty(this, V::class.java)

        private class DrawableProperty<T : Drawable, V>(
            property: KMutableProperty1<T, V>, type: Class<V>,
        ) : KProperty1Property<T, V>(property, false, type) {
            override fun afterSetBlock(value: T) {
                value.invalidateSelf()
            }
        }
    }
}
