package io.github.chenfei0928.util.recyclerview.grid

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 提供可以用span处理的[RecyclerView]列表填充框架（多span[GridLayoutManager]）。
 * 建议用于[GridLayoutManager]、无需一bean类型对多binder的场景
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-17 19:07
 */
typealias BaseGridSpanRecyclerViewBinding = AbsGridSpanRecyclerViewBinding<AbsGridSpanRecyclerViewBinding.LayoutParams>
