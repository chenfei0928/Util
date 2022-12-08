package io.github.chenfei0928.view.asyncinflater

import android.view.View
import android.view.ViewGroup
import androidx.annotation.EmptySuper
import androidx.annotation.MainThread

/**
 * 基类接口，不对外使用
 * 布局适配载入工具基类接口，提供判断是否为目标View和对View进行数据绑定接口
 *
 * @param VG   目标ViewGroup容器
 * @param Bean 实例类型
 */
interface BasicAdapter<VG : ViewGroup, Bean> {
    @MainThread
    fun isView(view: View): Boolean = true

    @MainThread
    fun onBindView(view: View, bean: Bean)

    @EmptySuper
    fun onAddOrShow(view: View) {
    }

    @EmptySuper
    fun onRemoveOrHide(view: View) {
    }
}
