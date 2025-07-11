package io.github.chenfei0928.util.glide

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.opengl.GLES10
import android.opengl.GLES11
import android.opengl.GLES20
import android.opengl.GLES30
import android.os.Build
import androidx.annotation.EmptySuper
import androidx.annotation.IntRange
import androidx.annotation.Px
import androidx.annotation.WorkerThread
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import io.github.chenfei0928.lang.use
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import kotlin.coroutines.CoroutineContext
import kotlin.math.max
import kotlin.math.min

/**
 * Glide的长图分页Target
 *
 * 使用时必须关闭当次请求的硬件加速（部分设备上在[Bitmap.createBitmap]时会裁出来之后全黑）。
 * 硬件加速时会使用OpenGLES接口来通过将纹理（Bitmap）发送到GPU内存中，由GPU直接负责绘制Bitmap，
 * 但这个GPU纹理有尺寸限制，根据不同设备上GPU实现不同会有不同。
 *
 * 由于长图尺寸较大，建议使用[DecodeFormat.PREFER_RGB_565]配置以降低内存消耗（使用此配置必须关闭硬件加速）
 * 设置[RequestOptions.diskCacheStrategy]为[DiskCacheStrategy.DATA]
 * 和[RequestOptions.skipMemoryCache]为(true)以禁用本次请求的内存缓存
 *
 * [关于pageHeight取值](https://github.com/nostra13/Android-Universal-Image-Loader/issues/281)
 *
 * Created by MrFeng on 2018/3/2.
 */
abstract class GlideLongImageGroupTarget(
    @all:Px
    @all:IntRange(from = 1, to = 4096)
    private val pageHeight: Int,
    private val inPreferredConfig: Bitmap.Config = Bitmap.Config.RGB_565,
) : CustomTarget<File>(), CoroutineScope {
    private var job = Job() // 定义job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job // 生命周期的协程
    private val splitBitmaps: MutableList<Bitmap> = ArrayList()

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    @EmptySuper
    override fun onLoadCleared(placeholder: Drawable?) {
        // noop
    }

    @Suppress("kotlin:S6310")
    override fun onResourceReady(resource: File, transition: Transition<in File>?) {
        launch(Dispatchers.Main) {
            val list = withContext(Dispatchers.Default) {
                splitBitmaps(resource)
            }
            splitBitmaps.addAll(list)
            onResourceReady(splitBitmaps)
        }
    }

    @WorkerThread
    @Suppress("ReturnCount")
    private fun splitBitmaps(resource: File): List<Bitmap> {
        val tmpBitmapOptions = BitmapFactory.Options()
        // 只进行获取尺寸的解码以获取其高度
        tmpBitmapOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(resource.absolutePath, tmpBitmapOptions)
        tmpBitmapOptions.inJustDecodeBounds = false
        val height = tmpBitmapOptions.outHeight
        val width = tmpBitmapOptions.outWidth

        // 解码的bitmapConfig
        tmpBitmapOptions.inPreferredConfig = this@GlideLongImageGroupTarget.inPreferredConfig
        // 对单页高度分页显示，超出部分独立一页
        val pageCount = height / pageHeight + if (height % pageHeight > 0) 1 else 0
        if (pageCount == 1) {
            // 解码bitmap
            return listOf(BitmapFactory.decodeFile(resource.absolutePath, tmpBitmapOptions))
        }

        // bitmap范围解码器，只有jpg、png支持范围解码
        val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            BitmapRegionDecoder.newInstance(resource.absolutePath)
        } else {
            @Suppress("DEPRECATION")
            BitmapRegionDecoder.newInstance(resource.absolutePath, true)
        }.use(BitmapRegionDecoder::recycle) { regionDecoder ->
            val tmpRect = Rect()
            // 裁剪并收集每个分页bitmap
            (0 until pageCount).mapTo(ArrayList(pageCount)) { currentPage ->
                val currentPageStartY = currentPage * pageHeight
                val currentPageHeight = min(height - currentPageStartY, pageHeight)
                @Suppress("kotlin:S6518")
                tmpRect.set(0, currentPageStartY, width, currentPageStartY + currentPageHeight)
                // 进行区域解码
                regionDecoder.decodeRegion(tmpRect, tmpBitmapOptions)
            }
        }
        // todo 添加检查解码格式不支持（webp、静态gif）时会不会报错或区域解码返回null
        if (null !in list) {
            return list
        }

        // 如果解码方式是硬件加速，将其取消（硬件加速bitmap在裁剪时将会由硬件负责渲染和裁切，也会遇到pageHeight限制问题）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && tmpBitmapOptions.inPreferredConfig == Bitmap.Config.HARDWARE
        ) {
            tmpBitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        // 解码完整的bitmap
        return BitmapFactory.decodeFile(
            resource.absolutePath, tmpBitmapOptions
        ).use(Bitmap::recycle) { fullBitmap ->
            list.mapIndexed { currentPage, bitmap ->
                // 如果当前bitmap为null，重新从完整bitmap中裁剪子区域
                val currentPageStartY = currentPage * pageHeight
                val currentPageHeight = min(height - currentPageStartY, pageHeight)
                bitmap ?: Bitmap.createBitmap(
                    fullBitmap, 0, currentPageStartY, width, currentPageHeight
                )
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

    //<editor-fold desc="获取 pageHeight 限制的方式" defaultstatus="collapsed">
    /**
     * 记录了几个获取 pageHeight 限制的方式，但好像没有一个靠谱的
     * 测试环境：Nexus 5x，Android 8.1
     * 真实限制大小：2048~4096？
     */
    @Suppress("unused")
    companion object {
        val openGlTextureBitmapMaxSize = getMaxTextureSize()

        /**
         * [相关来源](https://stackoverflow.com/questions/15313807/android-maximum-allowed-width-height-of-bitmap)
         */
        private fun getMaximumBitmapHeight() {
            Canvas().maximumBitmapHeight
        }

        /**
         * [相关来源](https://stackoverflow.com/questions/15313807/android-maximum-allowed-width-height-of-bitmap)
         */
        private fun getMaxTextureSize(): Int {
            //Safe minimum default size
            val IMAGE_MAX_BITMAP_DIMENSION = 2048
            //Get EGL Display
            val egl = EGLContext.getEGL() as EGL10
            val display: EGLDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
            //Initialise
            val version = IntArray(2)
            egl.eglInitialize(display, version)
            //Query total number of configurations
            val totalConfigurations = IntArray(1)
            egl.eglGetConfigs(display, null, 0, totalConfigurations)
            //Query actual list configurations
            val configurationsList: Array<EGLConfig?> =
                arrayOfNulls<EGLConfig>(totalConfigurations[0])
            egl.eglGetConfigs(
                display, configurationsList, totalConfigurations[0], totalConfigurations
            )
            val textureSize = IntArray(1)
            var maximumTextureSize = 0
            //Iterate through all the configurations to located the maximum texture size
            for (i in 0 until totalConfigurations[0]) {
                //Only need to check for width since opengl textures are always squared
                egl.eglGetConfigAttrib(
                    display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize
                )
                //Keep track of the maximum texture size
                if (maximumTextureSize < textureSize[0]) maximumTextureSize = textureSize[0]
            }
            //Release
            egl.eglTerminate(display)
            //Return largest texture size found, or default
            return max(maximumTextureSize, IMAGE_MAX_BITMAP_DIMENSION)
        }

        /**
         * [相关来源](https://www.programcreek.com/java-api-examples/?class=android.opengl.GLES10&method=glGetIntegerv)
         */
        fun getSupportedMaxPictureSize(): Int {
            val array = IntArray(1)
            GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, array, 0)
            if (array[0] == 0) {
                GLES11.glGetIntegerv(GLES11.GL_MAX_TEXTURE_SIZE, array, 0)
                if (array[0] == 0) {
                    GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, array, 0)
                    if (array[0] == 0) {
                        GLES30.glGetIntegerv(GLES30.GL_MAX_TEXTURE_SIZE, array, 0)
                    }
                }
            }
            return if (array[0] != 0) array[0] else 2048
        }
    }
    //</editor-fold>
}
