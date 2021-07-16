package com.chenfei.app

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Message
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import com.chenfei.library.R
import java.text.NumberFormat

/**
 * @author MrFeng
 * @date 2017/10/31
 */
open class ProgressDialog(
    context: Context
) : AppCompatDialog(context) {
    private var mProgressStyle = STYLE_SPINNER
    private var mProgressNumber: TextView? = null
    private var mProgressNumberFormat: String? = null
    private var mProgressPercent: TextView? = null
    private var mProgressPercentFormat: NumberFormat? = null

    private var mProgress: ProgressBar? = null
    private var mMessageView: TextView? = null

    private var mMessage: CharSequence? = context.getString(R.string.loading)

    var isIndeterminate: Boolean = false
        /**
         * Whether this ProgressDialog is in indeterminate mode.
         *
         * @return true if the dialog is in indeterminate mode, false otherwise
         */
        get() = mProgress?.isIndeterminate ?: field
        /**
         * Change the indeterminate mode for this ProgressDialog. In indeterminate
         * mode, the progress is ignored and the dialog shows an infinite
         * animation instead.
         *
         * **Note:** A ProgressDialog with style [.STYLE_SPINNER]
         * is always indeterminate and will ignore this setting.
         *
         * @param indeterminate true to enable indeterminate mode, false otherwise
         * @see .setProgressStyle
         */
        set(indeterminate) = if (mProgress != null) {
            mProgress!!.isIndeterminate = indeterminate
        } else {
            field = indeterminate
        }

    var progress: Int = 0
        /**
         * Gets the current progress.
         *
         * @return the current progress, a value between 0 and [.getMax]
         */
        get() = mProgress?.progress ?: field
        /**
         * Sets the current progress.
         *
         * @param value the current progress, a value between 0 and [.getMax]
         * @see ProgressBar.setProgress
         */
        set(value) = if (mProgress != null) {
            mProgress!!.progress = value
            onProgressChanged()
        } else {
            field = value
        }

    var secondaryProgress: Int = 0
        /**
         * Gets the current secondary progress.
         *
         * @return the current secondary progress, a value between 0 and [.getMax]
         */
        get() = mProgress?.secondaryProgress ?: field
        /**
         * Sets the secondary progress.
         *
         * @param secondaryProgress the current secondary progress, a value between 0 and
         * [.getMax]
         * @see ProgressBar.setSecondaryProgress
         */
        set(secondaryProgress) = if (mProgress != null) {
            mProgress!!.secondaryProgress = secondaryProgress
            onProgressChanged()
        } else {
            field = secondaryProgress
        }

    var max: Int = 0
        /**
         * Gets the maximum allowed progress value. The default value is 100.
         *
         * @return the maximum value
         */
        get() = mProgress?.max ?: field
        /**
         * Sets the maximum allowed progress value.
         */
        set(max) = if (mProgress != null) {
            mProgress!!.max = max
            onProgressChanged()
        } else {
            field = max
        }

    init {
        initFormats()
        super.setOnDismissListener {
            dismissListener?.onDismiss(this)
            internalDismissListener?.onDismiss(this)
            dismissMessage?.let {
                Message
                    .obtain(it)
                    .sendToTarget()
            }
        }
        super.setOnShowListener {
            showListener?.onShow(this)
            internalShowListener?.onShow(this)
        }
    }

    private fun initFormats() {
        mProgressNumberFormat = "%1d/%2d"
        mProgressPercentFormat = NumberFormat
            .getPercentInstance()
            .apply {
                maximumFractionDigits = 0
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (mProgressStyle == STYLE_HORIZONTAL) {
            setContentView(R.layout.alert_progress_dialog)
            mProgressNumber = findViewById(R.id.progress_number)
            mProgressPercent = findViewById(R.id.progress_percent)
        } else {
            setContentView(R.layout.dialog_progress)
        }
        mProgress = findViewById(android.R.id.progress)
        mMessageView = findViewById(R.id.message)

        if (max > 0) {
            max = max
        }
        if (progress > 0) {
            progress = progress
        }
        if (secondaryProgress > 0) {
            secondaryProgress = secondaryProgress
        }
        if (mMessage != null) {
            setMessage(mMessage)
        }
    }

    /**
     * Sets the style of this ProgressDialog, either [.STYLE_SPINNER] or
     * [.STYLE_HORIZONTAL]. The default is [.STYLE_SPINNER].
     *
     * **Note:** A ProgressDialog with style [.STYLE_SPINNER]
     * is always indeterminate and will ignore the [ indeterminate][.setIndeterminate] setting.
     *
     * @param style the style of this ProgressDialog, either [.STYLE_SPINNER] or
     * [.STYLE_HORIZONTAL]
     */
    fun setProgressStyle(style: Int) {
        mProgressStyle = style
    }

    fun setMessage(message: CharSequence?) {
        if (mMessageView != null) {
            mMessageView!!.text = message
        } else {
            mMessage = message
        }
    }

    private fun onProgressChanged() {
        /* Update the number and percent */
        val format = mProgressNumberFormat
        mProgressNumber?.text = if (format != null) {
            String.format(format, progress, max)
        } else {
            ""
        }

        val mProgressPercentFormat = mProgressPercentFormat
        mProgressPercent?.text = if (mProgressPercentFormat != null) {
            val percent = progress.toDouble() / max.toDouble()
            val tmp = SpannableString(mProgressPercentFormat.format(percent))
            tmp.setSpan(
                StyleSpan(android.graphics.Typeface.BOLD),
                0,
                tmp.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tmp
        } else {
            ""
        }
    }

    private var showListener: DialogInterface.OnShowListener? = null
    internal var internalShowListener: DialogInterface.OnShowListener? = null
    private var dismissMessage: Message? = null
    private var dismissListener: DialogInterface.OnDismissListener? = null
    internal var internalDismissListener: DialogInterface.OnDismissListener? = null

    override fun setDismissMessage(msg: Message?) {
        dismissMessage = msg
        dismissListener = null
    }

    override fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        dismissMessage = null
        dismissListener = listener
    }

    override fun setOnShowListener(listener: DialogInterface.OnShowListener?) {
        showListener = listener
    }

    companion object {
        /**
         * Creates a ProgressDialog with a circular, spinning progress
         * bar. This is the default.
         */
        const val STYLE_SPINNER = 0

        /**
         * Creates a ProgressDialog with a horizontal progress bar.
         */
        const val STYLE_HORIZONTAL = 1
    }
}
