package com.chenfei.widget

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet

/**
 * 修复该控件先设置checked，再设置text失效的bug
 * Created by MrFeng on 2018/5/31.
 */
class ToggleButton : android.widget.ToggleButton {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun setTextOff(textOff: CharSequence?) {
        super.setTextOff(textOff)
        syncTextState()
    }

    override fun setTextOn(textOn: CharSequence?) {
        super.setTextOn(textOn)
        syncTextState()
    }

    private fun syncTextState() {
        val checked = isChecked
        if (checked && textOn != null) {
            text = textOn
        } else if (!checked && textOff != null) {
            text = textOff
        }
    }
}
