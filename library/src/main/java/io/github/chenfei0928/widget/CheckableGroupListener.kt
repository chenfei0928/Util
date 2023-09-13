package io.github.chenfei0928.widget

import android.view.View
import android.widget.Checkable
import android.widget.CompoundButton
import android.widget.RadioGroup
import java.util.*

/**
 * checkable控件组的单选监听器（用于实现类似于[RadioGroup]的单选效果）。
 * 部分实现代码也来自于其内部实现[RadioGroup.mChildOnCheckedChangeListener]，
 * 并提供可以实现[RadioGroup.setOnCheckedChangeListener]的接口[onCheckedListener]来回调已选中的控件。
 *
 * 由于不同类型控件监听器实现的不同，故在类自身实现只提供切换选择时取消组内其他控件选择状态的操作，
 * 然后再由[asRadioButtonListener]等提供对应类型控件的监听器实现。
 * 由于代理了其监听器，故提供了类似于[android.widget.RadioButton.setOnCheckedChangeListener]的接口
 * [onCheckedChangeListener]用于监听单个选中的点击状态变化监听。
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-10-29 11:34
 */
open class CheckableGroupListener<T : Checkable>(
    val views: List<T>
) {

    constructor(views: Array<T>) : this(views.toList())
    constructor(views: Sequence<T>) : this(views.toList())

    var checked: T?
        get() = views.find { it.isChecked }
        set(value) {
            views.forEach {
                it.isChecked = it === value
            }
        }

    var checkedViewId: Int
        get() = (checked as? View)?.id ?: View.NO_ID
        set(value) {
            views.forEach {
                it.isChecked = (it as? View)?.id == value
            }
        }

    private var protectFromCheckedChange: Boolean = false

    /**
     * 隔离每个view的监听器，用于每个view的坚挺处理不一样的情况
     */
    private val isolateOnCheckChangedListenerMap: WeakHashMap<T, Function2<T, Boolean, Unit>> =
        WeakHashMap()

    /**
     * 单个控件选中状态变化的监听器
     */
    var onCheckedChangeListener: (view: T, isChecked: Boolean) -> Unit = { _, _ -> }

    /**
     * 已选中的控件监听，会传入被选中的控件
     */
    var onCheckedListener: (view: T) -> Unit = { }

    fun setIsolateOnCheckChangedListener(
        view: T, onCheckedChangeListener: (view: T, isChecked: Boolean) -> Unit
    ) {
        isolateOnCheckChangedListenerMap[view] = onCheckedChangeListener
    }

    fun onCheckedChangedImpl(buttonView: T, isChecked: Boolean) {
        // prevents from infinite recursion
        if (protectFromCheckedChange) {
            return
        }

        protectFromCheckedChange = true
        for (button in views) {
            if (button !== buttonView) {
                button.isChecked = false
                onCheckedChangeListener(button, false)
                isolateOnCheckChangedListenerMap[button]?.invoke(button, false)
            } else {
                onCheckedChangeListener(button, isChecked)
                isolateOnCheckChangedListenerMap[button]?.invoke(button, isChecked)
            }
        }
        if (isChecked) {
            onCheckedListener(buttonView)
        }
        protectFromCheckedChange = false
    }

    val asRadioButtonListener: CompoundButton.OnCheckedChangeListener by lazy(LazyThreadSafetyMode.NONE) {
        return@lazy CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            onCheckedChangedImpl(buttonView as T, isChecked)
        }
    }
}
