package io.github.chenfei0928.widget.recyclerview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 将该库中两处计算错误修复
 * {@link #setBubbleAndHandlePosition}
 * {@link #updateBubbleAndHandlePosition}
 * <p>
 *
 * @see <a href="https://github.com/AndroidDeveloperLB/LollipopContactsRecyclerViewFastScroller">Github仓库地址</a>
 * @see <a href="https://jitpack.io/#AndroidDeveloperLB/LollipopContactsRecyclerViewFastScroller/">JitPack仓库地址</a>
 */
public class RecyclerViewFastScroller extends LinearLayoutCompat {
    private static final int BUBBLE_ANIMATION_DURATION = 100;
    private static final int TRACK_SNAP_RANGE = 5;

    private TextView bubble;
    private View handle;
    private RecyclerView recyclerView;
    private int height;
    private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
            updateBubbleAndHandlePosition();
        }
    };
    private ViewPropertyAnimator currentAnimator;

    public RecyclerViewFastScroller(final Context context) {
        this(context, null);
    }

    public RecyclerViewFastScroller(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerViewFastScroller(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);
        setClipChildren(false);
    }

    public void setViewsToUse(@LayoutRes int layoutResId, @IdRes int bubbleResId, @IdRes int handleResId) {
        LayoutInflater.from(getContext()).inflate(layoutResId, this, true);
        bubble = findViewById(bubbleResId);
        if (bubble != null) {
            bubble.setVisibility(INVISIBLE);
        }
        handle = findViewById(handleResId);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = h;
        updateBubbleAndHandlePosition();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (event.getX() < handle.getX() - ViewCompat.getPaddingStart(handle)) {
                    return false;
                }
                if (currentAnimator != null) {
                    currentAnimator.cancel();
                }
                if (bubble != null && bubble.getVisibility() == INVISIBLE) {
                    showBubble();
                }
                handle.setSelected(true);
            case MotionEvent.ACTION_MOVE:
                final float y = event.getY();
                setBubbleAndHandlePosition(y, true);
                setRecyclerViewPosition(y);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handle.setSelected(false);
                hideBubble();
                return true;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setRecyclerView(final RecyclerView recyclerView) {
        if (this.recyclerView != recyclerView) {
            if (this.recyclerView != null) {
                this.recyclerView.removeOnScrollListener(onScrollListener);
            }
            this.recyclerView = recyclerView;
            if (this.recyclerView == null) {
                return;
            }
            recyclerView.addOnScrollListener(onScrollListener);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (recyclerView != null) {
            recyclerView.removeOnScrollListener(onScrollListener);
            recyclerView = null;
        }
    }

    private void setRecyclerViewPosition(float y) {
        if (recyclerView == null || recyclerView.getAdapter() == null) {
            return;
        }
        final int itemCount = recyclerView.getAdapter().getItemCount();
        float proportion;
        if (handle.getY() == 0) {
            proportion = 0f;
        } else if (handle.getY() + handle.getHeight() >= height - TRACK_SNAP_RANGE) {
            proportion = 1f;
        } else {
            proportion = y / height;
        }
        final int targetPos = getValueInRange(0, itemCount - 1, (int) (proportion * itemCount));
        recyclerView.scrollToPosition(targetPos);
        final String bubbleText = ((BubbleTextGetter) recyclerView.getAdapter()).getTextToShowInBubble(targetPos);
        if (bubble != null) {
            bubble.setText(bubbleText);
        }
    }

    private int getValueInRange(int min, int max, int value) {
        int minimum = Math.max(min, value);
        return Math.min(minimum, max);
    }

    private void updateBubbleAndHandlePosition() {
        if (bubble == null || handle.isSelected()) {
            return;
        }

        final int verticalScrollOffset = recyclerView.computeVerticalScrollOffset();
        final int verticalScrollRange = recyclerView.computeVerticalScrollRange();

        int visible = verticalScrollRange == 0 ? INVISIBLE : VISIBLE;
        if (getVisibility() != visible) {
            setVisibility(visible);
        }

        // 原始此处是 verticalScrollOffset / ((float) verticalScrollRange - height)
        // 因为原生的 LinearLayoutManager 设定返回为可渲染的总长度，SuperSLiM设定为返回相对控件的高度
        float proportion = verticalScrollOffset / (float) verticalScrollRange;
        setBubbleAndHandlePosition(height * proportion, false);
    }

    /**
     * @param y      移动到的y坐标
     * @param motion 如果是手势操作则为true，如果是列表滑动跳转则为false（用于处理用户点按时点按的是滑动条的中间位置）
     */
    private void setBubbleAndHandlePosition(float y, boolean motion) {
        final int handleHeight = handle.getHeight();
        final int offsetHandleHeight = motion ? handleHeight / 2 : 0;
        handle.setY(getValueInRange(0, height - handleHeight, (int) y - offsetHandleHeight));
        if (bubble != null) {
            int bubbleHeight = bubble.getHeight();
            bubble.setY(getValueInRange(0, height - bubbleHeight - handleHeight / 2, (int) y - bubbleHeight));
        }
    }

    private void showBubble() {
        if (bubble == null) {
            return;
        }
        bubble.setVisibility(VISIBLE);
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }
        currentAnimator = bubble.animate().alphaBy(0).alpha(1).setDuration(BUBBLE_ANIMATION_DURATION).setListener(null);
        currentAnimator.start();
    }

    private void hideBubble() {
        if (bubble == null) {
            return;
        }
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }
        currentAnimator = bubble.animate().alphaBy(1).alpha(0).setDuration(BUBBLE_ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                bubble.setVisibility(INVISIBLE);
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                bubble.setVisibility(INVISIBLE);
                currentAnimator = null;
            }
        });
        currentAnimator.start();
    }

    public interface BubbleTextGetter {
        String getTextToShowInBubble(int pos);
    }
}
