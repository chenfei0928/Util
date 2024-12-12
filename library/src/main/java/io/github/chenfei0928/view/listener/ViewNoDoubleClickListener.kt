package io.github.chenfei0928.view.listener

import android.view.View
import android.view.ViewConfiguration
import androidx.core.view.doOnDetach
import java.util.WeakHashMap

/**
 * 防双击点击监听器
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-12-26 11:22
 */
private object ViewNoDoubleClickListener : View.OnClickListener {
    val map = WeakHashMap<View, ViewOnClickInfo>()

    override fun onClick(v: View?) {
        val viewOnClickInfo = map[v]
            ?: return
        val currentTimeMillis = System.currentTimeMillis()
        // 如果两次点击间隔时间小于指定间隔之间，不处理
        if (currentTimeMillis - viewOnClickInfo.lastClickTime < viewOnClickInfo.doubleTapTimeout) {
            return
        }
        // 记录点击时间
        viewOnClickInfo.lastClickTime = currentTimeMillis
        // 回调点击事件
        viewOnClickInfo.onClickLis.onClick(v)
    }

    fun expungeStaleEntries() {
        map.size
    }
}

private data class ViewOnClickInfo(
    val doubleTapTimeout: Int,
    val onClickLis: View.OnClickListener,
    var lastClickTime: Long = 0
)

fun View.setNoDoubleOnClickListener(
    doubleTapTimeout: Int = ViewConfiguration.getDoubleTapTimeout(),
    l: View.OnClickListener
) {
    // 存储view与监听器的映射关系
    ViewNoDoubleClickListener.map[this] = ViewOnClickInfo(doubleTapTimeout, l)
    // 将防双击监听器设置到view
    setOnClickListener(ViewNoDoubleClickListener)
    // 当view从布局中移除时自动清理过时条目
    doOnDetach {
        ViewNoDoubleClickListener.expungeStaleEntries()
    }
}
