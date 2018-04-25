package com.chenfei.library.view;

import android.widget.CompoundButton;

/**
 * 用于直接摆放RadioButton，而不在RadioGroup中时将其设置为一个单选组
 * Created by MrFeng on 2017/8/30.
 */
public class RadioButtonGroupListener<T extends CompoundButton> implements CompoundButton.OnCheckedChangeListener {
    private final T[] mCompoundButtons;
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;
    private OnCheckedListener<T> mOnCheckedListener;
    private boolean mProtectFromCheckedChange;

    @SafeVarargs
    private RadioButtonGroupListener(T... buttons) {
        mCompoundButtons = buttons;
    }

    @SafeVarargs
    public static <T extends CompoundButton> RadioButtonGroupListener<T> bind(T... buttons) {
        RadioButtonGroupListener<T> listener = new RadioButtonGroupListener<>(buttons);
        for (T button : buttons) {
            button.setOnCheckedChangeListener(listener);
        }
        return listener;
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        mOnCheckedChangeListener = onCheckedChangeListener;
    }

    public void setOnCheckedListener(OnCheckedListener<T> onCheckedListener) {
        mOnCheckedListener = onCheckedListener;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // prevents from infinite recursion
        if (mProtectFromCheckedChange) {
            return;
        }

        mProtectFromCheckedChange = true;
        for (T button : mCompoundButtons) {
            if (button != buttonView) {
                button.setChecked(false);
                if (mOnCheckedChangeListener != null) {
                    mOnCheckedChangeListener.onCheckedChanged(button, false);
                }
            } else {
                if (mOnCheckedChangeListener != null) {
                    mOnCheckedChangeListener.onCheckedChanged(button, isChecked);
                }
            }
        }
        if (isChecked && mOnCheckedListener != null) {
            //noinspection unchecked
            mOnCheckedListener.onChecked((T) buttonView);
        }
        mProtectFromCheckedChange = false;
    }

    public interface OnCheckedListener<T extends CompoundButton> {
        void onChecked(T checkedButton);
    }
}
