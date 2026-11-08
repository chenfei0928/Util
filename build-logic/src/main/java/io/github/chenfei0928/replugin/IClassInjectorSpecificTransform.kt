package io.github.chenfei0928.replugin

import com.qihoo360.replugin.gradle.plugin.injector.identifier.GetIdentifierExprEditor
import com.qihoo360.replugin.gradle.plugin.injector.localbroadcast.LocalBroadcastExprEditor
import com.qihoo360.replugin.gradle.plugin.injector.provider.ProviderExprEditor
import com.qihoo360.replugin.gradle.plugin.injector.provider.ProviderExprEditor2
import com.qihoo360.replugin.gradle.plugin.manifest.ManifestAPI
import com.tencent.shadow.core.transform_kit.SpecificTransform
import com.tencent.shadow.core.transform_kit.TransformStep
import io.github.chenfei0928.Env
import javassist.CtClass
import javassist.expr.ExprEditor
import javassist.expr.MethodCall
import org.gradle.api.Project

/**
 * I class injector specific transform
 * [com.qihoo360.replugin.gradle.plugin.injector.Injectors]
 *
 * @author chenf()
 * @date 2025-04-29 10:46
 */
sealed class IClassInjectorSpecificTransform : SpecificTransform(), TransformStep {

    override fun setup(allInputClass: Set<CtClass>) {
        newStep(this)
    }

    sealed class BaseEditorTransform : IClassInjectorSpecificTransform() {
        protected abstract val editor: ExprEditor

        override fun transform(ctClass: CtClass) {
            /* 检查方法列表 */
            ctClass.declaredMethods.forEach {
                it.instrument(editor)
            }
            ctClass.methods.forEach {
                it.instrument(editor)
            }
        }
    }

    /**
     * Loader activity injector transform
     * [com.qihoo360.replugin.gradle.plugin.injector.loaderactivity.LoaderActivityInjector]
     *
     * @property project
     * @property variantDir
     * @constructor Create empty Loader activity injector transform
     */
    class LoaderActivityInjectorTransform(
        private val project: Project,
        private val variantDir: String,
    ) : IClassInjectorSpecificTransform() {

        /* LoaderActivity 替换规则 */
        private val loaderActivityRules = mapOf(
            "android.app.Activity" to "com.qihoo360.replugin.loader.a.PluginActivity",
            "android.app.TabActivity" to "com.qihoo360.replugin.loader.a.PluginTabActivity",
            "android.app.ListActivity" to "com.qihoo360.replugin.loader.a.PluginListActivity",
            "android.app.ActivityGroup" to "com.qihoo360.replugin.loader.a.PluginActivityGroup",
            "android.support.v4.app.FragmentActivity" to "com.qihoo360.replugin.loader.a.PluginFragmentActivity",
            "android.support.v7.app.AppCompatActivity" to "com.qihoo360.replugin.loader.a.PluginAppCompatActivity",
            "androidx.fragment.app.FragmentActivity" to "com.qihoo360.replugin.loader.a.PluginFragmentXActivity",
            "androidx.appcompat.app.AppCompatActivity" to "com.qihoo360.replugin.loader.a.PluginAppCompatXActivity",
            "android.preference.PreferenceActivity" to "com.qihoo360.replugin.loader.a.PluginPreferenceActivity",
            "android.app.ExpandableListActivity" to "com.qihoo360.replugin.loader.a.PluginExpandableListActivity",
        )

        override fun filter(allInputClass: Set<CtClass>): Set<CtClass> {
            val classNames = ManifestAPI().getActivities(project, variantDir) as List<String>
            return allInputClass.filterTo(java.util.HashSet()) {
                it.name in classNames
            }
        }

        override fun transform(ctClass: CtClass) {
            var ctCls = ctClass
            // ctCls 之前的父类
            var superCls = ctCls.superclass.let { originSuperCls ->
                /* 从当前 Activity 往上回溯，直到找到需要替换的 Activity */
                var superCls = originSuperCls
                while (superCls != null && superCls.name !in loaderActivityRules.keys) {
                    // println ">>> 向上查找 $superCls.name"
                    ctCls = superCls
                    superCls = ctCls.superclass
                }
                superCls
            }

            // 如果 ctCls 已经是 LoaderActivity，则不修改
            if (ctCls.name in loaderActivityRules.values) {
                // println "    跳过 ${ctCls.getName()}"
                return
            }

            /* 找到需要替换的 Activity, 修改 Activity 的父类为 LoaderActivity */
            if (superCls != null) {
                val targetSuperClsName = loaderActivityRules[superCls.name]
                // println "    ${ctCls.getName()} 的父类 $superCls.name 需要替换为 ${targetSuperClsName}"
                Env.logger.info("LoaderActivityInjectorTransform ${ctCls.name} 的父类 ${superCls.name} 需要替换为 $targetSuperClsName\n")
                val targetSuperCls = mClassPool.get(targetSuperClsName)

                if (ctCls.isFrozen) {
                    ctCls.defrost()
                }
                ctCls.superclass = targetSuperCls

                // 修改声明的父类后，还需要方法中所有的 super 调用。
                ctCls.declaredMethods.forEach { outerMethod ->
                    outerMethod.instrument(object : ExprEditor() {
                        override fun edit(call: MethodCall) {
                            if (call.isSuper) {
                                if (call.method.returnType.name == "void") {
                                    call.replace("{super." + call.getMethodName() + "($$);}")
                                } else {
                                    call.replace("{\$_ = super." + call.getMethodName() + "($$);}")
                                }
                            }
                        }
                    })
                }
            }
        }
    }

    /**
     * Local broadcast injector transform
     * [com.qihoo360.replugin.gradle.plugin.injector.localbroadcast.LocalBroadcastInjector]
     *
     * @constructor Create empty Local broadcast injector transform
     */
    object LocalBroadcastInjectorTransform : BaseEditorTransform() {
        override val editor = LocalBroadcastExprEditor()

        override fun filter(allInputClass: Set<CtClass>): Set<CtClass> {
            return allInputClass.filterTo(HashSet()) {
                it.name != "android.support.v4.content.LocalBroadcastManager" &&
                        it.name != "androidx.localbroadcastmanager.content.LocalBroadcastManager"
            }
        }
    }

    /**
     * Provider injector transform
     * [com.qihoo360.replugin.gradle.plugin.injector.provider.ProviderInjector]
     *
     * @constructor Create empty Provider injector transform
     */
    object ProviderInjectorTransform : BaseEditorTransform() {
        override val editor = ProviderExprEditor()
        override fun filter(allInputClass: Set<CtClass>): Set<CtClass> {
            return allInputClass.filterTo(HashSet()) {
                it.simpleName != "PluginProviderClient"
            }
        }
    }

    /**
     * Provider injector2transform
     * [com.qihoo360.replugin.gradle.plugin.injector.provider.ProviderInjector2]
     *
     * @constructor Create empty Provider injector2transform
     */
    object ProviderInjector2Transform : BaseEditorTransform() {
        override val editor = ProviderExprEditor2()
        override fun filter(allInputClass: Set<CtClass>): Set<CtClass> {
            return allInputClass.filterTo(HashSet()) {
                it.simpleName != "PluginProviderClient2"
            }
        }
    }

    /**
     * Get identifier injector transform
     * [com.qihoo360.replugin.gradle.plugin.injector.identifier.GetIdentifierInjector]
     *
     * @constructor Create empty Get identifier injector transform
     */
    object GetIdentifierInjectorTransform : BaseEditorTransform() {
        override val editor = GetIdentifierExprEditor()
        override fun filter(allInputClass: Set<CtClass>): Set<CtClass> {
            return allInputClass
        }
    }
}
