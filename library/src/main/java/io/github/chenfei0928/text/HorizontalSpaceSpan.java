package io.github.chenfei0928.text;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by MrFeng on 2017/6/1.
 */
public class HorizontalSpaceSpan extends ReplacementSpan {
    private final int mHorizontalSpace;

    public HorizontalSpaceSpan(int space) {
        mHorizontalSpace = space;
    }


    @Override
    public int getSize(@NonNull Paint paint, CharSequence text,
                       @IntRange(from = 0) int start, @IntRange(from = 0) int end,
                       @Nullable Paint.FontMetricsInt fm) {
        return mHorizontalSpace;
    }

    @Override
    public void draw(@NonNull Canvas canvas,
                     CharSequence text, @IntRange(from = 0) int start, @IntRange(from = 0) int end,
                     float x, int top, int y, int bottom,
                     @NonNull Paint paint) {
    }
}
