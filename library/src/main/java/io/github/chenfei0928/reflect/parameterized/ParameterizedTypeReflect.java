package io.github.chenfei0928.reflect.parameterized;

import java.lang.reflect.Type;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

/**
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2020-10-21 18:18
 */
public class ParameterizedTypeReflect {

    private ParameterizedTypeReflect() {
    }

    public static <P, C extends P, R> Class<R> getParentParameterizedTypeClassDefinedImplInChild(
            Class<P> parentClass,
            Class<C> finalChildClass,
            int positionInParentParameter
    ) {
        return new ParameterizedTypeReflect1<P, C, R>(
                parentClass, finalChildClass, positionInParentParameter
        ).getParentParameterizedTypeDefinedImplInChild();
    }

    public static <P, C extends P> Type getParentParameterizedTypeDefinedImplInChild(
            Class<P> parentClass,
            Class<C> finalChildClass,
            int positionInParentParameter
    ) {
        return new ParameterizedTypeReflect1<>(
                parentClass, finalChildClass, positionInParentParameter
        ).getType();
    }

    /**
     * 从子类/接口向上查找其到父类/接口的继承链
     * 包含最终子类（列表末尾）不包含顶级父类 {@code parentClass}
     *
     * @param <P>             父类类型
     * @param <C>             子类类型
     * @param parentClass     父类类实例
     * @param finalChildClass 最终子类类实例
     * @return 父类的直接子类 to 最终子类的节点链
     */
    @NonNull
    static <P, C extends P> Pair<ParentParameterizedTypeNode, ParentParameterizedTypeNode> getParentTypeDefinedImplInChild(
            Class<P> parentClass,
            Class<C> finalChildClass) {
        final ParentParameterizedTypeNode finalChildClassNode = new ParentParameterizedTypeNode(finalChildClass);
        ParentParameterizedTypeNode childClass = finalChildClassNode;
        // 从子类开始向上迭代查找超类，直到到达父类为止
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
                        return new Pair<>(childClass, finalChildClassNode);
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
