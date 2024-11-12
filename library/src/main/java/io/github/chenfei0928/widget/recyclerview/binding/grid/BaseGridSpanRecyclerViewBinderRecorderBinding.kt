package io.github.chenfei0928.widget.recyclerview.binding.grid

import androidx.recyclerview.widget.GridLayoutManager

/**
 * 提供bean类型映射保存的 [AbsGridSpanRecyclerViewBinding]，
 * 用于同一种Bean要对应多种layout的使用场景，扩展由[GridLayoutManager]支持。
 *
 * 对[AbsGridSpanRecyclerViewBinding]进行了添加 一bean类型对多binder支持，
 * 建议用于[GridLayoutManager]、一bean类型对多binder场景。
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-11-19 20:29
 */
typealias BaseGridSpanRecyclerViewBinderRecorderBinding =
        AbsGridSpanRecyclerViewBinderRecorderBinding<AbsGridSpanRecyclerViewBinderRecorderBinding.LayoutParams<*>>
