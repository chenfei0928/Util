package com.chenfei.util;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * RxBus事件总线，可以接受粘性事件，使用RxJava的RxBus，可以实现替换EventBus，摘自：黏性事件
 *
 * @author Admin
 * @date 2016/3/22
 * @see <a href="http://www.jianshu.com/p/943ceaccfdff">原博客</a>
 * @see <a href="https://github.com/SmartDengg/RxWeather/blob/master/common/src/main/java/com/joker/rxweather/common/rx/rxbus/RxBus.java">Github源文件</a>
 */
public class RxBus {
    private final Subject<Object> rxBus = PublishSubject.create().toSerialized();
    private final Map<Class<? extends StickEvent>, Subject> rxStickBuses = new HashMap<>();

    private RxBus() {
    }

    public static RxBus getDefault() {
        return SingletonHolder.instance;
    }

    public void post(Event event) {
        rxBus.onNext(event);
    }

    public <T extends Event> Observable<T> toObservable(Class<T> type) {
        return rxBus.hide().ofType(type);
    }

    public boolean hasObservers() {
        return rxBus.hasObservers();
    }

    private <T extends StickEvent> Subject<T> getStickSubject(Class<T> type) {
        @SuppressWarnings("unchecked") Subject<T> eventSubject = rxStickBuses.get(type);
        if (eventSubject == null) {
            synchronized (this) {
                @SuppressWarnings("unchecked") Subject<T> subject = rxStickBuses.get(type);
                eventSubject = subject;
                if (eventSubject == null) {
                    // 如果事件类没有加final修饰
                    if ((type.getModifiers() & Modifier.FINAL) == 0) {
                        throw new IllegalArgumentException("Stick粘性事件类必须为final");
                    }
                    eventSubject = BehaviorSubject.<T>create().toSerialized();
                    rxStickBuses.put(type, eventSubject);
                }
            }
        }
        return eventSubject;
    }

    public <T extends StickEvent> void postStick(T event) {
        @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) event.getClass();
        getStickSubject(clazz).onNext(event);
    }

    public <T extends StickEvent> Observable<T> toStickObservable(Class<T> type) {
        return getStickSubject(type).hide().ofType(type);
    }

    public synchronized boolean hasStickObservers(Class<? extends StickEvent> type) {
        return rxStickBuses.containsKey(type) && rxStickBuses.get(type).hasObservers();
    }

    public interface StickEvent {
    }

    public interface Event {
    }

    private static class SingletonHolder {
        private static final RxBus instance = new RxBus();
    }
}
