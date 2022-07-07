package com.google.android.material.appbar

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * 与refreshLayout绑定，当自身已完全展开时才启用以允许其下拉刷新
 */
fun AppBarLayout.addOnOffsetChangedSwipeRefreshEnable(refresher: SwipeRefreshLayout) {
    addOnOffsetChangedListener { _, verticalOffset ->
        refresher.isEnabled = verticalOffset == 0
    }
}
