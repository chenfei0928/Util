package io.github.chenfei0928.util.reflect;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 用来存储继承/实现节点，需要记录该节点类/接口信息，其是继承或实现来的父类/接口，
 * 如果是继承/实现接口，需要记录其接口是实现接口下标
 *
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2021-03-17 15:35
 */
class ParentParameterizedTypeNode {
    static final int BY_SUPER_CLASS = -1;

    @Nullable
    ParentParameterizedTypeNode parentNode;
    @Nullable
    ParentParameterizedTypeNode childNode;

    @NonNull
    final Class<?> nodeClass;
    /**
     * 如果该类的父节点是由继承获得，则为{@link ParentParameterizedTypeNode#BY_SUPER_CLASS}，
     * 如果是由实现接口获得的父节点，则父节点接口在当前类中实现的下标。
     */
    int interfaceIndex = BY_SUPER_CLASS;

    ParentParameterizedTypeNode(@NotNull Class<?> aClass) {
        nodeClass = aClass;
    }

    Class<?> getSuperclass() {
        return nodeClass.getSuperclass();
    }

    Class<?>[] getInterfaces() {
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

    ParentParameterizedTypeNode takeParentNode(Class<?> superNodeType) {
        ParentParameterizedTypeNode parentNode = new ParentParameterizedTypeNode(superNodeType);
        this.parentNode = parentNode;
        parentNode.childNode = this;
        return parentNode;
    }

    private String toChildString() {
        String childString = childNode != null ? childNode.toChildString() : null;
        return "ParentParameterizedTypeNode{" +
                "nodeClass=" + nodeClass +
                ", childNode=" + childString +
                ", interfaceIndex=" + interfaceIndex +
                '}';
    }

    private String toParentString() {
        String parentString = parentNode != null ? parentNode.toParentString() : null;
        return "ParentParameterizedTypeNode{" +
                "nodeClass=" + nodeClass +
                ", parentNode=" + parentString +
                ", interfaceIndex=" + interfaceIndex +
                '}';
    }

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
}
