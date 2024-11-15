package io.github.chenfei0928.view

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.view.ViewParent
import androidx.annotation.ColorInt
import androidx.appcompat.R

/**
 * View设置工具类
 *
 * @author Admin
 * @date 2015/8/19
 */
object ViewUtil {
    @ColorInt
    fun getThemeColor(context: Context): Int {
        // 由于Service没有主题，只能获取ApplicationInfo中的主题id，并根据其获取主题色
        return context.obtainStyledAttributes(
            context.applicationInfo.theme,
            intArrayOf(R.attr.colorPrimary)
        ).use {
            it.getColor(0, Color.BLACK)
        }
    }

    @ColorInt
    fun getColorControlHighlight(context: Context): Int {
        // 由于Service没有主题，只能获取ApplicationInfo中的主题id，并根据其获取主题色
        return context.obtainStyledAttributes(
            context.applicationInfo.theme,
            intArrayOf(android.R.attr.colorControlHighlight)
        ).use {
            it.getColor(0, 0x1a000000)
        }
    }

    /**
     * 获得一个view相对于其某个父view层级的坐标
     *
     * @param target 子view
     * @param parent 父view
     * @return 相对坐标[x, y]
     */
    fun getLocationOnViewParent(target: View, parent: ViewParent): FloatArray {
        var x = 0f
        var y = 0f
        var viewParent = target
        while (viewParent !== parent) {
            x += viewParent.x
            y += viewParent.y
            viewParent = viewParent.parent as View
        }
        return floatArrayOf(x, y)
    }

    /**
     * 计算指定的 View 在屏幕中的坐标。
     */
    fun calcViewScreenLocation(view: View): RectF {
        val location = IntArray(2)
        // 获取控件在屏幕中的位置，返回的数组分别为控件左顶点的 x、y 的值
        view.getLocationOnScreen(location)
        return RectF(
            location[0].toFloat(),
            location[1].toFloat(),
            (location[0] + view.width).toFloat(),
            (location[1] + view.height).toFloat()
        )
    }

    fun isMotionEventInView(view: View, ev: MotionEvent): Boolean {
        return calcViewScreenLocation(view).contains(ev.rawX, ev.rawY)
    }

    private fun getViewIdResString(v: View): String {
        val out = StringBuilder()
        val id = v.id
        if (id != View.NO_ID) {
            out.append(" #")
            out.append(Integer.toHexString(id))
            val r = v.context.resources
            try {
                val pkgName = when (id and -0x1000000) {
                    0x7f000000 -> "app"
                    0x01000000 -> "android"
                    else -> r.getResourcePackageName(id)
                }
                val typename = r.getResourceTypeName(id)
                val entryName = r.getResourceEntryName(id)
                out.append(" ")
                out.append(pkgName)
                out.append(":")
                out.append(typename)
                out.append("/")
                out.append(entryName)
            } catch (_: Resources.NotFoundException) {
                out.append(v)
            }
        }
        return out.toString()
    }
}
