package io.github.chenfei0928.reflect;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.chenfei0928.bean.DataA;
import io.github.chenfei0928.bean.DataB;
import io.github.chenfei0928.bean.DataInterface;
import kotlin.collections.CollectionsKt;

/**
 * 用例所用Data类
 *
 * @author chenf()
 * @date 2024-07-08 18:22
 */
class I<E> {

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
    static class IIntArray extends I<int[]> {
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

        static class IArrayList extends I1<Object, ArrayList<Object>> {
            @Override
            TypeToken<ArrayList<Object>> getGsonTypeToken() {
                return new TypeToken<java.util.ArrayList<Object>>() {
                };
            }

            @Override
            Class<ArrayList<Object>> getEClass() {
                return (Class<ArrayList<Object>>) ((Class) ArrayList.class);
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
}
