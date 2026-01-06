package io.github.chenfei0928.util.qrcode

import android.graphics.Bitmap
import androidx.annotation.Px
import androidx.collection.ArrayMap
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.withScale
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import io.github.chenfei0928.util.Log
import java.io.IOException

/**
 * 二维码工具类
 * [原博客](http://blog.csdn.net/books1958/article/details/46346531)
 *
 * 在使用当前类时确认引入了 [Zxing](https://github.com/zxing/zxing) 依赖，当前类有使用 `com.google.zxing` 包下多个类
 *
 * Created by MrFeng on 2016/11/8.
 */
object QRCodeUtil {
    private const val TAG = "Ut_QRCodeUtil"

    /**
     * 生成二维码Bitmap
     *
     * @param content   内容
     * @param widthPix  图片宽度
     * @param heightPix 图片高度
     * @param logoBm    二维码中心的Logo图标（可以为null）
     * @return 生成二维码及保存文件是否成功
     */
    @JvmStatic
    fun createQRImage(
        content: String?, @Px widthPix: Int, @Px heightPix: Int, logoBm: Bitmap?
    ): Bitmap? = if (content.isNullOrBlank()) {
        null
    } else try {
        createQrBitMatrix(content, widthPix, heightPix)
            ?.toBitmap(Bitmap.Config.ARGB_8888)
            ?.addLogo(logoBm)
    } catch (e: IOException) {
        Log.e(TAG, "createQRImage: ", e)
        null
    }

    @JvmStatic
    fun createQrBitMatrix(
        content: String?, @Px widthPix: Int, @Px heightPix: Int
    ): BitMatrix? = if (content.isNullOrBlank() || widthPix == 0 || heightPix == 0) {
        null
    } else try {
        // 配置参数
        val hints = ArrayMap<EncodeHintType, Any>(3).apply {
            this[EncodeHintType.CHARACTER_SET] = "utf-8"
            // 容错级别
            this[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L
            // 设置空白边距的宽度
            this[EncodeHintType.MARGIN] = 1 // default is 4
        }
        // 图像数据转换，使用了矩阵转换
        QRCodeWriter().encode(
            content, BarcodeFormat.QR_CODE, widthPix, heightPix, hints
        )
    } catch (e: WriterException) {
        Log.e(TAG, "createQRBitmap: ", e)
        null
    }

    /**
     * 在二维码中间添加Logo图案
     */
    private fun Bitmap.addLogo(logo: Bitmap?) = apply {
        if (logo == null) {
            return@apply
        }

        // 获取图片的宽高
        val srcWidth = this.width
        val srcHeight = this.height
        val logoWidth = logo.width
        val logoHeight = logo.height

        if (srcWidth == 0 || srcHeight == 0) {
            return@apply
        }

        if (logoWidth == 0 || logoHeight == 0) {
            return@apply
        }

        // logo大小为二维码整体大小的1/5
        val scaleFactor = srcWidth / 5f / logoWidth.toFloat()
        this.applyCanvas {
            withScale(scaleFactor, scaleFactor, srcWidth / 2f, srcHeight / 2f) {
                drawBitmap(logo, (srcWidth - logoWidth) / 2f, (srcHeight - logoHeight) / 2f, null)
            }
        }
    }
}
