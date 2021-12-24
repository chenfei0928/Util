package io.github.chenfei0928.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SoundEffectConstants;

/**
 * Created by MrFeng on 2016/11/14.
 */
public class CheckedTextView extends androidx.appcompat.widget.AppCompatCheckedTextView {
    private OnCheckedChangeListener mOnCheckedChangeListener;

    public CheckedTextView(Context context) {
        this(context, null);
    }

    public CheckedTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
         * @param buttonView The view whose state has changed.
         * @param isChecked  The new checked state of checkableView.
         */
        void onCheckedChanged(CheckedTextView buttonView, boolean isChecked);
    }
}
