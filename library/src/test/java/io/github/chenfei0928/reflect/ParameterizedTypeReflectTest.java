package io.github.chenfei0928.reflect;


import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.chenfei0928.bean.DataA;
import io.github.chenfei0928.bean.DataB;
import io.github.chenfei0928.bean.DataInterface;
import io.github.chenfei0928.reflect.parameterized.ParameterizedTypeReflect1;
import kotlin.collections.CollectionsKt;

import static org.junit.Assert.assertEquals;

/**
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2021-03-18 11:31
 */
public class ParameterizedTypeReflectTest {

    @org.junit.Test
    public void testParam() {
        test(I.class, Object.class);
        // 之类直接实现/数组实现
        test(I.IntArray.class, int[].class);
        test(I.IntArrayArray.class, int[][].class);
        test(I.IView.class, DataA.class);
        // 子类明确了泛型约束，受限于泛型擦除，只能获取到其Class
        test(IList.class, List.class);
        test(IList.IListView.class, (Class<List<DataA>>) ((Class) List.class));
        test(IList.IListEmpty.class, List.class);
        // 数组范型，还要查找子类中该范型数组元素的具体实现
        test(IListEArray.class, List[].class);
        test(IListEArray.IView.class, (Class<List<DataA>[]>) ((Class) List[].class));
        // 子类只实现了范型为数组（且未约束范围），但数组元素仍由范型约束由子类提供
        test(IEArray.class, Object[].class);
        test(IEArray.ViewArray.class, DataA[].class);
        // 子类继续范型约束该类型，要继续向子类查找该范型在子类中的实现
        test(II.class, DataInterface.class);
        test(II.IIViewGroup.class, DataB.class);
        test(II.IIView.class, DataA.class);
        test(II.Any.class, DataInterface.class);
        // 中间接口测试
        test(I1.class, List.class);
        test(I1.ArrayList.class, (Class<ArrayList<Object>>) ((Class) ArrayList.class));
        Class<Object> arrayListClass = new ParameterizedTypeReflect1(I1.class, I1.ArrayList.class, 1)
                .getParentParameterizedTypeDefinedImplInChild();
        assertEquals(arrayListClass, ArrayList.class);
        Class<Object> listClass = new ParameterizedTypeReflect1(I1.class, I1.class, 1)
                .getParentParameterizedTypeDefinedImplInChild();
        assertEquals(listClass, List.class);
        // Map
        test(IMap.class, Map.class);
        test(IMap.IMapIntView.class, (Class<Map<Integer, DataA>>) ((Class) Map.class));
        test(IMap.IMapIntArrayView.class, (Class<Map<String, int[]>>) ((Class) Map.class));
    }

    private static <IInterface extends I<R>, R> void test(Class<IInterface> finalChildClass, Class<R> paramsType) {
        ParameterizedTypeReflect1<I<R>, IInterface, R> reflect = new ParameterizedTypeReflect1(I.class, finalChildClass, 0);
        Class<R> reflectParentParameterizedTypeDefinedImplInChild = reflect.getParentParameterizedTypeDefinedImplInChild();
        assertEquals(reflectParentParameterizedTypeDefinedImplInChild, paramsType);
    }

    @org.junit.Test
    public void testParam1() {
        test1(I.class, Object.class, null);
        // 之类直接实现/数组实现
        test1(I.IntArray.class, int[].class, new int[]{1, 2, 3});
        test1(I.IntArrayArray.class, int[][].class, new int[][]{{1, 2, 3}, {4, 5, 6}});
        test1(I.IView.class, DataA.class, new DataA(5));
        // 子类明确了泛型约束，受限于泛型擦除，只能获取到其Class
        test1(IList.class, List.class, null);
        test1(IList.IListView.class, (Class<List<DataA>>) ((Class) List.class),
                CollectionsKt.arrayListOf(new DataA[]{new DataA(1), new DataA(2)})
        );
        test1(IList.IListEmpty.class, List.class, null);
        // 数组范型，还要查找子类中该范型数组元素的具体实现
        test1(IListEArray.class, List[].class, null);
        test1(IListEArray.IView.class, (Class<List<DataA>[]>) ((Class) List[].class), new List[]{
                CollectionsKt.arrayListOf(new DataA[]{new DataA(1), new DataA(2)}),
                CollectionsKt.arrayListOf(new DataA[]{new DataA(2), new DataA(3)})
        });
        // 子类只实现了范型为数组（且未约束范围），但数组元素仍由范型约束由子类提供
        test1(IEArray.class, Object[].class, null);
        test1(IEArray.ViewArray.class, DataA[].class, new DataA[]{new DataA(1), new DataA(2)});
        // 子类继续范型约束该类型，要继续向子类查找该范型在子类中的实现
        test1(II.class, DataInterface.class, null);
        test1(II.IIViewGroup.class, DataB.class, new DataB(2));
        test1(II.IIView.class, DataA.class, new DataA(1));
        test1(II.Any.class, DataInterface.class, null);
        // 中间接口测试
        test1(I1.class, List.class, null);
        test1(I1.ArrayList.class, (Class<ArrayList<Object>>) ((Class) ArrayList.class), null);
        // Map
        test1(IMap.class, Map.class, null);
        test1(IMap.IMapIntView.class, (Class<Map<Integer, DataA>>) ((Class) Map.class), Map.of(
                1, new DataA(1), 2, new DataA(2)
        ));
        test1(IMap.IMapIntArrayView.class, (Class<Map<String, int[]>>) ((Class) Map.class), Map.of(
                "123", new int[]{1, 2, 3}
        ));
    }

    private static final Gson gson = new Gson();

    private static <IInterface extends I<R>, R> void test1(Class<IInterface> finalChildClass, Class<R> paramsType, R data) {
        Type type = new ParameterizedTypeReflect1(I.class, finalChildClass, 0).getType();
        System.out.println(finalChildClass.getName() + " : " + type);
        if (data != null) {
            String json = gson.toJson(data);
            Object obj = gson.fromJson(json, (Type) type);
            assertEquals(json, gson.toJson(obj));
        }
    }
}

interface I<E> {
    /**
     * 子类直接实现一阶数组 Class: Int[]
     * <pre>
     *     Class
     * </pre>
     */
    class IntArray implements I<int[]> {
    }

    /**
     * 子类直接实现二阶或多阶数组 GenericArrayType
     * <pre>
     *     GenericArrayType
     *         Class
     * </pre>
     */
    class IntArrayArray implements I<int[][]> {
    }

    /**
     * 子类直接实现泛型元素类型 (Class<?>)
     * <pre>
     *     Class
     * </pre>
     */
    class IView implements I<DataA> {
    }
}

/**
 * 子类的范型约束ChildP虽然是一个Interface或Class，但其仍有范型定义
 * 但由于泛型擦除，只能获取到List.class
 * <pre>
 *     ParameterizedType
 * </pre>
 */
interface IList<E> extends I<List<E>> {
    class IListView implements IList<DataA> {
    }

    class IListEmpty implements IList {
    }
}

/**
 * 数组范型，还要查找子类中该范型数组元素的具体实现
 * 但由于泛型擦除，只能获取到 List[].class
 * <pre>
 *     GenericArrayType
 *         ParameterizedType
 * </pre>
 */
interface IListEArray<E> extends I<List<E>[]> {
    /**
     * 泛型擦除，只能获取到 List[].class
     */
    class IView implements IListEArray<DataA> {
    }
}

/**
 * 这一层子类只实现了范型为数组（且未约束范围），但数组元素仍由范型约束由子类提供
 * <pre>
 *     GenericArrayType
 *         ...
 * </pre>
 */
interface IEArray<E> extends I<E[]> {
    /**
     * 中间父类只实现了范型为数组（且未约束范围），数组元素由子类提供
     * <pre>
     *     GenericArrayType
     *         Class
     * </pre>
     * View[].class
     */
    class ViewArray implements IEArray<DataA> {
    }
}

/**
 * 子类继续范型约束该类型，要继续向子类查找该范型在子类中的实现
 * <pre>
 *     TypeVariable
 *         ...
 * </pre>
 */
interface II<E extends DataInterface> extends I<E> {

    class IIViewGroup implements II<DataB> {
    }

    class IIView implements II<DataA> {
    }

    class Any implements II {
    }
}

interface I1<E, T extends List<E>> extends I<T> {
    class ArrayList implements I1<Object, java.util.ArrayList<Object>> {
    }
}

interface IMap<K, V> extends I<Map<K, V>> {

    class IMapIntView implements IMap<Integer, DataA> {
    }

    class IMapIntArrayView implements IMap<String, int[]> {
    }
}
