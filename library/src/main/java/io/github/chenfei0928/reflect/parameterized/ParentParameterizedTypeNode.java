package io.github.chenfei0928.reflect.parameterized;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

/**
 * 用来存储继承/实现节点，需要记录该节点类/接口信息，其是继承或实现来的父类/接口，
 * 如果是继承/实现接口，需要记录其接口是实现接口下标
 *
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2021-03-17 15:35
 */
class ParentParameterizedTypeNode {
    static final int BY_SUPER_CLASS = -1;
    @NonNull
    final Class<?> nodeClass;
    @Nullable
    private ParentParameterizedTypeNode parentNode;
    @Nullable
    ParentParameterizedTypeNode childNode;
    /**
     * 如果该类的父节点是由继承获得，则为{@link ParentParameterizedTypeNode#BY_SUPER_CLASS}，
     * 如果是由实现接口获得的父节点，则父节点接口在当前类中实现的下标。
     */
    private int interfaceIndex = BY_SUPER_CLASS;

    ParentParameterizedTypeNode(@NotNull Class<?> aClass) {
        nodeClass = aClass;
    }

    private Class<?> getSuperclass() {
        return nodeClass.getSuperclass();
    }

    private Class<?>[] getInterfaces() {
        return nodeClass.getInterfaces();
    }

    Type getGenericSuperclass() {
        if (interfaceIndex == BY_SUPER_CLASS) {
            return nodeClass.getGenericSuperclass();
        } else {
            return nodeClass.getGenericInterfaces()[interfaceIndex];
        }
    }

    TypeVariable<? extends Class<?>>[] getTypeParameters() {
        return nodeClass.getTypeParameters();
    }

    private ParentParameterizedTypeNode takeParentNode(Class<?> superNodeType) {
        ParentParameterizedTypeNode parentTypeNode = new ParentParameterizedTypeNode(superNodeType);
        this.parentNode = parentTypeNode;
        parentTypeNode.childNode = this;
        return parentTypeNode;
    }

    private String toChildString() {
        String childString = childNode != null ? childNode.toChildString() : null;
        return '{' +
                "nodeClass=" + nodeClass +
                ", childNode=" + childString +
                ", interfaceIndex=" + interfaceIndex +
                '}';
    }

    private String toParentString() {
        String parentString = parentNode != null ? parentNode.toParentString() : null;
        return '{' +
                "nodeClass=" + nodeClass +
                ", parentNode=" + parentString +
                ", interfaceIndex=" + interfaceIndex +
                '}';
    }

    @NonNull
    @Override
    public String toString() {
        String parentString = parentNode != null ? parentNode.toParentString() : null;
        String childString = childNode != null ? childNode.toChildString() : null;
        return "ParentParameterizedTypeNode{" +
                "parentNode=" + parentString +
                ", nodeClass=" + nodeClass +
                ", childNode=" + childString +
                ", interfaceIndex=" + interfaceIndex +
                '}';
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
            Class<C> finalChildClass
    ) {
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
