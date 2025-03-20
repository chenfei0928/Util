package io.github.chenfei0928.util.qrcode

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.common.BitMatrix

/**
 * @author chenf()
 * @date 2025-03-17 14:51
 */
fun BitMatrix.toBitmap(config: Bitmap.Config = Bitmap.Config.ALPHA_8): Bitmap {
    val width = width
    val height = height

    val pixels = IntArray(width * height)
    // 下面这里按照二维码的算法，逐个生成二维码的图片
    // 两个for循环是图片横列扫描的结果
    for (y in 0 until height) {
        for (x in 0 until width) {
            if (get(x, y)) {
                pixels[y * width + x] = Color.BLACK
            } else {
                pixels[y * width + x] = Color.TRANSPARENT
            }
        }
    }
    // 生成二维码图片的格式
    return Bitmap.createBitmap(pixels, width, height, config)
}
