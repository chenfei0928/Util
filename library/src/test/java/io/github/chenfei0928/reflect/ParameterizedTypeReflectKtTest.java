package io.github.chenfei0928.reflect;

import java.util.ArrayList;
import java.util.List;

import io.github.chenfei0928.reflect.parameterized.ParameterizedTypeReflect1;
import io.github.chenfei0928.reflect.parameterized.ParameterizedTypeReflect2;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;

import static org.junit.Assert.assertEquals;

/**
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2021-03-18 11:31
 */
public class ParameterizedTypeReflectKtTest {

    @org.junit.Test
    public void testParam() {
        test(new I());
        // 之类直接实现/数组实现
        test(new I.IIntArray());
        test(new I.IntArrayArray());
        test(new I.IView());
        // 子类明确了泛型约束，受限于泛型擦除，只能获取到其Class
        test(new I.IList());
        test(new I.IList.IListView());
        test(new I.IList.IListEmpty());
        // 数组范型，还要查找子类中该范型数组元素的具体实现
        test(new I.IListEArray());
        test(new I.IListEArray.IView());
        // 子类只实现了范型为数组（且未约束范围），但数组元素仍由范型约束由子类提供
        test(new I.IEArray());
        test(new I.IEArray.ViewArray());
        // 子类继续范型约束该类型，要继续向子类查找该范型在子类中的实现
        test(new I.II());
        test(new I.II.IIViewGroup());
        test(new I.II.IIView());
        test(new I.II.Any());
        // 中间接口测试
        test(new I.I1());
        test(new I.I1.IArrayList());
        Class<Object> arrayListClass = new ParameterizedTypeReflect1(I.I1.class, I.I1.IArrayList.class)
                .getParentParameterizedTypeDefinedImplInChild(1);
        assertEquals(arrayListClass, ArrayList.class);
        Class<Object> listClass = new ParameterizedTypeReflect1(I.I1.class, I.I1.class)
                .getParentParameterizedTypeDefinedImplInChild(1);
        assertEquals(listClass, List.class);
        // Map
        test(new I.IMap());
        test(new I.IMap.IMapIntView());
        test(new I.IMap.IMapIntArrayView());
    }

    private static <IInterface extends I<R>, R> void test(
            IInterface finalChildClassInstance
    ) {
        KClass<IInterface> kClass = JvmClassMappingKt.getKotlinClass((Class<IInterface>) finalChildClassInstance.getClass());
        System.out.print(kClass.getSimpleName() + ": ");

        // 判断获取泛型被擦除后的Class
        KClass<I> ikClass = JvmClassMappingKt.getKotlinClass(I.class);
        ParameterizedTypeReflect2<I<R>> reflect = new ParameterizedTypeReflect2(ikClass, kClass);
        Class<R> refClass = reflect.getParentParameterizedTypeDefinedImplInChild(0);
        Class<R> overrideClass = finalChildClassInstance.getEClass();
        System.out.println("class by ref: " + refClass.getName() + ", by override:" + overrideClass.getName());
        assertEquals(refClass, overrideClass);
    }

    @org.junit.Test
    public void testParamKt() {
        // Kotlin 1.9.10 测试时，List、Map的泛型子类继承后泛型约束会丢失，MutableList、MutableMap则没问题
        testKt(new KI());
        // 之类直接实现/数组实现
        testKt(new KI.IIntArray());
        testKt(new KI.IntArrayArray());
        testKt(new KI.IView());
        // 子类明确了泛型约束，受限于泛型擦除，只能获取到其Class
        testKt(new KI.IList());
        testKt(new KI.IList.IListView());
//        testKt(new KI.IList.IListEmpty());
        // 数组范型，还要查找子类中该范型数组元素的具体实现
        testKt(new KI.IListEArray());
        testKt(new KI.IListEArray.IView());
        // 子类只实现了范型为数组（且未约束范围），但数组元素仍由范型约束由子类提供
        testKt(new KI.IEArray());
        testKt(new KI.IEArray.ViewArray());
        // 子类继续范型约束该类型，要继续向子类查找该范型在子类中的实现
        testKt(new KI.II());
        testKt(new KI.II.IIViewGroup());
        testKt(new KI.II.IIView());
//        testKt(new KI.II.Any());
        // 中间接口测试
        testKt(new KI.I1());
        testKt(new KI.I1.IArrayList());
        Class<Object> arrayListClass = new ParameterizedTypeReflect1(KI.I1.class, KI.I1.IArrayList.class)
                .getParentParameterizedTypeDefinedImplInChild(1);
        assertEquals(arrayListClass, ArrayList.class);
        Class<Object> listClass = new ParameterizedTypeReflect1(KI.I1.class, KI.I1.class)
                .getParentParameterizedTypeDefinedImplInChild(1);
        assertEquals(listClass, List.class);
        // Map
        testKt(new KI.IMap());
        testKt(new KI.IMap.IMapIntView());
        testKt(new KI.IMap.IMapIntArrayView());
    }

    private static <IInterface extends KI<R>, R> void testKt(
            IInterface finalChildClassInstance
    ) {
        KClass<IInterface> kClass = JvmClassMappingKt.getKotlinClass((Class<IInterface>) finalChildClassInstance.getClass());
        System.out.print(kClass.getSimpleName() + ": ");

        // 判断获取泛型被擦除后的Class
        KClass<KI> ikClass = JvmClassMappingKt.getKotlinClass(KI.class);
        ParameterizedTypeReflect2<I<R>> reflect = new ParameterizedTypeReflect2(ikClass, kClass);
        Class<R> refClass = reflect.getParentParameterizedTypeDefinedImplInChild(0);
        Class<R> overrideClass = finalChildClassInstance.getEClass();
        System.out.println("class by ref: " + refClass.getName() + ", by override:" + overrideClass.getName());
        assertEquals(refClass, overrideClass);
    }
}
