package io.github.chenfei0928.util.glide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.Log
import android.view.Gravity
import androidx.annotation.GravityInt
import androidx.annotation.Px
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import jp.wasabeef.glide.transformations.BitmapTransformation
import java.security.MessageDigest

/**
 * 使用Gravity来裁剪的 [BitmapTransformation]，支持两个方向上的Gravity位置摆放。
 *
 * 当只有一个方向被设置时，将会对另一个轴进行拉伸/缩放以全量显示，并在其设置轴上对齐（可能会留黑边）。
 * 当水平/垂直两个方向都被设置时，将会以不留黑边为目的进行缩放，并对更长的进行对齐。
 *
 * @author MrFeng()
 * @date 2022-05-12 11:00
 */
class GravityCropTransformation(
    @Px
    private val width: Int,
    @Px
    private val height: Int,
    @GravityInt
    private val gravity: Int = Gravity.CENTER,
) : BitmapTransformation() {

    override fun transform(
        context: Context, pool: BitmapPool,
        toTransform: Bitmap, outWidth: Int, outHeight: Int
    ): Bitmap {
        val width = if (width == 0) outWidth else width
        val height = if (height == 0) outHeight else height

        val bitmap = pool[width, height, toTransform.config]
        bitmap.setHasAlpha(true)

        val targetRect = run {
            val scale = getScale(toTransform, bitmap)

            val scaledWidth = scale * toTransform.width
            val scaledHeight = scale * toTransform.height
            val left = getLeft(width, scaledWidth)
            val top = getTop(height, scaledHeight)
            RectF(left, top, left + scaledWidth, top + scaledHeight)
        }

        bitmap.density = toTransform.density

        val canvas = Canvas(bitmap)
        canvas.drawBitmap(toTransform, null, targetRect, null)

        return bitmap
    }

    private fun getScale(src: Bitmap, dest: Bitmap): Float {
        val isHorizontal = Gravity.isHorizontal(gravity)
        val isVertical = Gravity.isVertical(gravity)
        val scaleX = dest.width.toFloat() / src.width
        val scaleY = dest.height.toFloat() / src.height
        return when {
            isHorizontal && isVertical -> {
                maxOf(scaleX, scaleY)
            }
            isHorizontal -> {
                scaleY
            }
            isVertical -> {
                scaleX
            }
            else -> {
                Log.i(TAG, "getScale: ${Companion.toString(gravity)}")
                maxOf(scaleX, scaleY)
            }
        }
    }

    private fun getTop(targetHeight: Int, scaledHeight: Float): Float {
        return when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
            Gravity.TOP -> 0f
            Gravity.CENTER_VERTICAL -> (targetHeight - scaledHeight) / 2
            Gravity.BOTTOM -> targetHeight - scaledHeight
            else -> 0f
        }
    }

    private fun getLeft(targetWidth: Int, scaledWidth: Float): Float {
        return when (gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
            Gravity.LEFT -> 0f
            Gravity.CENTER_HORIZONTAL -> (targetWidth - scaledWidth) / 2
            Gravity.RIGHT -> targetWidth - scaledWidth
            else -> 0f
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GravityCropTransformation

        if (width != other.width) return false
        if (height != other.height) return false
        if (gravity != other.gravity) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ID.hashCode()
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + gravity
        return result
    }

    override fun toString(): String {
        return "CropTransformation(width=$width, height=$height, gravity=$gravity:${toString(gravity)})"
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update((ID + width + height + gravity).toByteArray(CHARSET))
    }

    companion object {
        private const val TAG = "KW_GravityCropTrans"
        private const val VERSION = 1
        private const val ID = "io.github.chenfei0928.util.glide.CropTransformation.$VERSION"

        //<editor-fold desc="toString" defaultstatus="collapsed">
        /**
         * 参考自 [Gravity.toString]
         * @hide
         */
        fun toString(@GravityInt gravity: Int): String {
            val result = StringBuilder()
            if (gravity and Gravity.FILL == Gravity.FILL) {
                result.append("FILL").append(' ')
            } else {
                if (gravity and Gravity.FILL_VERTICAL == Gravity.FILL_VERTICAL) {
                    result.append("FILL_VERTICAL").append(' ')
                } else {
                    if (gravity and Gravity.TOP == Gravity.TOP) {
                        result.append("TOP").append(' ')
                    }
                    if (gravity and Gravity.BOTTOM == Gravity.BOTTOM) {
                        result.append("BOTTOM").append(' ')
                    }
                }
                if (gravity and Gravity.FILL_HORIZONTAL == Gravity.FILL_HORIZONTAL) {
                    result.append("FILL_HORIZONTAL").append(' ')
                } else {
                    if (gravity and Gravity.START == Gravity.START) {
                        result.append("START").append(' ')
                    } else if (gravity and Gravity.LEFT == Gravity.LEFT) {
                        result.append("LEFT").append(' ')
                    }
                    if (gravity and Gravity.END == Gravity.END) {
                        result.append("END").append(' ')
                    } else if (gravity and Gravity.RIGHT == Gravity.RIGHT) {
                        result.append("RIGHT").append(' ')
                    }
                }
            }
            if (gravity and Gravity.CENTER == Gravity.CENTER) {
                result.append("CENTER").append(' ')
            } else {
                if (gravity and Gravity.CENTER_VERTICAL == Gravity.CENTER_VERTICAL) {
                    result.append("CENTER_VERTICAL").append(' ')
                }
                if (gravity and Gravity.CENTER_HORIZONTAL == Gravity.CENTER_HORIZONTAL) {
                    result.append("CENTER_HORIZONTAL").append(' ')
                }
            }
            if (result.isEmpty()) {
                result.append("NO GRAVITY").append(' ')
            }
            if (gravity and Gravity.DISPLAY_CLIP_VERTICAL == Gravity.DISPLAY_CLIP_VERTICAL) {
                result.append("DISPLAY_CLIP_VERTICAL").append(' ')
            }
            if (gravity and Gravity.DISPLAY_CLIP_HORIZONTAL == Gravity.DISPLAY_CLIP_HORIZONTAL) {
                result.append("DISPLAY_CLIP_HORIZONTAL").append(' ')
            }
            result.deleteCharAt(result.length - 1)
            return result.toString()
        }
        //</editor-fold>
    }
}
