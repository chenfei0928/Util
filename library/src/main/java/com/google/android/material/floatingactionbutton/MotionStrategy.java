package com.google.android.material.floatingactionbutton;

import androidx.annotation.Nullable;

/**
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2021-04-25 15:08
 */
interface MotionLayoutStrategy extends MotionStrategy {
    public abstract void onChange(@Nullable ExtendedFloatingActionLayout.OnChangedCallback callback);
}
