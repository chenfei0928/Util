package io.github.chenfei0928.reflect

import com.google.gson.reflect.TypeToken
import io.github.chenfei0928.bean.DataA
import io.github.chenfei0928.bean.DataB
import io.github.chenfei0928.bean.DataInterface
import io.github.chenfei0928.lang.arrayClass

/**
 * @author chenf()
 * @date 2023-10-20 16:12
 */
@Suppress("UNCHECKED_CAST")
open class KI<E> {
    open val gsonTypeToken: TypeToken<E> = object : TypeToken<E>() {}
    open val eClass: Class<E> = Any::class.java as Class<E>
    open val demoDataOrNull: E? = null

    /**
     * 子类直接实现一阶数组 Class: Int[]
     * <pre>
     * Class
    </pre> *
     */
    class IIntArray : KI<IntArray>() {
        override val gsonTypeToken = object : TypeToken<IntArray>() {}
        override val eClass = IntArray::class.java
        override val demoDataOrNull = intArrayOf(1, 2, 3)
    }

    /**
     * 子类直接实现二阶或多阶数组 GenericArrayType
     * <pre>
     * GenericArrayType
     * Class
    </pre> *
     */
    class IntArrayArray : KI<Array<IntArray>>() {
        override val gsonTypeToken = object : TypeToken<Array<IntArray>>() {}
        override val eClass = Array<IntArray>::class.java
        override val demoDataOrNull = arrayOf(intArrayOf(1, 2, 3), intArrayOf(4, 5, 6))
    }

    /**
     * 子类直接实现泛型元素类型 (Class)
     * <pre>
     * Class
    </pre> *
     */
    class IView : KI<DataA>() {
        override val gsonTypeToken = object : TypeToken<DataA>() {}
        override val eClass = DataA::class.java
        override val demoDataOrNull = DataA(5)
    }

    /**
     * 子类的范型约束ChildP虽然是一个Interface或Class，但其仍有范型定义
     * 但由于泛型擦除，只能获取到MutableList.class
     * <pre>
     * ParameterizedType
     * </pre>
     */
    open class IList<E> : KI<MutableList<E>>() {
        override val gsonTypeToken = object : TypeToken<MutableList<E>>() {}
        override val eClass = MutableList::class.java as Class<MutableList<E>>

        class IListView : IList<DataA>() {
            override val gsonTypeToken: TypeToken<MutableList<DataA>> =
                object : TypeToken<MutableList<DataA>>() {}
            override val demoDataOrNull: MutableList<DataA> =
                arrayListOf(DataA(1), DataA(2))
        }
    }

    /**
     * 数组范型，还要查找子类中该范型数组元素的具体实现
     * 但由于泛型擦除，只能获取到 MutableList[].class
     * <pre>
     * GenericArrayType
     * ParameterizedType
    </pre> *
     */
    open class IListEArray<E> : KI<Array<MutableList<E>>>() {
        override val gsonTypeToken: TypeToken<Array<MutableList<E>>> =
            object : TypeToken<Array<MutableList<E>>>() {}
        override val eClass: Class<Array<MutableList<E>>> =
            MutableList::class.java.arrayClass() as Class<Array<MutableList<E>>>

        /**
         * 泛型擦除，只能获取到 MutableList[].class
         */
        class IView : IListEArray<DataA>() {
            override val gsonTypeToken: TypeToken<Array<MutableList<DataA>>> =
                object : TypeToken<Array<MutableList<DataA>>>() {}
            override val demoDataOrNull: Array<MutableList<DataA>> = arrayOf(
                arrayListOf(DataA(1), DataA(2)),
                arrayListOf(DataA(2), DataA(3))
            )
        }
    }

    /**
     * 这一层子类只实现了范型为数组（且未约束范围），但数组元素仍由范型约束由子类提供
     * <pre>
     * GenericArrayType
     * ...
    </pre> *
     */
    open class IEArray<E> : KI<Array<E>>() {
        override val gsonTypeToken: TypeToken<Array<E>> = object : TypeToken<Array<E>>() {}
        override val eClass: Class<Array<E>> = Array<Any>::class.java as Class<Array<E>>

        /**
         * 中间父类只实现了范型为数组（且未约束范围），数组元素由子类提供
         * <pre>
         * GenericArrayType
         * Class
         * </pre>
         * View[].class
         */
        class ViewArray : IEArray<DataA>() {
            override val gsonTypeToken: TypeToken<Array<DataA>> =
                object : TypeToken<Array<DataA>>() {}
            override val eClass: Class<Array<DataA>> =
                Array<DataA>::class.java
            override val demoDataOrNull: Array<DataA> =
                arrayOf(DataA(1), DataA(2))
        }
    }

    /**
     * 子类继续范型约束该类型，要继续向子类查找该范型在子类中的实现
     * <pre>
     * TypeVariable
     * ...
    </pre> *
     */
    open class II<E : DataInterface> : KI<E>() {
        override val gsonTypeToken: TypeToken<E> = object : TypeToken<E>() {}
        override val eClass: Class<E> = DataInterface::class.java as Class<E>

        class IIViewGroup : II<DataB>() {
            override val gsonTypeToken: TypeToken<DataB> = object : TypeToken<DataB>() {}
            override val eClass: Class<DataB> = DataB::class.java
            override val demoDataOrNull: DataB = DataB(2)
        }

        class IIView : II<DataA>() {
            override val gsonTypeToken: TypeToken<DataA> = object : TypeToken<DataA>() {}
            override val eClass: Class<DataA> = DataA::class.java
            override val demoDataOrNull: DataA = DataA(1)
        }
    }

    open class I1<E, T : List<E>> : KI<T>() {
        override val gsonTypeToken: TypeToken<T> = object : TypeToken<T>() {}
        override val eClass: Class<T> = List::class.java as Class<T>

        open class IM<E, T : MutableList<E>> : I1<E, T>() {
            override val gsonTypeToken: TypeToken<T> = object : TypeToken<T>() {}
            override val eClass: Class<T> = MutableList::class.java as Class<T>
        }

        class IArrayList : IM<Any, ArrayList<Any>>() {
            override val gsonTypeToken: TypeToken<ArrayList<Any>> =
                object : TypeToken<ArrayList<Any>>() {}
            override val eClass: Class<ArrayList<Any>> =
                ArrayList::class.java as Class<ArrayList<Any>>
        }
    }

    open class IMap<K, V> : KI<MutableMap<K, V>>() {
        override val gsonTypeToken: TypeToken<MutableMap<K, V>> =
            object : TypeToken<MutableMap<K, V>>() {}
        override val eClass: Class<MutableMap<K, V>> = Map::class.java as Class<MutableMap<K, V>>

        class IMapIntView : IMap<Int, DataA>() {
            override val gsonTypeToken: TypeToken<MutableMap<Int, DataA>> =
                object : TypeToken<MutableMap<Int, DataA>>() {}
            override val demoDataOrNull: MutableMap<Int, DataA> = mutableMapOf(
                1 to DataA(1), 2 to DataA(2)
            )
        }

        class IMapIntArrayView : IMap<String, IntArray>() {
            override val gsonTypeToken: TypeToken<MutableMap<String, IntArray>> =
                object : TypeToken<MutableMap<String, IntArray>>() {}
            override val demoDataOrNull: MutableMap<String, IntArray> = mutableMapOf(
                "123" to intArrayOf(1, 2, 3)
            )
        }
    }
}
