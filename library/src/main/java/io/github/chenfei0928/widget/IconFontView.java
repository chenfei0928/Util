package io.github.chenfei0928.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import io.github.chenfei0928.util.R;

/**
 * Created by wangjinpeng on 16/2/1.
 * IconFontView 为了IconFont设置的View，必须设定宽高为固定值，
 * xml使用时必须引用 xmlns:app="http://schemas.android.com/apk/res-auto"
 * 必须引用
 * app:fontAsset  -- 字体引用的ttf文件路径，目前只能放在assets资源目录下
 * android:textColor  -- 字体颜色，不传则为黑色
 * android:text -- 字体内容
 */
public class IconFontView extends View {
    private final TextPaint mTextPaint;
    private String mIconFontText;

    public IconFontView(Context context) {
        this(context, null, 0, 0);
    }

    public IconFontView(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public IconFontView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public IconFontView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mIconFontText = "";
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.density = getResources().getDisplayMetrics().density;
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        if (attrs != null) {
            ColorStateList textColor = null;
            String fontAsset = null;

            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconFontView, defStyleAttr, defStyleRes);

            int n = a.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = a.getIndex(i);
                if (attr == R.styleable.IconFontView_android_text) {
                    mIconFontText = a.getText(attr).toString();
                } else if (attr == R.styleable.IconFontView_android_textColor) {
                    textColor = a.getColorStateList(attr);
                } else if (attr == R.styleable.IconFontView_fontAsset) {
                    fontAsset = a.getString(attr);
                }
            }
            a.recycle();

            Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), fontAsset);
            if (typeface != null) {
                mTextPaint.setTypeface(typeface);
            }
            if (textColor != null) {
                mTextPaint.setColor(textColor.getDefaultColor());
            } else {
                mTextPaint.setColor(Color.BLACK);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int padLeft = getPaddingLeft();
        int padRight = getPaddingLeft();
        int padTop = getPaddingLeft();
        int padBottom = getPaddingLeft();

        int padingHor = padLeft > padRight ? padLeft : padRight;
        int padingVer = padTop > padBottom ? padTop : padBottom;

        int fontWidth = getWidth() - padingHor * 2;
        int fontHeight = getHeight() - padingVer * 2;
        //始终画在中间
        int fontSize = fontWidth > fontHeight ? fontHeight : fontWidth;
        mTextPaint.setTextSize(fontSize);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        int x = getWidth() / 2;
        int y = (int) (getHeight() / 2 + Math.abs(fontMetrics.ascent) / 2 - fontMetrics.leading * 3 / 4);
        canvas.drawText(mIconFontText, x, y, mTextPaint);
    }
}
