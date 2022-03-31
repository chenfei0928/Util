package io.github.chenfei0928.util.glide;

import android.graphics.drawable.Drawable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.request.transition.NoTransition;
import com.bumptech.glide.request.transition.Transition;

/**
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2019-09-09 16:54
 */
public class DrawableCrossFadeFactory extends com.bumptech.glide.request.transition.DrawableCrossFadeFactory {
    private final int duration;
    private DrawableCrossFadeTransition resourceTransition;

    DrawableCrossFadeFactory(int duration) {
        super(duration, true);
        this.duration = duration;
    }

    @Override
    public Transition<Drawable> build(DataSource dataSource, boolean isFirstResource) {
        return dataSource == DataSource.MEMORY_CACHE
                ? NoTransition.<Drawable>get() : getResourceTransition();
    }

    private Transition<Drawable> getResourceTransition() {
        if (resourceTransition == null) {
            resourceTransition = new DrawableCrossFadeTransition(duration, true);
        }
        return resourceTransition;
    }
}
