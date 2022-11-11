package io.github.chenfei0928.widget.recyclerview.binder

import androidx.annotation.CallSuper
import androidx.databinding.Observable
import io.github.chenfei0928.collection.mapToArray
import io.github.chenfei0928.util.R
import io.github.chenfei0928.util.createOnPropertyChanged
import io.github.chenfei0928.widget.recyclerview.ViewHolderTagValDelegate
import io.github.chenfei0928.widget.recyclerview.adapter.ViewHolder

/**
 * 带有一个环境参数的view更新实现
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-06 13:58
 */
abstract class BaseEnvironmentTwoWayLayoutBinder<Bean, VH : ViewHolder<Bean>>(
    private val environments: Array<Observable>,
) : TwoWayLayoutBinder<Bean, VH>() {

    @CallSuper
    override fun bindTwoWayCallback(holder: VH) {
        if (environments.isEmpty()) {
            return
        }
        // 监听环境变化
        holder.onEnvironmentChanged.forEachIndexed { index, it ->
            environments[index].addOnPropertyChangedCallback(it)
        }
    }

    @CallSuper
    override fun unbindTwoWayCallback(holder: VH) {
        if (environments.isEmpty()) {
            return
        }
        // 取消监听环境变化
        holder.onEnvironmentChanged.forEachIndexed { index, it ->
            environments[index].removeOnPropertyChangedCallback(it)
        }
    }

    /**
     * View与切环境变化的监听
     */
    private val VH.onEnvironmentChanged: Array<Observable.OnPropertyChangedCallback>
            by ViewHolderTagValDelegate(R.id.onPropertyChanged) { holder ->
                if (environments.isEmpty()) {
                    emptyArray()
                } else environments.mapToArray {
                    it.createOnPropertyChanged {
                        syncBeanChanged(holder, this, it)
                    }
                }
            }
}
