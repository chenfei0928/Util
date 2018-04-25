package com.chenfei.library.util.glide

import android.graphics.Bitmap
import android.support.annotation.IntRange
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg

/**
 * Glide的长图分页Target
 * 使用时必须关闭当次请求的硬件加速（部分设备上在[Bitmap.createBitmap]时会裁出来之后全黑）
 * 由于长图尺寸较大，建议使用[com.bumptech.glide.load.DecodeFormat.PREFER_RGB_565]配置以降低内存消耗（使用此配置必须关闭硬件加速）
 * 设置[com.bumptech.glide.request.RequestOptions.diskCacheStrategy]为[com.bumptech.glide.load.engine.DiskCacheStrategy.DATA]
 * 和[com.bumptech.glide.request.RequestOptions.skipMemoryCache]为[true]以禁用本次请求的内存缓存
 * Created by MrFeng on 2018/3/2.
 */
abstract class GlideLongImageGroupTarget(
        @param:IntRange(from = 1, to = 4096)
        private val pageHeight: Int
) : SimpleTarget<Bitmap>() {
    private val splitBitmaps: MutableList<Bitmap> = ArrayList()

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        val height = resource.height
        // 对单页高度分页显示，超出部分独立一页
        val page = height / pageHeight + if (height % pageHeight > 0) 1 else 0
        if (page == 1) {
            splitBitmaps.add(resource)
            onResourceReady(splitBitmaps)
        } else {
            val width = resource.width
            // 裁剪并收集每个分页bitmap
            async(UI) {
                val bg = bg {
                    for (currentPage in 0 until page) {
                        val currentPageStartY = currentPage * pageHeight
                        val currentPageHeight = Math.min(height - currentPageStartY, pageHeight)
                        splitBitmaps.add(Bitmap.createBitmap(resource, 0, currentPageStartY, width, currentPageHeight))
                    }
                    // 无分页的原始bitmap仍然由Glide管理，不能在此处回收，此处不要去调用 resource.recycle()
                    return@bg splitBitmaps
                }
                onResourceReady(bg.await())
            }
        }
    }

    abstract fun onResourceReady(splitBitmaps: List<Bitmap>)

    fun release() {
        // 如果只有一个子view说明没有进行分页，bitmap缓存释放由Glide负责，不能由此处回收
        if (splitBitmaps.size > 1) {
            splitBitmaps.forEach { it.recycle() }
        }
    }
}
