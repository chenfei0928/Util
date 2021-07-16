package com.google.android.material.floatingactionbutton;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import com.google.android.material.animation.AnimatorSetCompat;
import com.google.android.material.animation.MotionSpec;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Preconditions;

/**
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2021-04-25 15:00
 */
abstract class BaseMotionLayoutStrategy implements MotionLayoutStrategy {

    private final Context context;
    @NonNull
    private final ExtendedFloatingActionLayout fab;
    private final ArrayList<Animator.AnimatorListener> listeners = new ArrayList<>();
    private final AnimatorTracker tracker;

    @Nullable
    private MotionSpec defaultMotionSpec;
    @Nullable
    private MotionSpec motionSpec;

    BaseMotionLayoutStrategy(@NonNull ExtendedFloatingActionLayout fab, AnimatorTracker tracker) {
        this.fab = fab;
        this.context = fab.getContext();
        this.tracker = tracker;
    }

    @Override
    public final void setMotionSpec(@Nullable MotionSpec motionSpec) {
        this.motionSpec = motionSpec;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public final MotionSpec getCurrentMotionSpec() {
        if (motionSpec != null) {
            return motionSpec;
        }

        if (defaultMotionSpec == null) {
            defaultMotionSpec = MotionSpec.createFromResource(context, getDefaultMotionSpecResource());
        }

        return Preconditions.checkNotNull(defaultMotionSpec);
    }

    @Override
    public final void addAnimationListener(@NonNull Animator.AnimatorListener listener) {
        listeners.add(listener);
    }

    @Override
    public final void removeAnimationListener(@NonNull Animator.AnimatorListener listener) {
        listeners.remove(listener);
    }

    @NonNull
    @Override
    public final List<Animator.AnimatorListener> getListeners() {
        return listeners;
    }

    @Override
    @Nullable
    public MotionSpec getMotionSpec() {
        return motionSpec;
    }

    @Override
    @CallSuper
    public void onAnimationStart(Animator animator) {
        tracker.onNextAnimationStart(animator);
    }

    @Override
    @CallSuper
    public void onAnimationEnd() {
        tracker.clear();
    }

    @Override
    @CallSuper
    public void onAnimationCancel() {
        tracker.clear();
    }

    @Override
    public AnimatorSet createAnimator() {
        return createAnimator(getCurrentMotionSpec());
    }

    @SuppressLint("RestrictedApi")
    @NonNull
    AnimatorSet createAnimator(@NonNull MotionSpec spec) {
        List<Animator> animators = new ArrayList<>();

        if (spec.hasPropertyValues("opacity")) {
            animators.add(spec.getAnimator("opacity", fab, View.ALPHA));
        }

        if (spec.hasPropertyValues("scale")) {
            animators.add(spec.getAnimator("scale", fab, View.SCALE_Y));
            animators.add(spec.getAnimator("scale", fab, View.SCALE_X));
        }

        if (spec.hasPropertyValues("width")) {
            animators.add(spec.getAnimator("width", fab, ExtendedFloatingActionButton.WIDTH));
        }

        if (spec.hasPropertyValues("height")) {
            animators.add(spec.getAnimator("height", fab, ExtendedFloatingActionButton.HEIGHT));
        }

        if (spec.hasPropertyValues("paddingStart")) {
            animators.add(
                    spec.getAnimator("paddingStart", fab, ExtendedFloatingActionButton.PADDING_START));
        }

        if (spec.hasPropertyValues("paddingEnd")) {
            animators.add(spec.getAnimator("paddingEnd", fab, ExtendedFloatingActionButton.PADDING_END));
        }

        AnimatorSet set = new AnimatorSet();
        AnimatorSetCompat.playTogether(set, animators);
        return set;
    }

    @Override
    @Deprecated
    public final void onChange(@Nullable ExtendedFloatingActionButton.OnChangedCallback callback) {
    }
}
