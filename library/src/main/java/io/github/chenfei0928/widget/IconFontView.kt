package io.github.chenfei0928.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.collection.ArrayMap
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.use
import io.github.chenfei0928.util.MapCache
import io.github.chenfei0928.util.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Created by wangjinpeng on 16/2/1.
 * IconFontView 为了IconFont设置的View，必须设定宽高为固定值，
 * xml使用时必须引用 xmlns:app="http://schemas.android.com/apk/res-auto"
 *
 * 必须引用:
 * - `app:fontAsset` 字体引用的ttf文件路径，目前只能放在assets资源目录下
 * - `android:font` 字体引用的ttf文件路径，API 26或以上可用
 * - `android:textColor` 字体颜色，不传则为黑色
 * - `android:text` 字体内容
 */
open class IconFontView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    var iconFontText = ""
        set(value) {
            field = value
            invalidate()
        }
    var colorListState: ColorStateList? = null
        set(value) {
            field = value
            drawableStateChanged()
        }

    init {
        textPaint.density = resources.displayMetrics.density
        textPaint.textAlign = Paint.Align.CENTER

        context.obtainStyledAttributes(
            attrs, R.styleable.IconFontView, defStyleAttr, defStyleRes
        ).use { a ->
            for (i in 0 until a.indexCount) {
                when (val attr = a.getIndex(i)) {
                    R.styleable.IconFontView_android_text -> {
                        iconFontText = a.getText(attr).toString()
                    }
                    R.styleable.IconFontView_android_font -> {
                        textPaint.typeface =
                            ResourcesCompat.getCachedFont(context, a.getResourceId(attr, 0))
                    }
                    R.styleable.IconFontView_android_textColor -> {
                        colorListState = a.getColorStateList(attr)
                    }
                    R.styleable.IconFontView_fontAsset -> {
                        textPaint.typeface =
                            typefaceCache[getContext().applicationContext][a.getString(attr)!!]
                    }
                }
            }
        }
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        val colorListState = colorListState
        if (colorListState?.isStateful == true) {
            textPaint.color = colorListState.getColorForState(
                drawableState, colorListState.defaultColor
            )
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val padLeft = paddingLeft
        val padRight = paddingLeft
        val padTop = paddingLeft
        val padBottom = paddingLeft

        val padingHor = max(padLeft.toDouble(), padRight.toDouble()).toInt()
        val padingVer = max(padTop.toDouble(), padBottom.toDouble()).toInt()

        val fontWidth = width - padingHor * 2
        val fontHeight = height - padingVer * 2
        // 始终画在中间
        val fontSize = min(fontWidth.toDouble(), fontHeight.toDouble()).toInt()
        textPaint.textSize = fontSize.toFloat()
        val fontMetrics = textPaint.fontMetrics
        canvas.drawText(
            iconFontText,
            (width / 2).toFloat(),
            (height / 2.0 + abs(fontMetrics.ascent.toDouble()) / 2 - fontMetrics.leading * 3 / 4).toFloat(),
            textPaint
        )
    }

    companion object {
        private val typefaceCache =
            MapCache.Basic<Context, MapCache<String, Typeface>> { context ->
                // 其key内存占用可以忽略，主要是value需要weakRef
                MapCache.WeakValue(ArrayMap()) { key ->
                    Typeface.createFromAsset(context.assets, key)
                }
            }
    }
}
