package com.google.android.material.floatingactionbutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Property;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.material.animation.MotionSpec;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.internal.DescendantOffsetUtils;
import com.google.android.material.internal.ThemeEnforcement;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.AnimatorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import io.github.chenfei0928.util.R;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static java.lang.Math.min;

/**
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2021-04-25 14:40
 */
public class ExtendedFloatingActionLayout extends FrameLayout implements CoordinatorLayout.AttachedBehavior {

    private static final int ANIM_STATE_NONE = 0;
    private static final int ANIM_STATE_HIDING = 1;
    private static final int ANIM_STATE_SHOWING = 2;

    private int animState = ANIM_STATE_NONE;

    private final AnimatorTracker changeVisibilityTracker = new AnimatorTracker();
    @NonNull
    private final MotionLayoutStrategy extendStrategy;
    private final MotionLayoutStrategy showStrategy = new ExtendedFloatingActionLayout.ShowStrategy(changeVisibilityTracker);
    private final MotionLayoutStrategy hideStrategy = new ExtendedFloatingActionLayout.HideStrategy(changeVisibilityTracker);
    private final int collapsedSize;

    private int extendedPaddingStart;
    private int extendedPaddingEnd;

    @NonNull
    private final CoordinatorLayout.Behavior<ExtendedFloatingActionLayout> behavior;

    private boolean isExtended = true;
    private boolean isTransforming = false;
    private boolean animateShowBeforeLayout = false;

    /**
     * Callback to be invoked when the visibility or the state of an ExtendedFloatingActionLayout
     * changes.
     */
    public abstract static class OnChangedCallback {

        /**
         * Called when a ExtendedFloatingActionLayout has been {@link
         * #show(ExtendedFloatingActionLayout.OnChangedCallback) shown}.
         *
         * @param extendedFab the FloatingActionButton that was shown.
         */
        public void onShown(ExtendedFloatingActionLayout extendedFab) {
        }

        /**
         * Called when a ExtendedFloatingActionLayout has been {@link
         * #hide(ExtendedFloatingActionLayout.OnChangedCallback) hidden}.
         *
         * @param extendedFab the ExtendedFloatingActionLayout that was hidden.
         */
        public void onHidden(ExtendedFloatingActionLayout extendedFab) {
        }

        /**
         * Called when a ExtendedFloatingActionLayout has been {@link
         * #extend(ExtendedFloatingActionLayout.OnChangedCallback) extended} to show the icon and the
         * text.
         *
         * @param extendedFab the ExtendedFloatingActionLayout that was extended.
         */
        public void onExtended(ExtendedFloatingActionLayout extendedFab) {
        }
    }

    public ExtendedFloatingActionLayout(@NonNull Context context) {
        this(context, null);
    }

    public ExtendedFloatingActionLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings("nullness")
    public ExtendedFloatingActionLayout(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // Ensure we are using the correctly themed context rather than the context that was passed in.
        context = getContext();
        behavior = new ExtendedFloatingActionLayout.ExtendedFloatingActionLayoutBehavior<>(context, attrs);
        @SuppressLint("RestrictedApi") TypedArray a =
                ThemeEnforcement.obtainStyledAttributes(
                        context, attrs, R.styleable.ExtendedFloatingActionLayout, defStyleAttr, 0);

        MotionSpec showMotionSpec =
                MotionSpec.createFromAttribute(
                        context, a, R.styleable.ExtendedFloatingActionLayout_showMotionSpec);
        MotionSpec hideMotionSpec =
                MotionSpec.createFromAttribute(
                        context, a, R.styleable.ExtendedFloatingActionLayout_hideMotionSpec);
        MotionSpec extendMotionSpec =
                MotionSpec.createFromAttribute(
                        context, a, R.styleable.ExtendedFloatingActionLayout_extendMotionSpec);
        collapsedSize =
                a.getDimensionPixelSize(R.styleable.ExtendedFloatingActionLayout_collapsedSize, -1);
        extendedPaddingStart = ViewCompat.getPaddingStart(this);
        extendedPaddingEnd = ViewCompat.getPaddingEnd(this);

        AnimatorTracker changeSizeTracker = new AnimatorTracker();
        extendStrategy =
                new ExtendedFloatingActionLayout.ChangeSizeStrategy(
                        changeSizeTracker,
                        new ExtendedFloatingActionLayout.Size() {
                            @Override
                            public int getWidth() {
                                return getMeasuredWidth()
                                        - getCollapsedPadding() * 2
                                        + extendedPaddingStart
                                        + extendedPaddingEnd;
                            }

                            @Override
                            public int getHeight() {
                                return getMeasuredHeight();
                            }

                            @Override
                            public int getPaddingStart() {
                                return extendedPaddingStart;
                            }

                            @Override
                            public int getPaddingEnd() {
                                return extendedPaddingEnd;
                            }

                            @Override
                            public ViewGroup.LayoutParams getLayoutParams() {
                                return new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                            }
                        },
                        /* extending= */ true);

        showStrategy.setMotionSpec(showMotionSpec);
        hideStrategy.setMotionSpec(hideMotionSpec);
        extendStrategy.setMotionSpec(extendMotionSpec);
        a.recycle();
    }

    @NonNull
    @Override
    public CoordinatorLayout.Behavior<ExtendedFloatingActionLayout> getBehavior() {
        return behavior;
    }

    /**
     * Extends or shrinks the fab depending on the value of {@param extended}.
     */
    public void setExtended(boolean extended) {
        if (this.isExtended == extended) {
            return;
        }

        MotionLayoutStrategy MotionLayoutStrategy = extendStrategy;
        if (MotionLayoutStrategy.shouldCancel()) {
            return;
        }

        MotionLayoutStrategy.performNow();
    }

    public final boolean isExtended() {
        return isExtended;
    }

    /**
     * Sets whether to enable animation for a call to show {@link #show} even if the view has not been
     * laid out yet.
     *
     * <p>This may be set to {@code true} if the button is initially hidden but should animate when
     * later shown. The default is {@code false}.
     */
    public void setAnimateShowBeforeLayout(boolean animateShowBeforeLayout) {
        this.animateShowBeforeLayout = animateShowBeforeLayout;
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        super.setPaddingRelative(start, top, end, bottom);
        if (isExtended && !isTransforming) {
            extendedPaddingStart = start;
            extendedPaddingEnd = end;
        }
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        if (isExtended && !isTransforming) {
            extendedPaddingStart = ViewCompat.getPaddingStart(this);
            extendedPaddingEnd = ViewCompat.getPaddingEnd(this);
        }
    }

    /**
     * Add a listener that will be invoked when this ExtendedFloatingActionLayout is shown. See {@link
     * Animator.AnimatorListener}.
     *
     * <p>Components that add a listener should take care to remove it when finished via {@link
     * #removeOnShowAnimationListener(Animator.AnimatorListener)}.
     *
     * @param listener listener to add
     */
    public void addOnShowAnimationListener(@NonNull Animator.AnimatorListener listener) {
        showStrategy.addAnimationListener(listener);
    }

    /**
     * Remove a listener that was previously added via
     * {@link #addOnShowAnimationListener(Animator.AnimatorListener)}.
     *
     * @param listener listener to remove
     */
    public void removeOnShowAnimationListener(@NonNull Animator.AnimatorListener listener) {
        showStrategy.removeAnimationListener(listener);
    }

    /**
     * Add a listener that will be invoked when this ExtendedFloatingActionLayout is hidden. See
     * {@link Animator.AnimatorListener}.
     *
     * <p>Components that add a listener should take care to remove it when finished via {@link
     * #removeOnHideAnimationListener(Animator.AnimatorListener)}.
     *
     * @param listener listener to add
     */
    public void addOnHideAnimationListener(@NonNull Animator.AnimatorListener listener) {
        hideStrategy.addAnimationListener(listener);
    }

    /**
     * Remove a listener that was previously added via
     * {@link #addOnHideAnimationListener(Animator.AnimatorListener)}.
     *
     * @param listener listener to remove
     */
    public void removeOnHideAnimationListener(@NonNull Animator.AnimatorListener listener) {
        hideStrategy.removeAnimationListener(listener);
    }

    /**
     * Add a listener that will be invoked when this ExtendedFloatingActionLayout is extended. See
     * {@link Animator.AnimatorListener}.
     *
     * <p>Components that add a listener should take care to remove it when finished via {@link
     * #removeOnExtendAnimationListener(Animator.AnimatorListener)}.
     *
     * @param listener listener to add
     */
    public void addOnExtendAnimationListener(@NonNull Animator.AnimatorListener listener) {
        extendStrategy.addAnimationListener(listener);
    }

    /**
     * Remove a listener that was previously added via
     * {@link #addOnExtendAnimationListener(Animator.AnimatorListener)}.
     *
     * @param listener listener to remove
     */
    public void removeOnExtendAnimationListener(@NonNull Animator.AnimatorListener listener) {
        extendStrategy.removeAnimationListener(listener);
    }

    /**
     * Hides the button.
     *
     * <p>This method will animate the button hide if the view has already been laid out.
     */
    public void hide() {
        performMotion(hideStrategy, null);
    }

    /**
     * Hides the button.
     *
     * <p>This method will animate the button hide if the view has already been laid out.
     *
     * @param callback the callback to notify when this view is hidden
     */
    public void hide(@NonNull ExtendedFloatingActionLayout.OnChangedCallback callback) {
        performMotion(hideStrategy, callback);
    }

    /**
     * Shows the button.
     *
     * <p>This method will animate the button show if the view has already been laid out, or if {@link
     * #setAnimateShowBeforeLayout} is {@code true}.
     */
    public void show() {
        performMotion(showStrategy, null);
    }

    /**
     * Shows the button.
     *
     * <p>This method will animate the button show if the view has already been laid out, or if {@link
     * #setAnimateShowBeforeLayout} is {@code true}.
     *
     * @param callback the callback to notify when this view is shown
     */
    public void show(@NonNull ExtendedFloatingActionLayout.OnChangedCallback callback) {
        performMotion(showStrategy, callback);
    }

    /**
     * Extends the FAB to show the text and the icon.
     *
     * <p>This method will not affect an extended FAB which holds just text and no icon. Also, this
     * method will animate the button show if the view has already been laid out.
     *
     * @see #extend(ExtendedFloatingActionLayout.OnChangedCallback)
     */
    public void extend() {
        performMotion(extendStrategy, null);
    }

    /**
     * Extends the FAB to show the text and the icon.
     *
     * <p>This method will not affect an extended FAB which holds just text and no icon. Also, this
     * method will animate the button show if the view has already been laid out.
     *
     * @param callback the callback to notify when the FAB is extended
     */
    public void extend(@NonNull final ExtendedFloatingActionLayout.OnChangedCallback callback) {
        performMotion(extendStrategy, callback);
    }

    /**
     * Returns the motion spec for the show animation.
     */
    @Nullable
    public MotionSpec getShowMotionSpec() {
        return showStrategy.getMotionSpec();
    }

    /**
     * Updates the motion spec for the show animation.
     *
     * @attr ref com.google.android.material.R.styleable#ExtendedFloatingActionLayout_showMotionSpec
     */
    public void setShowMotionSpec(@Nullable MotionSpec spec) {
        showStrategy.setMotionSpec(spec);
    }

    /**
     * Updates the motion spec for the show animation.
     *
     * @attr ref com.google.android.material.R.styleable#ExtendedFloatingActionLayout_showMotionSpec
     */
    public void setShowMotionSpecResource(@AnimatorRes int id) {
        setShowMotionSpec(MotionSpec.createFromResource(getContext(), id));
    }

    /**
     * Returns the motion spec for the hide animation.
     */
    @Nullable
    public MotionSpec getHideMotionSpec() {
        return hideStrategy.getMotionSpec();
    }

    /**
     * Updates the motion spec for the hide animation.
     *
     * @attr ref com.google.android.material.R.styleable#ExtendedFloatingActionLayout_hideMotionSpec
     */
    public void setHideMotionSpec(@Nullable MotionSpec spec) {
        hideStrategy.setMotionSpec(spec);
    }

    /**
     * Updates the motion spec for the hide animation.
     *
     * @attr ref com.google.android.material.R.styleable#ExtendedFloatingActionLayout_hideMotionSpec
     */
    public void setHideMotionSpecResource(@AnimatorRes int id) {
        setHideMotionSpec(MotionSpec.createFromResource(getContext(), id));
    }

    /**
     * Returns the motion spec for the extend animation.
     */
    @Nullable
    public MotionSpec getExtendMotionSpec() {
        return extendStrategy.getMotionSpec();
    }

    /**
     * Updates the motion spec for the extend animation.
     *
     * @attr ref com.google.android.material.R.styleable#ExtendedFloatingActionLayout_extendMotionSpec
     */
    public void setExtendMotionSpec(@Nullable MotionSpec spec) {
        extendStrategy.setMotionSpec(spec);
    }

    /**
     * Updates the motion spec for the extend animation.
     *
     * @attr ref com.google.android.material.R.styleable#ExtendedFloatingActionLayout_extendMotionSpec
     */
    public void setExtendMotionSpecResource(@AnimatorRes int id) {
        setExtendMotionSpec(MotionSpec.createFromResource(getContext(), id));
    }

    private void performMotion(
            @NonNull final MotionLayoutStrategy strategy, @Nullable final OnChangedCallback callback) {
        if (strategy.shouldCancel()) {
            return;
        }

        boolean shouldAnimate = shouldAnimateVisibilityChange();
        if (!shouldAnimate) {
            strategy.performNow();
            strategy.onChange(callback);
            return;
        }

        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        Animator animator = strategy.createAnimator();
        animator.addListener(
                new AnimatorListenerAdapter() {
                    private boolean cancelled;

                    @Override
                    public void onAnimationStart(Animator animation) {
                        strategy.onAnimationStart(animation);
                        cancelled = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        cancelled = true;
                        strategy.onAnimationCancel();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        strategy.onAnimationEnd();
                        if (!cancelled) {
                            strategy.onChange(callback);
                        }
                    }
                });

        for (Animator.AnimatorListener l : strategy.getListeners()) {
            animator.addListener(l);
        }

        animator.start();
    }

    private boolean isOrWillBeShown() {
        if (getVisibility() != View.VISIBLE) {
            // If we're not currently visible, return true if we're animating to be shown
            return animState == ANIM_STATE_SHOWING;
        } else {
            // Otherwise if we're visible, return true if we're not animating to be hidden
            return animState != ANIM_STATE_HIDING;
        }
    }

    private boolean isOrWillBeHidden() {
        if (getVisibility() == View.VISIBLE) {
            // If we're currently visible, return true if we're animating to be hidden
            return animState == ANIM_STATE_HIDING;
        } else {
            // Otherwise if we're not visible, return true if we're not animating to be shown
            return animState != ANIM_STATE_SHOWING;
        }
    }

    private boolean shouldAnimateVisibilityChange() {
        return (ViewCompat.isLaidOut(this) || (!isOrWillBeShown() && animateShowBeforeLayout))
                && !isInEditMode();
    }

    /**
     * A Property wrapper around the <code>width</code> functionality handled by the {@link
     * ViewGroup.LayoutParams#width} value.
     */
    static final Property<View, Float> WIDTH =
            new Property<View, Float>(Float.class, "width") {
                @Override
                public void set(@NonNull View object, @NonNull Float value) {
                    object.getLayoutParams().width = value.intValue();
                    object.requestLayout();
                }

                @NonNull
                @Override
                public Float get(@NonNull View object) {
                    return (float) object.getLayoutParams().width;
                }
            };

    /**
     * A Property wrapper around the <code>height</code> functionality handled by the {@link
     * ViewGroup.LayoutParams#height} value.
     */
    static final Property<View, Float> HEIGHT =
            new Property<View, Float>(Float.class, "height") {
                @Override
                public void set(@NonNull View object, @NonNull Float value) {
                    object.getLayoutParams().height = value.intValue();
                    object.requestLayout();
                }

                @NonNull
                @Override
                public Float get(@NonNull View object) {
                    return (float) object.getLayoutParams().height;
                }
            };

    /**
     * A Property wrapper around the <code>paddingStart</code> functionality handled by the {@link
     * ViewCompat#setPaddingRelative(View, int, int, int, int)}.
     */
    static final Property<View, Float> PADDING_START =
            new Property<View, Float>(Float.class, "paddingStart") {
                @Override
                public void set(@NonNull View object, @NonNull Float value) {
                    ViewCompat.setPaddingRelative(
                            object,
                            value.intValue(),
                            object.getPaddingTop(),
                            ViewCompat.getPaddingEnd(object),
                            object.getPaddingBottom());
                }

                @NonNull
                @Override
                public Float get(@NonNull View object) {
                    return (float) ViewCompat.getPaddingStart(object);
                }
            };

    /**
     * A Property wrapper around the <code>paddingEnd</code> functionality handled by the {@link
     * ViewCompat#setPaddingRelative(View, int, int, int, int)}.
     */
    static final Property<View, Float> PADDING_END =
            new Property<View, Float>(Float.class, "paddingEnd") {
                @Override
                public void set(@NonNull View object, @NonNull Float value) {
                    ViewCompat.setPaddingRelative(
                            object,
                            ViewCompat.getPaddingStart(object),
                            object.getPaddingTop(),
                            value.intValue(),
                            object.getPaddingBottom());
                }

                @NonNull
                @Override
                public Float get(@NonNull View object) {
                    return (float) ViewCompat.getPaddingEnd(object);
                }
            };

    /**
     * Shrink to the smaller value between paddingStart and paddingEnd, such that when shrunk the icon
     * will be centered.
     */
    @VisibleForTesting
    int getCollapsedSize() {
        return collapsedSize < 0
                ? min(ViewCompat.getPaddingStart(this), ViewCompat.getPaddingEnd(this)) * 2
                : collapsedSize;
    }

    int getCollapsedPadding() {
        return (getCollapsedSize()) / 2;
    }

    /**
     * Behavior designed for use with {@link ExtendedFloatingActionLayout} instances. Its main
     * function is to move {@link ExtendedFloatingActionLayout} views so that any displayed {@link
     * com.google.android.material.snackbar.Snackbar}s do not cover them.
     */
    protected static class ExtendedFloatingActionLayoutBehavior<
            T extends ExtendedFloatingActionLayout>
            extends CoordinatorLayout.Behavior<T> {
        private static final boolean AUTO_HIDE_DEFAULT = true;

        private Rect tmpRect;
        @Nullable
        private ExtendedFloatingActionLayout.OnChangedCallback internalAutoHideCallback;
        private boolean autoHideEnabled;

        public ExtendedFloatingActionLayoutBehavior() {
            super();
            autoHideEnabled = AUTO_HIDE_DEFAULT;
        }

        // Behavior attrs should be nullable in the framework
        @SuppressWarnings("argument.type.incompatible")
        public ExtendedFloatingActionLayoutBehavior(
                @NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            TypedArray a =
                    context.obtainStyledAttributes(
                            attrs, R.styleable.ExtendedFloatingActionLayout_Behavior_Layout);
            autoHideEnabled =
                    a.getBoolean(
                            R.styleable.ExtendedFloatingActionLayout_Behavior_Layout_behavior_autoHide,
                            AUTO_HIDE_DEFAULT);
            a.recycle();
        }

        /**
         * Sets whether the associated ExtendedFloatingActionLayout automatically hides when there is
         * not enough space to be displayed. This works with {@link AppBarLayout} and {@link
         * BottomSheetBehavior}.
         *
         * <p>In case auto-shrink is enabled, it will take precedence over the auto-hide option.
         *
         * @param autoHide true to enable automatic hiding
         * @attr ref
         * com.google.android.material.R.styleable#ExtendedFloatingActionLayout_Behavior_Layout_behavior_autoHide
         */
        public void setAutoHideEnabled(boolean autoHide) {
            autoHideEnabled = autoHide;
        }

        /**
         * Returns whether the associated ExtendedFloatingActionLayout automatically hides when there is
         * not enough space to be displayed.
         *
         * @return true if enabled
         * @attr ref
         * com.google.android.material.R.styleable#ExtendedFloatingActionLayout_Behavior_Layout_behavior_autoHide
         */
        public boolean isAutoHideEnabled() {
            return autoHideEnabled;
        }

        @Override
        @SuppressWarnings({"unchecked"})
        // TODO: remove this stub method. Adding now to mantain compatibility since the override is
        // not correct.
        public boolean getInsetDodgeRect(
                @NonNull CoordinatorLayout parent,
                @NonNull ExtendedFloatingActionLayout child,
                @NonNull Rect rect) {
            return super.getInsetDodgeRect(parent, (T) child, rect);
        }

        @Override
        public void onAttachedToLayoutParams(@NonNull CoordinatorLayout.LayoutParams lp) {
            if (lp.dodgeInsetEdges == Gravity.NO_GRAVITY) {
                // If the developer hasn't set dodgeInsetEdges, lets set it to BOTTOM so that
                // we dodge any Snackbars
                lp.dodgeInsetEdges = Gravity.BOTTOM;
            }
        }

        @Override
        public boolean onDependentViewChanged(
                @NotNull CoordinatorLayout parent, @NonNull ExtendedFloatingActionLayout child, @NotNull View dependency) {
            if (dependency instanceof AppBarLayout) {
                // If we're depending on an AppBarLayout we will show/hide it automatically
                // if the FAB is anchored to the AppBarLayout
                updateFabVisibilityForAppBarLayout(parent, (AppBarLayout) dependency, child);
            } else if (isBottomSheet(dependency)) {
                updateFabVisibilityForBottomSheet(dependency, child);
            }
            return false;
        }

        private static boolean isBottomSheet(@NonNull View view) {
            final ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (lp instanceof CoordinatorLayout.LayoutParams) {
                return ((CoordinatorLayout.LayoutParams) lp).getBehavior() instanceof BottomSheetBehavior;
            }
            return false;
        }

        @VisibleForTesting
        void setInternalAutoHideCallback(@Nullable ExtendedFloatingActionLayout.OnChangedCallback callback) {
            internalAutoHideCallback = callback;
        }

        private boolean shouldUpdateVisibility(
                @NonNull View dependency, @NonNull ExtendedFloatingActionLayout child) {
            final CoordinatorLayout.LayoutParams lp =
                    (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            if (!autoHideEnabled) {
                return false;
            }

            if (lp.getAnchorId() != dependency.getId()) {
                // The anchor ID doesn't match the dependency, so we won't automatically
                // show/hide the FAB
                return false;
            }

            return true;
        }

        @SuppressLint("RestrictedApi")
        private boolean updateFabVisibilityForAppBarLayout(
                CoordinatorLayout parent,
                @NonNull AppBarLayout appBarLayout,
                @NonNull ExtendedFloatingActionLayout child) {
            if (!shouldUpdateVisibility(appBarLayout, child)) {
                return false;
            }

            if (tmpRect == null) {
                tmpRect = new Rect();
            }

            // First, let's get the visible rect of the dependency
            final Rect rect = tmpRect;
            DescendantOffsetUtils.getDescendantRect(parent, appBarLayout, rect);

            if (rect.bottom <= appBarLayout.getMinimumHeightForVisibleOverlappingContent()) {
                // If the anchor's bottom is below the seam, we'll animate our FAB out
                shrinkOrHide(child);
            } else {
                // Else, we'll animate our FAB back in
                extendOrShow(child);
            }
            return true;
        }

        private boolean updateFabVisibilityForBottomSheet(
                @NonNull View bottomSheet, @NonNull ExtendedFloatingActionLayout child) {
            if (!shouldUpdateVisibility(bottomSheet, child)) {
                return false;
            }
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            if (bottomSheet.getTop() < child.getHeight() / 2 + lp.topMargin) {
                shrinkOrHide(child);
            } else {
                extendOrShow(child);
            }
            return true;
        }

        /**
         * Shrinks the Extended FAB, in case auto-shrink is enabled, or hides it in case auto-hide is
         * enabled. The priority is given to the default shrink option, and the button will be hidden
         * only when the auto-shrink is {@code false} and auto-hide is {@code true}.
         *
         * @attr ref
         * com.google.android.material.R.styleable#ExtendedFloatingActionLayout_Behavior_Layout_behavior_autoShrink
         * @attr ref
         * com.google.android.material.R.styleable#ExtendedFloatingActionLayout_Behavior_Layout_behavior_autoHide
         * @see #setAutoHideEnabled(boolean)
         */
        protected void shrinkOrHide(@NonNull ExtendedFloatingActionLayout fab) {
            ExtendedFloatingActionLayout.OnChangedCallback callback = internalAutoHideCallback;
            MotionLayoutStrategy strategy = fab.hideStrategy;

            fab.performMotion(strategy, callback);
        }

        /**
         * Extends the Extended FAB, in case auto-shrink is enabled, or show it in case auto-hide is
         * enabled. The priority is given to the default extend option, and the button will be shown
         * only when the auto-shrink is {@code false} and auto-hide is {@code true}.
         *
         * @attr ref
         * com.google.android.material.R.styleable#ExtendedFloatingActionLayout_Behavior_Layout_behavior_autoShrink
         * @attr ref
         * com.google.android.material.R.styleable#ExtendedFloatingActionLayout_Behavior_Layout_behavior_autoHide
         * @see #setAutoHideEnabled(boolean)
         */
        protected void extendOrShow(@NonNull ExtendedFloatingActionLayout fab) {
            ExtendedFloatingActionLayout.OnChangedCallback callback = internalAutoHideCallback;
            MotionLayoutStrategy strategy = fab.showStrategy;

            fab.performMotion(strategy, callback);
        }

        @Override
        public boolean onLayoutChild(
                @NonNull CoordinatorLayout parent,
                @NonNull ExtendedFloatingActionLayout child,
                int layoutDirection) {
            // First, let's make sure that the visibility of the FAB is consistent
            final List<View> dependencies = parent.getDependencies(child);
            for (int i = 0, count = dependencies.size(); i < count; i++) {
                final View dependency = dependencies.get(i);
                if (dependency instanceof AppBarLayout) {
                    if (updateFabVisibilityForAppBarLayout(parent, (AppBarLayout) dependency, child)) {
                        break;
                    }
                } else if (isBottomSheet(dependency)) {
                    if (updateFabVisibilityForBottomSheet(dependency, child)) {
                        break;
                    }
                }
            }
            // Now let the CoordinatorLayout lay out the FAB
            parent.onLayoutChild(child, layoutDirection);
            return true;
        }
    }

    interface Size {
        int getWidth();

        int getHeight();

        int getPaddingStart();

        int getPaddingEnd();

        ViewGroup.LayoutParams getLayoutParams();
    }

    class ChangeSizeStrategy extends BaseMotionLayoutStrategy {

        private final ExtendedFloatingActionLayout.Size size;
        private final boolean extending;

        ChangeSizeStrategy(AnimatorTracker animatorTracker, ExtendedFloatingActionLayout.Size size, boolean extending) {
            super(ExtendedFloatingActionLayout.this, animatorTracker);
            this.size = size;
            this.extending = extending;
        }

        @Override
        public void performNow() {
            isExtended = extending;
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (layoutParams == null) {
                return;
            }

            layoutParams.width = size.getLayoutParams().width;
            layoutParams.height = size.getLayoutParams().height;
            ViewCompat.setPaddingRelative(
                    ExtendedFloatingActionLayout.this,
                    size.getPaddingStart(),
                    getPaddingTop(),
                    size.getPaddingEnd(),
                    getPaddingBottom());
            requestLayout();
        }

        @Override
        public void onChange(@Nullable final ExtendedFloatingActionLayout.OnChangedCallback callback) {
            if (callback == null) {
                return;
            }

            if (extending) {
                callback.onExtended(ExtendedFloatingActionLayout.this);
            }
        }

        @Override
        public int getDefaultMotionSpecResource() {
            return extending
                    ? R.animator.mtrl_extended_fab_change_size_expand_motion_spec
                    : R.animator.mtrl_extended_fab_change_size_collapse_motion_spec;
        }

        @NonNull
        @Override
        public AnimatorSet createAnimator() {
            MotionSpec spec = getCurrentMotionSpec();
            if (spec.hasPropertyValues("width")) {
                PropertyValuesHolder[] widthValues = spec.getPropertyValues("width");
                widthValues[0].setFloatValues(getWidth(), size.getWidth());
                spec.setPropertyValues("width", widthValues);
            }

            if (spec.hasPropertyValues("height")) {
                PropertyValuesHolder[] heightValues = spec.getPropertyValues("height");
                heightValues[0].setFloatValues(getHeight(), size.getHeight());
                spec.setPropertyValues("height", heightValues);
            }

            if (spec.hasPropertyValues("paddingStart")) {
                PropertyValuesHolder[] paddingValues = spec.getPropertyValues("paddingStart");
                paddingValues[0].setFloatValues(
                        ViewCompat.getPaddingStart(ExtendedFloatingActionLayout.this), size.getPaddingStart());
                spec.setPropertyValues("paddingStart", paddingValues);
            }

            if (spec.hasPropertyValues("paddingEnd")) {
                PropertyValuesHolder[] paddingValues = spec.getPropertyValues("paddingEnd");
                paddingValues[0].setFloatValues(
                        ViewCompat.getPaddingEnd(ExtendedFloatingActionLayout.this), size.getPaddingEnd());
                spec.setPropertyValues("paddingEnd", paddingValues);
            }

            return super.createAnimator(spec);
        }

        @Override
        public void onAnimationStart(Animator animator) {
            super.onAnimationStart(animator);
            isExtended = extending;
            isTransforming = true;
        }

        @Override
        public void onAnimationEnd() {
            super.onAnimationEnd();
            isTransforming = false;

            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (layoutParams == null) {
                return;
            }
            layoutParams.width = size.getLayoutParams().width;
            layoutParams.height = size.getLayoutParams().height;
        }

        @Override
        public boolean shouldCancel() {
            return extending == isExtended;
        }
    }

    class ShowStrategy extends BaseMotionLayoutStrategy {

        public ShowStrategy(AnimatorTracker animatorTracker) {
            super(ExtendedFloatingActionLayout.this, animatorTracker);
        }

        @Override
        public void performNow() {
            setVisibility(VISIBLE);
            setAlpha(1f);
            setScaleY(1f);
            setScaleX(1f);
        }

        @Override
        public void onChange(@Nullable final ExtendedFloatingActionLayout.OnChangedCallback callback) {
            if (callback != null) {
                callback.onShown(ExtendedFloatingActionLayout.this);
            }
        }

        @Override
        public int getDefaultMotionSpecResource() {
            return R.animator.mtrl_extended_fab_show_motion_spec;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
            setVisibility(VISIBLE);
            animState = ANIM_STATE_SHOWING;
        }

        @Override
        public void onAnimationEnd() {
            super.onAnimationEnd();
            animState = ANIM_STATE_NONE;
        }

        @Override
        public boolean shouldCancel() {
            return isOrWillBeShown();
        }
    }

    class HideStrategy extends BaseMotionLayoutStrategy {

        private boolean isCancelled;

        public HideStrategy(AnimatorTracker animatorTracker) {
            super(ExtendedFloatingActionLayout.this, animatorTracker);
        }

        @Override
        public void performNow() {
            setVisibility(GONE);
        }

        @Override
        public void onChange(@Nullable final ExtendedFloatingActionLayout.OnChangedCallback callback) {
            if (callback != null) {
                callback.onHidden(ExtendedFloatingActionLayout.this);
            }
        }

        @Override
        public boolean shouldCancel() {
            return isOrWillBeHidden();
        }

        @Override
        public int getDefaultMotionSpecResource() {
            return R.animator.mtrl_extended_fab_hide_motion_spec;
        }

        @Override
        public void onAnimationStart(Animator animator) {
            super.onAnimationStart(animator);
            isCancelled = false;
            setVisibility(VISIBLE);
            animState = ANIM_STATE_HIDING;
        }

        @Override
        public void onAnimationCancel() {
            super.onAnimationCancel();
            isCancelled = true;
        }

        @Override
        public void onAnimationEnd() {
            super.onAnimationEnd();
            animState = ANIM_STATE_NONE;
            if (!isCancelled) {
                setVisibility(GONE);
            }
        }
    }
}
