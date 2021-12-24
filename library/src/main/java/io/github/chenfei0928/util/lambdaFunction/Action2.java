package io.github.chenfei0928.util.lambdaFunction;

/**
 * RxJava 1.x 类似的 Action2
 * Created by MrFeng on 2017/1/6.
 */
@FunctionalInterface
public interface Action2<T1, T2> {
    void call(T1 t1, T2 t2);
}
