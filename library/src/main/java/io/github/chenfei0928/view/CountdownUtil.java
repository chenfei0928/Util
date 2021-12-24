package io.github.chenfei0928.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.widget.TextView;

import io.github.chenfei0928.util.lambdaFunction.Action1;

import androidx.annotation.StringRes;

/**
 * 倒计时工具类
 * Created by MrFeng on 2016/12/9.
 */
public class CountdownUtil {
    public static Animator countdownText(TextView tv, @StringRes int completeString, @StringRes int nextString) {
        tv.setEnabled(false);
        return countdownText(60,
                aLong -> tv.setText(tv.getContext().getString(nextString, aLong)),
                () -> {
                    tv.setText(completeString);
                    tv.setEnabled(true);
                });
    }

    public static Animator countdownText(int takeCount, Action1<Integer> onNext, Runnable onCompleted) {
        ValueAnimator animator = ValueAnimator.ofInt(takeCount, 0)
                .setDuration(takeCount * 1000);
        animator.setInterpolator(null);
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            onNext.call(value);
        });
        animator.addListener(new AnimatorListenerAdapter() {
            boolean mIsCanceled = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                mIsCanceled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mIsCanceled) {
                    return;
                }
                onCompleted.run();
            }
        });
        animator.start();
        return animator;
    }
}
