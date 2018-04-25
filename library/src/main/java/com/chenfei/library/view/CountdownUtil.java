package com.chenfei.library.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.support.annotation.StringRes;
import android.widget.TextView;

import com.chenfei.library.util.lambdaFunction.Action1;

/**
 * 倒计时工具类
 * Created by MrFeng on 2016/12/9.
 */
public class CountdownUtil {
    public static void countdownText(TextView tv, @StringRes int completeString, @StringRes int nextString) {
        tv.setEnabled(false);
        countdownText(60,
                aLong -> tv.setText(tv.getContext().getString(nextString, aLong)),
                () -> {
                    tv.setText(completeString);
                    tv.setEnabled(true);
                });
    }

    public static void countdownText(int takeCount, Action1<Integer> onNext, Runnable onCompleted) {
        ValueAnimator animator = ValueAnimator.ofInt(takeCount, 0)
                .setDuration(takeCount * 1000);
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            onNext.call(value);
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                onCompleted.run();
            }
        });
        animator.start();
    }
}
