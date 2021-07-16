package com.chenfei.text;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;

public class TopGravitySpan extends ReplacementSpan {

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return (int) paint.measureText(text, start, end);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        // 此行顶端坐标减去上文字顶端距离基线距离（负数）
        // y参数传入的是要绘制文字的基线baseline
        canvas.drawText(text, start, end,
                x, top - paint.ascent(),
                paint);
    }
}
