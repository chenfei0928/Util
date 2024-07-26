package io.github.chenfei0928.util.glide;

import android.graphics.drawable.Drawable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.request.transition.NoTransition;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.request.transition.TransitionFactory;

/**
 * {@link com.bumptech.glide.request.transition.DrawableCrossFadeFactory}
 *
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2019-09-09 16:54
 */
public class DrawableCrossFadeFactory implements TransitionFactory<Drawable> {
    private final int duration;
    private final boolean isCrossFadeEnabled;
    private DrawableCrossFadeTransition resourceTransition;

    public DrawableCrossFadeFactory(int duration, boolean isCrossFadeEnabled) {
        this.duration = duration;
        this.isCrossFadeEnabled = isCrossFadeEnabled;
    }

    @Override
    public Transition<Drawable> build(DataSource dataSource, boolean isFirstResource) {
        return dataSource == DataSource.MEMORY_CACHE
                ? NoTransition.<Drawable>get() : getResourceTransition();
    }

    private Transition<Drawable> getResourceTransition() {
        if (resourceTransition == null) {
            resourceTransition = new DrawableCrossFadeTransition(duration, isCrossFadeEnabled);
        }
        return resourceTransition;
    }
}
