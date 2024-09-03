package io.github.chenfei0928.util.qrcode

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.annotation.Px
import androidx.core.graphics.withScale
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import io.github.chenfei0928.util.Log
import java.io.File
import java.io.IOException

/**
 * 二维码工具类
 * [原博客](http://blog.csdn.net/books1958/article/details/46346531)
 *
 * Created by MrFeng on 2016/11/8.
 */
object QRCodeUtil {
    private const val TAG = "KW_QRCodeUtil"

    /**
     * 生成二维码Bitmap
     *
     * @param content   内容
     * @param widthPix  图片宽度
     * @param heightPix 图片高度
     * @param logoBm    二维码中心的Logo图标（可以为null）
     * @param filePath  用于存储二维码图片的文件路径
     * @return 生成二维码及保存文件是否成功
     */
    @JvmStatic
    fun createQRImage(
        content: String?, @Px widthPix: Int, @Px heightPix: Int, logoBm: Bitmap?, filePath: String
    ): Boolean = if (content.isNullOrBlank()) {
        false
    } else try {
        var bitmap = createQRBitmap(content, widthPix, heightPix)
        if (logoBm != null && bitmap != null) {
            bitmap = addLogo(bitmap, logoBm)
        }
        // 必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
        bitmap != null && bitmap.compress(
            Bitmap.CompressFormat.JPEG, 100, File(filePath).outputStream()
        )
    } catch (e: IOException) {
        Log.e(TAG, "createQRImage: ", e)
        false
    }

    @JvmStatic
    fun createQRBitmap(
        content: String?, @Px widthPix: Int, @Px heightPix: Int
    ): Bitmap? = if (content.isNullOrBlank() || widthPix == 0 || heightPix == 0) {
        null
    } else try {
        // 配置参数
        val hints = androidx.collection.ArrayMap<EncodeHintType, Any>(3).apply {
            this[EncodeHintType.CHARACTER_SET] = "utf-8"
            // 容错级别
            this[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L
            // 设置空白边距的宽度
            this[EncodeHintType.MARGIN] = 1 // default is 4
        }

        // 图像数据转换，使用了矩阵转换
        val bitMatrix = QRCodeWriter().encode(
            content, BarcodeFormat.QR_CODE, widthPix, heightPix, hints
        )
        val pixels = IntArray(bitMatrix.width * bitMatrix.height)
        // 下面这里按照二维码的算法，逐个生成二维码的图片
        // 两个for循环是图片横列扫描的结果
        for (y in 0 until bitMatrix.height) {
            for (x in 0 until bitMatrix.width) {
                if (bitMatrix.get(x, y)) {
                    pixels[y * bitMatrix.width + x] = Color.BLACK
                } else {
                    pixels[y * bitMatrix.width + x] = Color.TRANSPARENT
                }
            }
        }

        // 生成二维码图片的格式，使用 ALPHA_8
        Bitmap.createBitmap(
            pixels, bitMatrix.width, bitMatrix.height, Bitmap.Config.ALPHA_8
        )
    } catch (e: WriterException) {
        Log.e(TAG, "createQRBitmap: ", e)
        null
    }

    /**
     * 在二维码中间添加Logo图案
     */
    private fun addLogo(src: Bitmap?, logo: Bitmap?): Bitmap? {
        if (src == null) {
            return null
        }

        if (logo == null) {
            return src
        }

        // 获取图片的宽高
        val srcWidth = src.width
        val srcHeight = src.height
        val logoWidth = logo.width
        val logoHeight = logo.height

        if (srcWidth == 0 || srcHeight == 0) {
            return null
        }

        if (logoWidth == 0 || logoHeight == 0) {
            return src
        }

        return try {
            // logo大小为二维码整体大小的1/5
            val scaleFactor = srcWidth * 1.0f / 5f / logoWidth.toFloat()
            val bitmap: Bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(bitmap)
            canvas.drawBitmap(src, 0f, 0f, null)
            canvas.withScale(
                scaleFactor, scaleFactor, (srcWidth / 2).toFloat(), (srcHeight / 2).toFloat()
            ) {
                drawBitmap(logo, (srcWidth - logoWidth) / 2f, (srcHeight - logoHeight) / 2f, null)
            }
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "addLogo: ", e)
            null
        }
    }
}
