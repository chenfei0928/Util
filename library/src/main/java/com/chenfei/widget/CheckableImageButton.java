package com.chenfei.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.SoundEffectConstants;

import com.chenfei.lib_libraryhelper.R;

/**
 * @author MrFeng
 * @date 2016/11/14
 */
public class CheckableImageButton extends com.google.android.material.internal.CheckableImageButton {
    private OnCheckedChangeListener mOnCheckedChangeListener;

    public CheckableImageButton(Context context) {
        this(context, null);
    }

    public CheckableImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckableImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CheckableImageButton, defStyleAttr, 0);
        boolean checked = a.getBoolean(R.styleable.CheckableImageButton_android_checked, false);
        a.recycle();
        setChecked(checked);
        setClickable(true);
        setFocusable(true);
    }

    @Override
    public boolean performClick() {
        toggle();

        final boolean handled = super.performClick();
        if (!handled) {
            // View only makes a sound effect if the onClickListener was
            // called, so we'll need to make one here instead.
            playSoundEffect(SoundEffectConstants.CLICK);
        }

        return handled;
    }

    /**
     * <p>Changes the checked state of this button.</p>
     *
     * @param checked true to check the button, false to uncheck it
     */
    @Override
    public void setChecked(boolean checked) {
        if (isChecked() != checked) {
            super.setChecked(checked);
            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged(this, isChecked());
            }
        }
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    /**
     * Interface definition for a callback to be invoked when the checked state of this View is
     * changed.
     */
    public interface OnCheckedChangeListener {

        /**
         * Called when the checked state of a compound button has changed.
         *
         * @param checkableView The view whose state has changed.
         * @param isChecked     The new checked state of checkableView.
         */
        void onCheckedChanged(CheckableImageButton checkableView, boolean isChecked);
    }
}
