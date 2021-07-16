package com.chenfei.util.lambdaFunction;

/**
 * RxJava 1.0 Actions
 * Created by MrFeng on 2017/1/6.
 */
public class Actions {
    private static final EmptyAction EMPTY_ACTION = new EmptyAction();

    @SuppressWarnings("unchecked")
    public static <T0, T1, T2> EmptyAction<T0, T1, T2> empty() {
        return EMPTY_ACTION;
    }

    static final class EmptyAction<T0, T1, T2> implements
            Action0,
            Action1<T0>,
            Action2<T0, T1>,
            Action3<T0, T1, T2> {

        @Override
        public void call() {
            // deliberately no op
        }

        @Override
        public void call(T0 t1) {
            // deliberately no op
        }

        @Override
        public void call(T0 t1, T1 t2) {
            // deliberately no op
        }

        @Override
        public void call(T0 t0, T1 t1, T2 t2) {
            // deliberately no op
        }
    }
}
