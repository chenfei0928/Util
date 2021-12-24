package com.google.android.material.appbar

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.MenuItem
import androidx.annotation.ColorInt
import androidx.appcompat.R
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import io.github.chenfei0928.util.kotlin.blendColors

/**
 * 根据AppBarLayout收起进度来调整ToolBar返回箭头的着色，类似[CollapsingToolbarLayout]Title效果
 * 部分逻辑参考 [CollapsingToolbarLayout.OffsetUpdateListener]
 *
 * Created by MrFeng on 2017/7/19.
 */
open class CollapsingToolbarBackListener(
    collapsingToolbarLayout: CollapsingToolbarLayout,
    protected val actionBar: ActionBar,
    @SuppressLint("PrivateResource") drawableIcon: Int = R.drawable.abc_ic_ab_back_material
) : FractionOffsetChangedListener(collapsingToolbarLayout) {
    protected val backIcon: Drawable = ContextCompat
        .getDrawable(actionBar.themedContext, drawableIcon)!!
        .mutate()
    private var menus: Array<MenuItem> = arrayOf()

    // 收起之后的颜色
    @ColorInt
    var collapsedColor = Color.BLACK
        set(value) {
            field = value
            syncActionBarColor(getColor())
        }

    // 展开之后的颜色
    @ColorInt
    var expandedColor = Color.WHITE
        set(value) {
            field = value
            syncActionBarColor(getColor())
        }

    @ColorInt
    private fun getColor() = blendColors(expandedColor, collapsedColor, expandedFraction)

    /**
     * 设置菜单图标着色，将会给返回按钮、菜单图标进行着色
     */
    open fun syncActionBarColor(@ColorInt color: Int) {
        DrawableCompat.setTint(backIcon, color)
        actionBar.setHomeAsUpIndicator(backIcon)
        menus.forEach { DrawableCompat.setTint(it.icon, color) }
    }

    /**
     * 设置需要进行自动着色的菜单项，在[android.app.Activity.onCreateOptionsMenu]创建完成之后
     * 获取需要进行跟随工具栏折叠状态进行着色的菜单项后调用此方法进行设置需要着色的菜单项。
     * 此方法只接受有设置icon的菜单项
     */
    fun addMenus(vararg items: MenuItem) {
        items.forEach {
            addMenu(it)
        }
    }

    /**
     * 追加设置需要进行自动着色的菜单项，在[android.app.Activity.onCreateOptionsMenu]创建完成之后
     * 获取需要进行跟随工具栏折叠状态进行着色的菜单项后调用此方法进行设置需要着色的菜单项。
     * 此方法只接受有设置icon的菜单项
     */
    fun addMenu(item: MenuItem?) {
        if (item?.icon == null) {
            return
        }
        this.menus += item
        // 将其icon再此处保留变化并添加着色支持，不影响到其它位置的类似图标
        item.icon = DrawableCompat.wrap(item.icon.mutate())
        // 着色
        DrawableCompat.setTint(item.icon, getColor())
    }

    /**
     * 子类重写该方法对折叠工具栏折叠进度的ui交互
     *
     * @param expandedFraction 展开进度，0为已完全展开[expandedColor]，1为已完全收起[collapsedColor]
     */
    override fun onExpandedFractionChanged(expandedFraction: Float) {
        syncActionBarColor(getColor())
    }
}
