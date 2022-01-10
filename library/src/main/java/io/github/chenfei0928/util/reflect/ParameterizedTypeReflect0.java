package io.github.chenfei0928.util.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import io.github.chenfei0928.util.kotlin.ReflectKt;

/**
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2020-10-21 18:18
 */
public class ParameterizedTypeReflect0 {

    /**
     * 获取子类在父类中实现的指定下标的范型类型，可以在不添加抽象方法时获取子类所实现的范型类型
     * {@code
     * Parent<R>
     * Child extends Parent<SomeType>
     * }
     *
     * @param <Parent>                  父类类型
     * @param <Child>                   子类类型
     * @param parentClass               父类类实例
     * @param finalChildClass           最终子类类实例
     * @param positionInParentParameter 要获取的父类声明范型的指定下标
     * @return 在子类实现的父类中指定声明的类型
     */
    public static <Parent, Child extends Parent, R> TypeBoundsContract<R> getParentParameterizedTypeDefinedImplInChild(
            Class<Parent> parentClass,
            Class<Child> finalChildClass,
            int positionInParentParameter) {
        // 要获取的类型声明
        TypeVariable<?> parentTypeParameter = parentClass.getTypeParameters()[positionInParentParameter];
        // 如果查找的是当前类，不需要从继承链上查找，直接返回当前类的泛型约束
        if (parentClass == finalChildClass) {
            return findParameterizedTypeDefinedImplInChild(parentClass, finalChildClass, parentTypeParameter);
        }
        // 从子类开始向上迭代查找超类，直到到达父类为止，并获取父类的直接子类
        ParentParameterizedTypeNode childClassNode = getParentTypeDefinedImplInChild(parentClass, finalChildClass).first;
        // 从父类到子类查找过程中的类型边界约束
        TypeBoundsContract<R> parentTypeBoundsContract = null;
        // 从父类的直接子类开始向最后实现类遍历
        for (ParentParameterizedTypeNode childClass = childClassNode;
             childClass != null;
             childClass = childClass.childNode) {
            // 获取其所实现的父类的范型定义
            Type genericSuperclass = childClass.getGenericSuperclass();
            // 如果当前节点没有指定（忽略了）范型化参数，直接返回当前节点父类的范型约束
            if (!(genericSuperclass instanceof ParameterizedType)) {
                return parentTypeBoundsContract;
            }
            Type childActualTypeArgument = ((ParameterizedType) genericSuperclass)
                    .getActualTypeArguments()[positionInParentParameter];
            if (childActualTypeArgument instanceof Class<?>) {
                // 找到了所需范型类型
                // Child extends Parent<SomeType>
                return new TypeBoundsContract<>((Class<R>) childActualTypeArgument, null, null);
            } else if (childActualTypeArgument instanceof TypeVariable<?>) {
                // 子类继续范型约束该类型，要继续向子类查找该范型在子类中的实现，如：
                // Child<R> extends Parent<R>
                // Child<R extends XXX> extends Parent<R>
                // Child<R super XXX> extends Parent<R>
                parentTypeParameter = (TypeVariable<?>) childActualTypeArgument;
//                return findParameterizedTypeDefinedImplInChild(
//                        (Class<Parent>) childClass.nodeClass, finalChildClass, parentTypeParameter);
                // 查找该范型在该类中的声明下标
                TypeVariable<? extends Class<?>>[] childClassTypeParameters = childClass.getTypeParameters();
                for (int i = 0; i < childClassTypeParameters.length; i++) {
                    TypeVariable<? extends Class<?>> childClassTypeParameter = childClassTypeParameters[i];
                    // 如果其范型名称相同，就是找到了指定的范型，此时 childClassTypeParameter 就是子类要求的范型约束范围
                    if (childClassTypeParameter.getName().equals(parentTypeParameter.getName())) {
                        // 先将当前子类的范型约谁范围保存下来，以备当前子类的子类未实现范型约束情况
                        parentTypeBoundsContract = new TypeBoundsContract<>(null,
                                (TypeVariable<? extends Class<R>>) childClassTypeParameter, null);
                        // 如果这一层就是最终子类的类型定义
                        if (finalChildClass == childClass.nodeClass) {
                            return parentTypeBoundsContract;
                        } else {
                            positionInParentParameter = i;
                        }
                    }
                }
            } else if (childActualTypeArgument instanceof GenericArrayType) {
                // 如果是数组范型，还要查找子类中该范型数组元素的具体实现
                // Child extends Parent<XXX[]>
                return findParameterizedArrayTypeDefinedImplInChild(
                        (Class<? super Child>) childClass.nodeClass, finalChildClass, (GenericArrayType) childActualTypeArgument);
            } else if (childActualTypeArgument instanceof ParameterizedType) {
                // 子类的范型约束ChildP虽然是一个Interface或Class，但其仍有范型定义
                // Child<ChildR> extends Parent<ChildParameterized<ChildR>>
                ParameterizedType parameterizedType = (ParameterizedType) childActualTypeArgument;
                // 由于有范型擦除机制，此处定义的 ChildR 在 ChildP<ChildR> 中会被擦除，无法维持到运行时，只获取ChildR的类型即可
                Class<R> childParameterizedClass = (Class<R>) parameterizedType.getRawType();
                return new TypeBoundsContract<>(childParameterizedClass, null, null);
            } else {
                throw new IllegalArgumentException("范型参数类型不匹配：" +
                        "\n父类：" + parentClass +
                        "\n最终子类：" + finalChildClass +
                        "\n当前子类：" + childClass +
                        "\n当前子类实现的父类中的范型定义：" + childActualTypeArgument + childActualTypeArgument.getClass() +
                        "\n当前子类的直接父类中定义的范型：" + parentTypeParameter);
            }
        }
        if (parentTypeBoundsContract != null) {
            return parentTypeBoundsContract;
        } else {
            throw new IllegalArgumentException("找不到指定的范型参数." +
                    " 父类：" + parentClass +
                    " 最终子类：" + finalChildClass +
                    " 最终子类中定义的范型：" + parentTypeParameter +
                    " 继承路径中的节点：" + childClassNode);
        }
    }

    /**
     * 当父类定义了一个数组范型约束，调用该方法来获取数组范型约束的实现
     * 如
     * {@code Child extends Parent<XXX[]>}
     * 该XXX可以为数组、范型约束或具体类
     *
     * @param parentClass     声明数组约束的父类类实例
     * @param finalChildClass 最终子类类实例
     * @param arrayType       父类的数组范型约束实现 XXX[]
     * @param <Parent>        声明数组约束的父类类型
     * @param <Child>         子类类型
     * @param <R>             父类类声明的范型类型
     * @return 子类实现的范型约束范围
     */
    private static <Parent, Child extends Parent, R> TypeBoundsContract<R> findParameterizedArrayTypeDefinedImplInChild(
            Class<Parent> parentClass,
            Class<Child> finalChildClass,
            GenericArrayType arrayType
    ) {
        // 如果是数组范型，还要查找子类中该范型数组元素的具体实现
        // Child extends Parent<XXX[]>
        Type genericComponentType = arrayType.getGenericComponentType();
        if (genericComponentType instanceof Class<?>) {
            // 如果当前类直接指定了范型元素类型
            // Child extends Parent<R-Element[]>
            Class<?> arrayClass = ReflectKt.arrayClass((Class<?>) genericComponentType);
            return new TypeBoundsContract(arrayClass, null, null);
        } else if (genericComponentType instanceof TypeVariable<?>) {
            // 这一层子类只实现了范型为数组（且未约束范围），但数组元素仍由范型约束由子类提供
            // Child<ChildR> extends Parent<ChildR[]>
            // 获取子类中实现的范型约束 ChildR
            TypeBoundsContract<Object> parameterizedTypeDefinedImplInChild = findParameterizedTypeDefinedImplInChild(
                    parentClass, finalChildClass, (TypeVariable<?>) genericComponentType);
            // 将生成ChildR的数组
            Class<?> arrayClass = ReflectKt.arrayClass(parameterizedTypeDefinedImplInChild.getClazz());
            return new TypeBoundsContract(arrayClass, null, null);
        } else if (genericComponentType instanceof GenericArrayType) {
            // 二阶或多阶数组
            // Child extends Parent<XXX[][]>
            TypeBoundsContract<Object> typeBoundsContract =
                    findParameterizedArrayTypeDefinedImplInChild(parentClass, finalChildClass, (GenericArrayType) genericComponentType);
            // 将生成ChildR的数组
            Class<?> arrayClass = ReflectKt.arrayClass(typeBoundsContract.getClazz());
            return new TypeBoundsContract(arrayClass, null, null);
        } else if (genericComponentType instanceof ParameterizedType) {
            // 子类的范型约束虽然是一个Interface或Class，但其仍有范型定义
            // Child<ChildR> extends Parent<List<ChildR>[]>
            ParameterizedType parameterizedType = (ParameterizedType) genericComponentType;
            // 将生成ChildR的数组
            // 由于有范型擦除机制，此处定义的 ChildR 在 List<ChildR>[] 中会被擦除，无法维持到运行时，只获取数组元素类型即可
            Class<?> arrayClass = ReflectKt.arrayClass((Class<?>) parameterizedType.getRawType());
            return new TypeBoundsContract(arrayClass, null, null);
        } else if (genericComponentType instanceof WildcardType) {
            // 范型约束只约束了范围，而没有继续约束实现，通常不会被类实现，或许为字段或方法（？）
            return new TypeBoundsContract(null, null, (WildcardType) genericComponentType);
        } else {
            throw new IllegalArgumentException("数组范型参数类型不匹配：" +
                    "定义范型约束的当前子类：" + parentClass +
                    "最终子类：" + finalChildClass +
                    "当前子类实现的父类中的范型定义：" + arrayType +
                    "当前子类的声明的数组范型约束的元素的范型：" + genericComponentType + genericComponentType.getClass());
        }
    }

    /**
     * 当父类定义了一个数组范型约束，但该数组范型约束元素类型又由范型约束提供，
     * 传入该数组元素范型约束和声明该数组元素范型约束的父类、最终子类类对象，
     * 来查找该数组范型约束元素的子类约束实现。
     * <p>
     * 如：
     * <pre>
     *     Child<R> extends Parent<R>
     *     Child<R extends XXX> extends Parent<R>
     *     Child<R super XXX> extends Parent<R>
     * </pre>
     *
     * @param parentClass               声明数组约束的父类类实例
     * @param finalChildClass           最终子类类实例
     * @param typeVariableInParentClass 父类实现的数组范型约束元素的范型约束声明 R
     * @param <Parent>                  声明数组约束的父类类型
     * @param <Child>                   子类类型
     * @param <R>                       父类类声明的范型类型
     * @return 子类实现的范型约束范围
     */
    private static <Parent, Child extends Parent, R> TypeBoundsContract<R> findParameterizedTypeDefinedImplInChild(
            Class<Parent> parentClass,
            Class<Child> finalChildClass,
            TypeVariable<?> typeVariableInParentClass
    ) {
        if (parentClass == finalChildClass) {
            // 当前子类就是最终子类，使用当前子类的泛型约束范围
            return new TypeBoundsContract<>(null,
                    (TypeVariable<? extends Class<R>>) typeVariableInParentClass, null);
        }
        // 查找该范型在该类中的声明下标
        TypeVariable<? extends Class<?>>[] childClassTypeParameters = parentClass.getTypeParameters();
        for (int i = 0; i < childClassTypeParameters.length; i++) {
            TypeVariable<? extends Class<?>> childClassTypeParameter = childClassTypeParameters[i];
            // 如果其范型名称相同，就是找到了指定的范型，此时 childClassTypeParameter 就是子类要求的范型约束范围
            if (childClassTypeParameter.getName().equals(typeVariableInParentClass.getName())) {
                return getParentParameterizedTypeDefinedImplInChild(parentClass, finalChildClass, i);
            }
        }
        throw new IllegalArgumentException("无法在中间子类找到指定参数名的范型参数." +
                "中间子类：" + parentClass +
                "中间子类中定义的范型：" + typeVariableInParentClass +
                "最终子类：" + finalChildClass);
    }

    /**
     * 从子类/接口向上查找其到父类/接口的继承链
     * 包含最终子类（列表末尾）不包含顶级父类 {@code parentClass}
     *
     * @param <Parent>        父类类型
     * @param <Child>         子类类型
     * @param parentClass     父类类实例
     * @param finalChildClass 最终子类类实例
     * @return 父类的直接子类 to 最终子类的节点链
     */
    @NonNull
    static <Parent, Child extends Parent> Pair<ParentParameterizedTypeNode, ParentParameterizedTypeNode> getParentTypeDefinedImplInChild(
            Class<Parent> parentClass,
            Class<Child> finalChildClass) {
        final ParentParameterizedTypeNode finalChildClassNode = new ParentParameterizedTypeNode(finalChildClass);
        ParentParameterizedTypeNode childClass = finalChildClassNode;
        // 从子类开始向上迭代查找超类，直到到达父类为止
        findParent:
        while (true) {
            // 获得这个子类的超类
            Class<?> superclass = childClass.getSuperclass();
            if (parentClass == superclass) {
                // 记录其是由继承获得的父类
                childClass.interfaceIndex = ParentParameterizedTypeNode.BY_SUPER_CLASS;
                // 如果子类的超类就是要找的父类，遍历结束
                break;
            } else if (superclass != null && parentClass.isAssignableFrom(superclass)) {
                // 记录其是由继承获得的父类
                childClass.interfaceIndex = ParentParameterizedTypeNode.BY_SUPER_CLASS;
                // 如果子类的超类仍然是这个接口的子类，继续遍历这个超类的超类
                childClass = childClass.takeParentNode(superclass);
            } else {
                // 如果它的超类不是要找的父类的子类，查找是否其接口是该父类的子类
                Class<?>[] interfaces = childClass.getInterfaces();
                for (int i = 0; i < interfaces.length; i++) {
                    Class<?> anInterface = interfaces[i];
                    if (parentClass == anInterface) {
                        // 记录其接口实现的接口下标
                        childClass.interfaceIndex = i;
                        // 如果子类的超接口就是要找的父类，遍历结束
                        break findParent;
                    } else if (parentClass.isAssignableFrom(anInterface)) {
                        // 记录其接口实现的接口下标
                        childClass.interfaceIndex = i;
                        // 如果子类的超类仍然是这个接口的子类，继续遍历这个超类的超类
                        childClass = childClass.takeParentNode(anInterface);
                    }
                }
            }
        }
        return new Pair<>(childClass, finalChildClassNode);
    }
}
