package io.github.chenfei0928.demo

/**
 * @author chenf()
 * @date 2024-12-16 16:17
 */
abstract class I<E : Any> {

    open class I1<E, T : List<E>> : I<T>() {

        open class IM<E, T : MutableList<E>> : I1<E, T>()

        class IArrayList : IM<Any, ArrayList<Any>>()
    }
}
