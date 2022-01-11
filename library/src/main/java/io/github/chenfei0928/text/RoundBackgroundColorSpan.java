package io.github.chenfei0928.text;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Px;

/**
 * 背景为圆角纯色的{@link android.text.style.BackgroundColorSpan}
 *
 * @author MrFeng
 * @date 2017/12/28
 * @see <a href="http://blog.csdn.net/zyldzs27/article/details/75091299">原博客</a>
 */
public class RoundBackgroundColorSpan extends ReplacementSpan {
    private final RectF mTmpRectF = new RectF();
    @ColorInt
    private final int bgColor;
    @ColorInt
    private final int textColor;
    // 圆角的半径
    public int radius = 15;
    // 文本内容相对圆角背景水平方向的内边距
    public int paddingHorizontal = 20;
    // 文本内容相对圆角背景垂直方向的内边距
    public int paddingVertical = 1;

    public RoundBackgroundColorSpan(@Px int radius, @ColorInt int bgColor, @ColorInt int textColor) {
        super();
        this.radius = radius;
        this.bgColor = bgColor;
        this.textColor = textColor;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return ((int) paint.measureText(text, start, end) + paddingHorizontal * 2);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text,
                     int start, int end, float x, int top, int y, int bottom,
                     @NonNull Paint paint) {
        int originalColor = paint.getColor();
        paint.setColor(this.bgColor);
        // 设置圆角背景绘制区域
        mTmpRectF.set(x,
                top + paddingVertical,
                x + ((int) paint.measureText(text, start, end) + paddingHorizontal * 2),
                bottom - paddingVertical);
        // 绘制圆角背景
        canvas.drawRoundRect(mTmpRectF, radius, radius, paint);
        paint.setColor(this.textColor);
        // 绘制文字
        canvas.drawText(text, start, end, x + paddingHorizontal, y, paint);
        paint.setColor(originalColor);
    }
}
