package io.github.chenfei0928.app

import android.content.Context
import android.content.DialogInterface
import android.text.InputFilter
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.annotation.ReturnThis
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.getSystemService
import androidx.core.view.updateLayoutParams
import io.github.chenfei0928.widget.ToastUtil
import org.jetbrains.anko.dip

/**
 * 处理可以输入内容的Dialog
 *
 * @author MrFeng
 * @date 2018/1/24.
 */
class EditorDialogBuilder(
    val context: Context
) {
    val builder = AlertDialog.Builder(context)
    val editor = AppCompatEditText(context)

    /**
     * 设置图标，会显示在title的左边
     */
    @ReturnThis
    fun setIcon(@DrawableRes icon: Int): EditorDialogBuilder {
        builder.setIcon(icon)
        return this
    }

    /**
     * 设置标题
     */
    @ReturnThis
    fun setTitle(@StringRes title: Int): EditorDialogBuilder {
        builder.setTitle(title)
        return this
    }

    /**
     * 设置显示的消息提示
     */
    @ReturnThis
    fun setMessage(text: CharSequence?): EditorDialogBuilder {
        builder.setMessage(text)
        return this
    }

    /**
     * 设置确认按钮
     */
    @ReturnThis
    @JvmOverloads
    inline fun setPositiveButton(
        @StringRes textId: Int,
        @StringRes emptyHint: Int,
        crossinline filter: (String) -> String = { it },
        crossinline listener: (DialogInterface, EditText, String) -> Unit
    ): EditorDialogBuilder {
        setPositiveButton(textId) { dialog, _ ->
            // 获取参数
            val code = filter(editor.text.toString())
            if (code.isEmpty()) {
                ToastUtil.showShort(context, emptyHint)
            } else {
                // 隐藏输入法
                context.getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(
                    editor.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
                )
                editor.clearFocus()
                // 提交
                listener(dialog, editor, code)
            }
        }
        return this
    }

    /**
     * 设置确认按钮
     */
    @ReturnThis
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
    @ReturnThis
    fun setPositiveButton(
        @StringRes textId: Int, listener: DialogInterface.OnClickListener?
    ): EditorDialogBuilder {
        builder.setPositiveButton(textId, listener)
        return this
    }

    /**
     * 设置取消按钮
     */
    @ReturnThis
    fun setNegativeButton(
        @StringRes textId: Int, listener: DialogInterface.OnClickListener?
    ): EditorDialogBuilder {
        builder.setNegativeButton(textId, listener)
        return this
    }

    @ReturnThis
    fun setOnKeyListener(onKeyListener: DialogInterface.OnKeyListener?): EditorDialogBuilder {
        builder.setOnKeyListener(onKeyListener)
        return this
    }

    @ReturnThis
    fun setCancelable(cancelable: Boolean): EditorDialogBuilder {
        builder.setCancelable(cancelable)
        return this
    }

    /**
     * 设置输入框的提示内容
     */
    @ReturnThis
    fun setHint(@StringRes hint: Int): EditorDialogBuilder {
        editor.setHint(hint)
        return this
    }

    /**
     * 设置输入框的提示内容
     */
    @ReturnThis
    fun setHint(hint: CharSequence?): EditorDialogBuilder {
        editor.hint = hint
        return this
    }

    @ReturnThis
    fun setSingleLine(): EditorDialogBuilder {
        editor.setSingleLine()
        return this
    }

    /**
     * 设置输入框的输入类型，适用于输入法的面板
     */
    @ReturnThis
    fun setInputType(type: Int): EditorDialogBuilder {
        editor.inputType = type
        return this
    }

    /**
     * 设置可输入内容的过滤器
     */
    @ReturnThis
    fun setFilters(filters: Array<InputFilter>): EditorDialogBuilder {
        editor.filters = filters
        return this
    }

    private var showListener: (AlertDialog) -> Unit = {}

    @ReturnThis
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
                marginStart = dp16
                marginEnd = dp16
            }
        }
        dialog.show()
    }
}
