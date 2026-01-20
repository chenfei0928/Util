package io.github.chenfei0928.util

import org.gradle.api.Transformer
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import java.util.function.BiFunction

/**
 * @author chenf()
 * @date 2026-01-20 11:22
 */
abstract class AbsProvider<T : Any> : Provider<T> {
    protected abstract val value: T
    override fun get(): T = value
    override fun getOrNull(): T = value
    override fun getOrElse(defaultValue: T): T = value
    override fun <S : Any> map(transformer: Transformer<out S?, in T>): Provider<S> =
        LazyProvider { transformer.transform(value)!! }

    override fun filter(spec: Spec<in T>): Provider<T> = LazyProvider {
        if (spec.isSatisfiedBy(value)) value else throw IllegalStateException("value not satisfied")
    }

    override fun <S : Any> flatMap(transformer: Transformer<out Provider<out S>?, in T>): Provider<S> =
        transformer.transform(value)!! as Provider<S>

    override fun isPresent(): Boolean = true
    override fun orElse(value: T): Provider<T> = this
    override fun orElse(provider: Provider<out T>): Provider<T> = this
    override fun <U : Any, R : Any> zip(
        right: Provider<U>, combiner: BiFunction<in T, in U, out R?>
    ): Provider<R> = LazyProvider { combiner.apply(value, right.get())!! }

    class LazyProvider<T : Any>(value: () -> T) : AbsProvider<T>() {
        override val value: T by lazy(value)
    }
}
