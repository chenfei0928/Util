package io.github.chenfei0928.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import androidx.annotation.Keep;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * 无副作用，类似于{@link Keep}
 * 指定某个方法被混淆删除后不会产生副作用，用于去除Debug日志用
 *
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2019-08-19 14:22
 */
@Retention(CLASS)
@Target({PACKAGE, TYPE, ANNOTATION_TYPE, CONSTRUCTOR, METHOD, FIELD})
public @interface NoSideEffects {
}
