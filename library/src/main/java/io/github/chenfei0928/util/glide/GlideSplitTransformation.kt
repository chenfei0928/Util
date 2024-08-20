package io.github.chenfei0928.util.glide

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.Px
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.bumptech.glide.util.Util
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.math.min

/**
 * Created by MrFeng on 2018/3/5.
 */
class GlideSplitTransformation(
    private val page: Int,
    @Px
    private val pageHeight: Int
) : BitmapTransformation() {

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val config = toTransform.config
        val currentPageStartY = page * pageHeight
        val currentPageHeight = min(
            (toTransform.height - currentPageStartY).toDouble(),
            pageHeight.toDouble()
        ).toInt()
        val result = pool[toTransform.width, currentPageHeight, config]
        result.setHasAlpha(true)

        val canvas = Canvas(result)
        canvas.drawBitmap(toTransform, 0f, currentPageStartY.toFloat(), DEFAULT_PAINT)
        canvas.setBitmap(null)

        return result
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)

        val radiusData = ByteBuffer.allocate(4).putInt(page).putInt(pageHeight).array()
        messageDigest.update(radiusData)
    }

    override fun hashCode(): Int {
        return Util.hashCode(ID.hashCode(), Util.hashCode(page, Util.hashCode(pageHeight)))
    }

    override fun equals(other: Any?): Boolean {
        if (other is GlideSplitTransformation) {
            return (this.page == other.page
                    && this.pageHeight == other.pageHeight)
        }
        return false
    }

    companion object {
        private val DEFAULT_PAINT = Paint(TransformationUtils.PAINT_FLAGS)
        private const val ID = "io.github.chenfei0928.util.glide.GlideSplitTransformation"
        private val ID_BYTES = ID.toByteArray(CHARSET)
    }
}
