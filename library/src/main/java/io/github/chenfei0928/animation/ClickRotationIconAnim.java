package io.github.chenfei0928.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.util.Property;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by MrFeng on 2017/7/17.
 */
public class ClickRotationIconAnim {
    // 单击的旋转动画
    private final ObjectAnimator mSingleClickAnim;
    // 重复点击时的旋转动画
    private final ObjectAnimator mDoubleClickAnim;
    // 动画当前执行到的角度
    private float mCurrentValue = 0;
    private final Property<View, Float> rotation = new Property<>(Float.class, "rotation") {
        @Override
        public void set(View object, Float value) {
            object.setRotation(value);
            mCurrentValue = value;
        }

        @Override
        public Float get(View object) {
            return object.getRotation();
        }
    };
    // 动画执行目标角度
    private int mTargetValue = 720;

    public ClickRotationIconAnim(View target) {
        // 单击，1.5秒2圈，先加速后减速
        mSingleClickAnim = ObjectAnimator.ofFloat(target, rotation, 0f, 720f);
        mSingleClickAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        mSingleClickAnim.setDuration(1500);
        // 连续点击，1.5秒时间逐渐减速
        mDoubleClickAnim = ObjectAnimator.ofFloat(target, rotation, 0, 0);
        mDoubleClickAnim.setInterpolator(new DecelerateInterpolator());
        mDoubleClickAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 动画结束后，将动画目标值重置
                mCurrentValue = 0;
                mTargetValue = 720;
            }
        });
        mDoubleClickAnim.setDuration(1500);
    }

    /**
     * 用户点击旋转球，进行旋转，如果连续点击要加速旋转
     */
    public void click() {
        if (!mSingleClickAnim.isRunning() && !mDoubleClickAnim.isRunning()) {
            // 单击、连续点击动画都没有在执行，执行单击动画
            mSingleClickAnim.start();
        } else {
            // 某个动画已经在执行，取消单选动画，并开始连续点击时的动画
            if (mSingleClickAnim.isRunning()) {
                mSingleClickAnim.cancel();
            }
            // 追加两圈旋转
            mTargetValue += 720;
            mDoubleClickAnim.setFloatValues(mCurrentValue, mTargetValue);
            mDoubleClickAnim.start();
        }
    }
}
