package io.github.chenfei0928.app

import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.text.InputFilter
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.updateLayoutParams
import io.github.chenfei0928.util.ToastUtil
import org.jetbrains.anko.dip

/**
 * 处理可以输入内容的Dialog
 * Created by MrFeng on 2018/1/24.
 */
class EditorDialogBuilder(
    val context: Context
) {
    val builder = AlertDialog.Builder(context)
    val editor = AppCompatEditText(context)

    /**
     * 设置图标，会显示在title的左边
     */
    fun setIcon(@DrawableRes icon: Int): EditorDialogBuilder {
        builder.setIcon(icon)
        return this
    }

    /**
     * 设置标题
     */
    fun setTitle(@StringRes title: Int): EditorDialogBuilder {
        builder.setTitle(title)
        return this
    }

    /**
     * 设置显示的消息提示
     */
    fun setMessage(text: CharSequence?): EditorDialogBuilder {
        builder.setMessage(text)
        return this
    }

    /**
     * 设置确认按钮
     */
    @JvmOverloads
    inline fun setPositiveButton(
        @StringRes textId: Int,
        @StringRes emptyHint: Int,
        crossinline filter: (String) -> String = { it },
        crossinline listener: (DialogInterface, EditText, String) -> Unit
    ): EditorDialogBuilder {
        return setPositiveButton(textId, DialogInterface.OnClickListener { dialog, _ ->
            // 获取参数
            val code = filter(editor.text.toString())
            if (code.isEmpty()) {
                ToastUtil.showShort(context, emptyHint)
            } else {
                // 隐藏输入法
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(editor.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                editor.clearFocus()
                // 提交
                listener(dialog, editor, code)
            }
        })
    }

    /**
     * 设置确认按钮
     */
    inline fun setPositiveButton(
        @StringRes textId: Int, crossinline listener: (DialogInterface, EditText, Int) -> Unit
    ): EditorDialogBuilder {
        builder.setPositiveButton(textId) { dialog, which ->
            listener(dialog, editor, which)
        }
        return this
    }

    /**
     * 设置确认按钮
     */
    fun setPositiveButton(
        @StringRes textId: Int, listener: DialogInterface.OnClickListener?
    ): EditorDialogBuilder {
        builder.setPositiveButton(textId, listener)
        return this
    }

    /**
     * 设置取消按钮
     */
    fun setNegativeButton(
        @StringRes textId: Int, listener: DialogInterface.OnClickListener?
    ): EditorDialogBuilder {
        builder.setNegativeButton(textId, listener)
        return this
    }

    fun setOnKeyListener(onKeyListener: DialogInterface.OnKeyListener?): EditorDialogBuilder {
        builder.setOnKeyListener(onKeyListener)
        return this
    }

    fun setCancelable(cancelable: Boolean): EditorDialogBuilder {
        builder.setCancelable(cancelable)
        return this
    }

    /**
     * 设置输入框的提示内容
     */
    fun setHint(@StringRes hint: Int): EditorDialogBuilder {
        editor.setHint(hint)
        return this
    }

    /**
     * 设置输入框的提示内容
     */
    fun setHint(hint: CharSequence?): EditorDialogBuilder {
        editor.hint = hint
        return this
    }

    fun setSingleLine(): EditorDialogBuilder {
        editor.setSingleLine()
        return this
    }

    /**
     * 设置输入框的输入类型，适用于输入法的面板
     */
    fun setInputType(type: Int): EditorDialogBuilder {
        editor.inputType = type
        return this
    }

    /**
     * 设置可输入内容的过滤器
     */
    fun setFilters(filters: Array<InputFilter>): EditorDialogBuilder {
        editor.filters = filters
        return this
    }

    private var showListener: (AlertDialog) -> Unit = {}

    fun setOnShowListener(l: (AlertDialog) -> Unit): EditorDialogBuilder {
        showListener = l
        return this
    }

    fun show() {
        val dialog = builder
            .setView(editor)
            .create()
        dialog.setOnShowListener {
            showListener(dialog)
            editor.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                val dp16 = context.dip(16f)
                setMargins(dp16, topMargin, dp16, bottomMargin)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    marginStart = dp16
                    marginEnd = dp16
                }
            }
        }
        dialog.show()
    }
}
