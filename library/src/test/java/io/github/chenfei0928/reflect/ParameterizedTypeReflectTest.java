package io.github.chenfei0928.reflect;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

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
        test(I.View.class, android.view.View.class);
        // 子类明确了泛型约束，受限于泛型擦除，只能获取到其Class
        test(IList.class, List.class);
        test(IList.IListView.class, (Class<List<View>>) ((Class) List.class));
        test(IList.IListEmpty.class, List.class);
        // 数组范型，还要查找子类中该范型数组元素的具体实现
        test(IListEArray.class, List[].class);
        test(IListEArray.View.class, (Class<List<View>[]>) ((Class) List[].class));
        // 子类只实现了范型为数组（且未约束范围），但数组元素仍由范型约束由子类提供
        test(IEArray.class, Object[].class);
        test(IEArray.ViewArray.class, android.view.View[].class);
        // 子类继续范型约束该类型，要继续向子类查找该范型在子类中的实现
        test(II.class, android.view.View.class);
        test(II.ViewGroup.class, android.view.ViewGroup.class);
        test(II.View.class, android.view.View.class);
        test(II.Any.class, android.view.View.class);
        // 中间接口测试
        test(I1.class, List.class);
        test(I1.ArrayList.class, (Class<ArrayList<Object>>) ((Class) ArrayList.class));
        Class<Object> arrayListClass = new ParameterizedTypeReflect1(I1.class, I1.ArrayList.class, 1)
                .getParentParameterizedTypeDefinedImplInChild();
        assertEquals(arrayListClass, ArrayList.class);
        Class<Object> listClass = new ParameterizedTypeReflect1(I1.class, I1.class, 1)
                .getParentParameterizedTypeDefinedImplInChild();
        assertEquals(listClass, List.class);
    }

    private static <IInterface extends I<R>, R> void test(Class<IInterface> finalChildClass, Class<R> paramsType) {
        ParameterizedTypeReflect1<I<R>, IInterface, R> reflect = new ParameterizedTypeReflect1(I.class, finalChildClass, 0);
        Class<R> reflectParentParameterizedTypeDefinedImplInChild = reflect.getParentParameterizedTypeDefinedImplInChild();
        assertEquals(reflectParentParameterizedTypeDefinedImplInChild, paramsType);
    }
}

interface I1<E, T extends List<E>> extends I<T> {
    class ArrayList implements I1<Object, java.util.ArrayList<Object>> {
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
    class View implements I<android.view.View> {
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
    class IListView implements IList<android.view.View> {
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
    class View implements IListEArray<android.view.View> {
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
    class ViewArray implements IEArray<android.view.View> {
    }
}

/**
 * 子类继续范型约束该类型，要继续向子类查找该范型在子类中的实现
 * <pre>
 *     TypeVariable
 *         ...
 * </pre>
 */
interface II<E extends android.view.View> extends I<E> {

    class ViewGroup implements II<android.view.ViewGroup> {
    }

    class View implements II<android.view.View> {
    }

    class Any implements II {
    }
}
