package io.github.chenfei0928.reflect.parameterized

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf

/**
 * 用来存储继承/实现节点，需要记录该节点类/接口信息，其是继承或实现来的父类/接口，
 * 如果是继承/实现接口，需要记录其接口是实现接口下标
 *
 * @author chenf()
 * @date 2024-07-08 11:40
 */
class ParentParameterizedKtTypeNode(
    val nodeKClass: KClass<*>,
) {
    var parentNode: ParentParameterizedKtTypeNode? = null
    var childNode: ParentParameterizedKtTypeNode? = null

    var supertype: KType? = null

    fun takeParentNode(
        supertype: KType, superNodeType: KClass<*>
    ) = ParentParameterizedKtTypeNode(superNodeType).also { parentNode ->
        this.supertype = supertype
        this.parentNode = parentNode
        parentNode.childNode = this
    }

    private fun toChildString(): String {
        return "ParentParameterizedKtTypeNode{" +
                "nodeClass=" + nodeKClass +
                ", childNode=" + childNode?.toChildString() +
                '}'
    }

    private fun toParentString(): String {
        return "ParentParameterizedKtTypeNode{" +
                "nodeClass=" + nodeKClass +
                ", parentNode=" + parentNode?.toParentString() +
                '}'
    }

    override fun toString(): String {
        return "ParentParameterizedKtTypeNode{" +
                "parentNode=" + parentNode?.toParentString() +
                ", nodeClass=" + nodeKClass +
                ", supertype=" + supertype +
                ", childNode=" + childNode?.toChildString() +
                '}'
    }

    companion object {

        /**
         * 从子类/接口向上查找其到父类/接口的继承链
         * 包含最终子类（列表末尾）不包含顶级父类[parentKClass]
         *
         * @param <P>             父类类型
         * @param <C>             子类类型
         * @param parentKClass    父类类实例
         * @param finalChildClass 最终子类类实例
         * @return 父类的直接子类 to 最终子类的节点链
         */
        fun <P : Any, C : P> getParentTypeDefinedImplInChild(
            parentKClass: KClass<P>,
            finalChildClass: KClass<C>,
        ): Pair<ParentParameterizedKtTypeNode, ParentParameterizedKtTypeNode> {
            val finalChildNode = ParentParameterizedKtTypeNode(finalChildClass)
            var childClass: ParentParameterizedKtTypeNode = finalChildNode
            // 从子类开始向上迭代查找超类，直到到达父类为止
            while (true) {
                // 获得这个子类的超类
                val supertypes = childClass.nodeKClass.supertypes
                for (supertype in supertypes) {
                    val superClass = supertype.classifier
                    when {
                        superClass !is KClass<*> -> {
                            continue
                        }
                        parentKClass == superClass -> {
                            childClass.supertype = supertype
                            return childClass to finalChildNode
                        }
                        superClass.isSubclassOf(parentKClass) -> {
                            childClass.supertype = supertype
                            childClass = childClass.takeParentNode(supertype, superClass)
                            break
                        }
                    }
                }
            }
        }
    }
}
