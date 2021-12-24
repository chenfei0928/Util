package io.github.chenfei0928.util.lambdaFunction;

/**
 * RxJava 1.x 类似的 Action1
 * Created by MrFeng on 2017/1/6.
 */
@FunctionalInterface
public interface Action1<T> {
    void call(T t);
}
