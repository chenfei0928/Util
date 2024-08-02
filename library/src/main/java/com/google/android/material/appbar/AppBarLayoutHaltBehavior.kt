package com.google.android.material.appbar

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.coordinatorlayout.widget.CoordinatorLayout
import io.github.chenfei0928.util.Log
import java.lang.reflect.Field

/**
 * AppBarLayout的快速滚动中的点击停止滚动
 * Created by MrFeng on 2017/11/28.
 */
class AppBarLayoutHaltBehavior : AppBarLayout.Behavior {
    private var mNeedInterceptTouchEvent = false

    constructor()

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        ev: MotionEvent
    ): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            val runnable: Runnable? = try {
                flingRunnableField?.get(this@AppBarLayoutHaltBehavior) as Runnable
            } catch (e: IllegalAccessException) {
                Log.e(TAG, "flingRunnable: ", e)
                null
            }
            // 根据Fling动画Runnable是否存在判断是否是在自动滑动中，如果是则需要拦截此次滑动事件
            mNeedInterceptTouchEvent = runnable != null
            if (runnable != null) {
                child.removeCallbacks(runnable)
                clearFlingRunnable()
            }
        } else if (ev.actionMasked == MotionEvent.ACTION_UP) {
            // 如果在自动滑动中点击的，则拦截此次点击事件
            return if (mNeedInterceptTouchEvent) {
                super.onInterceptTouchEvent(parent, child, ev)
                true
            } else {
                super.onInterceptTouchEvent(parent, child, ev)
            }
        }
        return super.onInterceptTouchEvent(parent, child, ev)
    }

    public override fun onFlingFinished(parent: CoordinatorLayout, layout: AppBarLayout) {
        super.onFlingFinished(parent, layout)
        // 清空该字段，以标记自动滑动完成
        clearFlingRunnable()
    }

    private fun clearFlingRunnable() {
        try {
            flingRunnableField?.set(this, null)
        } catch (e: IllegalAccessException) {
            Log.e(TAG, "clearFlingRunnable: ", e)
        }
    }

    companion object {
        private const val TAG = "KW_AppBarLayoutHaltBeh"

        private val flingRunnableField: Field? by lazy {
            try {
                HeaderBehavior::class.java.getDeclaredField("flingRunnable")
                    .apply { isAccessible = true }
            } catch (e: NoSuchFieldException) {
                Log.e(TAG, "getFlingRunnableField: ", e)
                null
            }
        }
    }
}
