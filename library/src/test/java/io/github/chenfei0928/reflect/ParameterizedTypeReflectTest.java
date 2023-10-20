package io.github.chenfei0928.reflect;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
        test(new I());
        // 之类直接实现/数组实现
        test(new I.IntArray());
        test(new I.IntArrayArray());
        test(new I.IView());
        // 子类明确了泛型约束，受限于泛型擦除，只能获取到其Class
        test(new IList());
        test(new IList.IListView());
        test(new IList.IListEmpty());
        // 数组范型，还要查找子类中该范型数组元素的具体实现
        test(new IListEArray());
        test(new IListEArray.IView());
        // 子类只实现了范型为数组（且未约束范围），但数组元素仍由范型约束由子类提供
        test(new IEArray());
        test(new IEArray.ViewArray());
        // 子类继续范型约束该类型，要继续向子类查找该范型在子类中的实现
        test(new II());
        test(new II.IIViewGroup());
        test(new II.IIView());
        test(new II.Any());
        // 中间接口测试
        test(new I1());
        test(new I1.ArrayList());
        Class<Object> arrayListClass = new ParameterizedTypeReflect1(I1.class, I1.ArrayList.class)
                .getParentParameterizedTypeDefinedImplInChild(1);
        assertEquals(arrayListClass, ArrayList.class);
        Class<Object> listClass = new ParameterizedTypeReflect1(I1.class, I1.class)
                .getParentParameterizedTypeDefinedImplInChild(1);
        assertEquals(listClass, List.class);
        // Map
        test(new IMap());
        test(new IMap.IMapIntView());
        test(new IMap.IMapIntArrayView());
    }

    private static final Gson gson = new Gson();

    private static <IInterface extends I<R>, R> void test(
            IInterface finalChildClassInstance
    ) {
        Class<IInterface> finalChildClass = (Class<IInterface>) finalChildClassInstance.getClass();

        // 判断获取泛型被擦除后的Class
        ParameterizedTypeReflect1<I<R>> reflect = new ParameterizedTypeReflect1(I.class, finalChildClass);
        Class<R> reflectParentParameterizedTypeDefinedImplInChild =
                reflect.getParentParameterizedTypeDefinedImplInChild(0);
        Class<R> paramsType = finalChildClassInstance.getEClass();
        assertEquals(reflectParentParameterizedTypeDefinedImplInChild, paramsType);

        // 获取泛型的Type
        Type refType = new ParameterizedTypeReflect1<>(I.class, finalChildClass).getType(0);
        Type gsonType = finalChildClassInstance.getGsonTypeToken().getType();
        // 此处对Xxx[]类型时二者获取到的数据不一致，不做测试断言处理，仅打印
        // int[] 时，refType为 class [I 的虚拟机Class类
        // gsonType为 int[][] 的GsonType创建的GenericArrayTypeImpl类
        // 而GsonType没有对 class [I 调用equals的情况进行处理
        // 既 com.google.gson.internal.$Gson$Types.arrayOf(int.class).equals(int[].class) 会返回 false
        System.out.println(finalChildClass.getSimpleName() + ": " + refType + ", gson: " + gsonType);
//        assertEquals(finalChildClassInstance.getType().getType(), type);

        // 检查gson反序列化
        R data = finalChildClassInstance.getDemoDataOrNull();
        if (data != null) {
            String json = gson.toJson(data);
            Object obj = gson.fromJson(json, (Type) refType);
            assertEquals(json, gson.toJson(obj));
        }
    }

    //<editor-fold desc="用例所用Data类" defaultstatus="collapsed">
    static class I<E> {

        TypeToken<E> getGsonTypeToken() {
            return new TypeToken<E>() {
            };
        }

        Class<E> getEClass() {
            return (Class<E>) Object.class;
        }

        E getDemoDataOrNull() {
            return null;
        }

        /**
         * 子类直接实现一阶数组 Class: Int[]
         * <pre>
         *     Class
         * </pre>
         */
        static class IntArray extends I<int[]> {
            @Override
            TypeToken<int[]> getGsonTypeToken() {
                return new TypeToken<int[]>() {
                };
            }

            @Override
            Class<int[]> getEClass() {
                return int[].class;
            }

            @Override
            int[] getDemoDataOrNull() {
                return new int[]{1, 2, 3};
            }
        }

        /**
         * 子类直接实现二阶或多阶数组 GenericArrayType
         * <pre>
         *     GenericArrayType
         *         Class
         * </pre>
         */
        static class IntArrayArray extends I<int[][]> {
            @Override
            TypeToken<int[][]> getGsonTypeToken() {
                return new TypeToken<int[][]>() {
                };
            }

            @Override
            Class<int[][]> getEClass() {
                return int[][].class;
            }

            @Override
            int[][] getDemoDataOrNull() {
                return new int[][]{{1, 2, 3}, {4, 5, 6}};
            }
        }

        /**
         * 子类直接实现泛型元素类型 (Class<?>)
         * <pre>
         *     Class
         * </pre>
         */
        static class IView extends I<DataA> {
            @Override
            TypeToken<DataA> getGsonTypeToken() {
                return new TypeToken<DataA>() {
                };
            }

            @Override
            Class<DataA> getEClass() {
                return DataA.class;
            }

            @Override
            DataA getDemoDataOrNull() {
                return new DataA(5);
            }
        }
    }

    /**
     * 子类的范型约束ChildP虽然是一个Interface或Class，但其仍有范型定义
     * 但由于泛型擦除，只能获取到List.class
     * <pre>
     *     ParameterizedType
     * </pre>
     */
    static class IList<E> extends I<List<E>> {

        @Override
        TypeToken<List<E>> getGsonTypeToken() {
            return new TypeToken<List<E>>() {
            };
        }

        @Override
        Class<List<E>> getEClass() {
            return (Class<List<E>>) ((Class) List.class);
        }

        static class IListView extends IList<DataA> {
            @Override
            TypeToken<List<DataA>> getGsonTypeToken() {
                return new TypeToken<List<DataA>>() {
                };
            }

            @Override
            Class<List<DataA>> getEClass() {
                return (Class<List<DataA>>) ((Class) List.class);
            }

            @Override
            List<DataA> getDemoDataOrNull() {
                return CollectionsKt.arrayListOf(new DataA[]{new DataA(1), new DataA(2)});
            }
        }

        static class IListEmpty extends IList {
            @Override
            Class<List> getEClass() {
                return List.class;
            }
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
    static class IListEArray<E> extends I<List<E>[]> {
        @Override
        TypeToken<List<E>[]> getGsonTypeToken() {
            return new TypeToken<List<E>[]>() {
            };
        }

        @Override
        Class<List<E>[]> getEClass() {
            return (Class<List<E>[]>) ((Class) List[].class);
        }

        /**
         * 泛型擦除，只能获取到 List[].class
         */
        static class IView extends IListEArray<DataA> {
            @Override
            TypeToken<List<DataA>[]> getGsonTypeToken() {
                return new TypeToken<List<DataA>[]>() {
                };
            }

            @Override
            Class<List<DataA>[]> getEClass() {
                return (Class<List<DataA>[]>) ((Class) List[].class);
            }

            @Override
            List<DataA>[] getDemoDataOrNull() {
                return new List[]{
                        CollectionsKt.arrayListOf(new DataA[]{new DataA(1), new DataA(2)}),
                        CollectionsKt.arrayListOf(new DataA[]{new DataA(2), new DataA(3)})
                };
            }
        }
    }

    /**
     * 这一层子类只实现了范型为数组（且未约束范围），但数组元素仍由范型约束由子类提供
     * <pre>
     *     GenericArrayType
     *         ...
     * </pre>
     */
    static class IEArray<E> extends I<E[]> {
        @Override
        TypeToken<E[]> getGsonTypeToken() {
            return new TypeToken<E[]>() {
            };
        }

        @Override
        Class<E[]> getEClass() {
            return (Class) Object[].class;
        }

        /**
         * 中间父类只实现了范型为数组（且未约束范围），数组元素由子类提供
         * <pre>
         *     GenericArrayType
         *         Class
         * </pre>
         * View[].class
         */
        static class ViewArray extends IEArray<DataA> {
            @Override
            TypeToken<DataA[]> getGsonTypeToken() {
                return new TypeToken<DataA[]>() {
                };
            }

            @Override
            Class<DataA[]> getEClass() {
                return DataA[].class;
            }

            @Override
            DataA[] getDemoDataOrNull() {
                return new DataA[]{new DataA(1), new DataA(2)};
            }
        }
    }

    /**
     * 子类继续范型约束该类型，要继续向子类查找该范型在子类中的实现
     * <pre>
     *     TypeVariable
     *         ...
     * </pre>
     */
    static class II<E extends DataInterface> extends I<E> {
        @Override
        TypeToken<E> getGsonTypeToken() {
            return new TypeToken<E>() {
            };
        }

        @Override
        Class<E> getEClass() {
            return (Class<E>) DataInterface.class;
        }

        static class IIViewGroup extends II<DataB> {
            @Override
            TypeToken<DataB> getGsonTypeToken() {
                return new TypeToken<DataB>() {
                };
            }

            @Override
            Class<DataB> getEClass() {
                return DataB.class;
            }

            @Override
            DataB getDemoDataOrNull() {
                return new DataB(2);
            }
        }

        static class IIView extends II<DataA> {
            @Override
            TypeToken<DataA> getGsonTypeToken() {
                return new TypeToken<DataA>() {
                };
            }

            @Override
            Class<DataA> getEClass() {
                return DataA.class;
            }

            @Override
            DataA getDemoDataOrNull() {
                return new DataA(1);
            }
        }

        static class Any extends II {
            @Override
            Class getEClass() {
                return DataInterface.class;
            }
        }
    }

    static class I1<E, T extends List<E>> extends I<T> {
        @Override
        TypeToken<T> getGsonTypeToken() {
            return new TypeToken<T>() {
            };
        }

        @Override
        Class<T> getEClass() {
            return (Class<T>) ((Class) List.class);
        }

        static class ArrayList extends I1<Object, java.util.ArrayList<Object>> {
            @Override
            TypeToken<java.util.ArrayList<Object>> getGsonTypeToken() {
                return new TypeToken<java.util.ArrayList<Object>>() {
                };
            }

            @Override
            Class<java.util.ArrayList<Object>> getEClass() {
                return (Class<java.util.ArrayList<Object>>) ((Class) java.util.ArrayList.class);
            }
        }
    }

    static class IMap<K, V> extends I<Map<K, V>> {
        @Override
        TypeToken<Map<K, V>> getGsonTypeToken() {
            return new TypeToken<Map<K, V>>() {
            };
        }

        @Override
        Class<Map<K, V>> getEClass() {
            return (Class<Map<K, V>>) ((Class) Map.class);
        }

        static class IMapIntView extends IMap<Integer, DataA> {
            @Override
            TypeToken<Map<Integer, DataA>> getGsonTypeToken() {
                return new TypeToken<Map<Integer, DataA>>() {
                };
            }

            @Override
            Class<Map<Integer, DataA>> getEClass() {
                return (Class<Map<Integer, DataA>>) ((Class) Map.class);
            }

            @Override
            Map<Integer, DataA> getDemoDataOrNull() {
                return Map.of(
                        1, new DataA(1), 2, new DataA(2)
                );
            }
        }

        static class IMapIntArrayView extends IMap<String, int[]> {
            @Override
            TypeToken<Map<String, int[]>> getGsonTypeToken() {
                return new TypeToken<Map<String, int[]>>() {
                };
            }

            @Override
            Class<Map<String, int[]>> getEClass() {
                return (Class<Map<String, int[]>>) ((Class) Map.class);
            }

            @Override
            Map<String, int[]> getDemoDataOrNull() {
                return Map.of(
                        "123", new int[]{1, 2, 3}
                );
            }
        }
    }
    //</editor-fold>
}
