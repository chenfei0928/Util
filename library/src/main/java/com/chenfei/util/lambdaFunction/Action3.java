package com.chenfei.util.lambdaFunction;

/**
 * RxJava 1.x 类似的 Action3
 * Created by MrFeng on 2017/12/20.
 */
@FunctionalInterface
public interface Action3<T1, T2, T3> {
    void call(T1 t1, T2 t2, T3 t3);
}
