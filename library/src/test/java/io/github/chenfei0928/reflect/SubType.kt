package io.github.chenfei0928.reflect

import androidx.collection.ArraySet
import androidx.lifecycle.Lifecycle
import io.github.chenfei0928.bean.DataA
import io.github.chenfei0928.bean.DataInterface
import org.junit.Before

 /**
 * @author chenf()
 * @date 2025-01-14 16:42
 */
class SubType {
    @Before
    fun before() {
        System.setProperty("gson.allowCapturingTypeVariables", "true");
    }

    @org.junit.Test
    fun testParam() {
        check<Set<Any>, ArraySet<Long>>()
        check<Set<Enum<*>>, MutableSet<Lifecycle.State>>()
        check<Any, IntArray>()
        check<Array<Any>, Array<I<*>>>()
        check<Array<Any>, IntArray>()
        check<Map<DataInterface, Long>, HashMap<DataA, Long>>()
//        check<GeneratedMessageLite, >()
        check<Any, Int>()
    }

    private inline fun <reified Parent, reified Child> check() {
        val parentType = jTypeOf<Parent>()
        val childType = jTypeOf<Child>()
        println("Parent $parentType, Child $childType")
        assert(childType.isSubtypeOf(parentType))
    }

    private inline fun <reified Parent, reified Child> not() {
        val parentType = jTypeOf<Parent>()
        val childType = jTypeOf<Child>()
        println("not Parent $parentType, Child $childType")
        assert(!childType.isSubtypeOf(parentType))
    }
}
