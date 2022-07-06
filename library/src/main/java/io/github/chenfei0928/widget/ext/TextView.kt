/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-07-16 10:03
 */
package io.github.chenfei0928.widget.ext

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Build
import android.text.TextPaint
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.annotation.Size
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.PrecomputedTextCompat
import androidx.core.view.doOnLayout
import androidx.core.widget.TextViewCompat
import io.github.chenfei0928.concurrent.ExecutorUtil
import io.github.chenfei0928.util.contains
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun TextView.isEmpty() = length() <= 0

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
fun TextView.setShrinkText(text: String, lastLinePadding: Int) =
    setShrinkText(text, lastLinePadding, maxLines)

fun TextView.setShrinkText(text: String, lastLinePadding: Int, maxLines: Int) {
    doOnLayout {
        setShrinkTextInternal(text, lastLinePadding, maxLines)
    }
}

/**
 * 设置最后一行距离指定宽度截断的text，用于ui展现“查看全部”等
 */
@SuppressLint("SetTextI18n")
private fun TextView.setShrinkTextInternal(text: String, lastLinePadding: Int, maxLines: Int) {
    this.text = text
    val paint = paint
    val layout = layout
    // 将layout的宽度减去最后一行的保护边距、省略号宽度，获得目标宽度（最后一行不要超过多宽）
    val targetWidth = layout.width - lastLinePadding - paint.measureText("...")
    // 最后一行开始的字符下标
    val lineStart = layout.getLineStart(maxLines - 1)
    // 检查多少字可以在该行空下对应宽度
    val indexEndTrimmedRevised =
        binaryMeasureTextSafeWidthForTextEndIndex(paint, text, lineStart, targetWidth)
    this.text = text.substring(0, indexEndTrimmedRevised) + "..."
}

/**
 * 使用二分法对文字绘制后的宽度进行测量，以限制到一个安全宽度内
 *
 * @param paint     TextView的画笔，用于测量指定文字绘制后的宽度
 * @param text      要进行绘制的目标文字
 * @param lineStart 开始测量的第一个字符下标索引
 * @param safeWidth 目标安全宽度，要求目标文字绘制完成后不要超过该宽度
 */
private fun binaryMeasureTextSafeWidthForTextEndIndex(
    paint: TextPaint,
    text: String,
    lineStart: Int,
    safeWidth: Float
): Int {
    if (lineStart >= text.length)
        return text.length

    var lo = lineStart
    var hi = text.length - 1

    while (lo < hi) {
        val mid = (lo + hi) / 2
        if (mid == lo) {
            return mid
        }
        val midVar = paint.measureText(text, lineStart, mid)

        if (midVar == safeWidth) {
            return mid
        } else if (midVar > safeWidth) {
            hi = mid - 1
        } else {
            lo = mid
        }
    }
    return lo
}

/**
 * 将耗时的文字测量操作放到异步执行，适用于在列表中显示大量文字时的优化
 * 需开启RecyclerView的LayoutManager预取：[androidx.recyclerview.widget.RecyclerView.LayoutManager.setItemPrefetchEnabled]
 * 并要求LayoutManager实现：[androidx.recyclerview.widget.RecyclerView.LayoutManager.collectAdjacentPrefetchPositions]
 * 不要在调用此方法后设置TextView属性
 */
fun AppCompatTextView.setTextFuture(@Size(min = 200) text: CharSequence) {
    setTextFuture(
        PrecomputedTextCompat.getTextFuture(
            text,
            TextViewCompat.getTextMetricsParams(this),
            ExecutorUtil
        )
    )
}

// 删除线
var TextView.strikeThroughText by TextViewPaintDelegate(Paint.STRIKE_THRU_TEXT_FLAG)

// 下划线
var TextView.underLineText by TextViewPaintDelegate(Paint.UNDERLINE_TEXT_FLAG)

private class TextViewPaintDelegate(private val flag: Int) : ReadWriteProperty<TextView, Boolean> {

    override fun getValue(thisRef: TextView, property: KProperty<*>): Boolean {
        return flag in thisRef.paintFlags
    }

    override fun setValue(thisRef: TextView, property: KProperty<*>, value: Boolean) {
        thisRef.paintFlags = if (value) {
            // 对paintFlags或操作flag，将其位置1
            thisRef.paintFlags or flag
        } else {
            // 将flag取反后对paintFlags与操作，将其位置0
            thisRef.paintFlags and flag.inv()
        }
        thisRef.invalidate()
    }
}
