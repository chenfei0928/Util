package io.github.chenfei0928.content.sp.delegate

import androidx.annotation.WorkerThread
import androidx.collection.ArrayMap
import androidx.lifecycle.LifecycleOwner
import io.github.chenfei0928.concurrent.coroutines.coroutineScope
import io.github.chenfei0928.content.sp.AbsSpSaver
import io.github.chenfei0928.content.sp.registerOnSharedPreferenceChangeListener
import io.github.chenfei0928.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

private const val TAG = "KW_AbsSpSaverWatcher"

/**
 * 建立并缓存[AbsSpSaver]子类的字段映射
 */
private object AbsSpSaverKProperty1Cache {
    private val spSaverKProperty1Cache =
        mutableMapOf<Class<out AbsSpSaver>, Map<String, KProperty1<out AbsSpSaver, *>?>>()

    /**
     * 获取kotlin类的字段缓存表。
     * 如果缓存中已经建立了该类的缓存，直接从缓存中获取，否则建立该类字段的缓存
     */
    @Suppress("UNCHECKED_CAST")
    private suspend fun <SpSaver : AbsSpSaver> getKotlinClassFieldsCache(
        spSaver: SpSaver
    ): Map<String, KProperty1<SpSaver, *>?> {
        val clazz = spSaver.javaClass
        return if (clazz in spSaverKProperty1Cache) {
            spSaverKProperty1Cache[clazz] as Map<String, KProperty1<SpSaver, *>?>
        } else {
            // 如果缓存中没有，在子线程中生成缓存并返回
            withContext(Dispatchers.Default) {
                synchronized(clazz) {
                    spSaverKProperty1Cache.getOrPut(clazz) {
                        // 解析字段并生成缓存
                        obtainPropertyMapCache(spSaver)
                    } as Map<String, KProperty1<SpSaver, *>?>
                }
            }
        }
    }

    /**
     * 生成属性字段的缓存
     *
     * @param spSaver sp委托保存实例，用于获取字段是否是委托字段，以及的委托属性在sp中保存的key值
     */
    @WorkerThread
    private fun <SpSaver : AbsSpSaver> obtainPropertyMapCache(
        spSaver: SpSaver
    ): Map<String, KProperty1<SpSaver, *>?> {
        // 创建map缓存并预解析所有字段
        val cacheOutput: MutableMap<String, KProperty1<SpSaver, *>?> =
            ArrayMap<String, KProperty1<SpSaver, *>?>()
        // 通过反射查找字段，此过程会比较耗时（约几百个ms）
        spSaver.javaClass.kotlin.memberProperties.forEach { property ->
            // 获取该字段的委托
            property.isAccessible = true
            val delegate = property.getDelegate(spSaver)
            // 如果该字段是委托字段，将其key与字段的映射关系保存下来
            if (delegate is AbsSpSaver.AbsSpDelegate<*>) {
                cacheOutput[delegate.obtainDefaultKey(property)] = property
            }
        }
        return cacheOutput
    }

    /**
     * 预先准备保存实例类的字段缓存
     */
    fun <SpSaver : AbsSpSaver> prepare(owner: LifecycleOwner, spSaver: SpSaver) {
        owner.coroutineScope.launch {
            getKotlinClassFieldsCache(spSaver)
        }
    }

    /**
     * 根据sp保存实例和其在sp文件中所保存的字段名获取其属性字段
     */
    suspend fun <SpSaver : AbsSpSaver> findSpKProperty1BySpKey(
        spSaver: SpSaver, key: String
    ): KProperty1<SpSaver, *>? = getKotlinClassFieldsCache(spSaver)[key]
}

/**
 * 监听sp的变化并通知字段变化
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-08-05 16:12
 */
fun <SpSaver : AbsSpSaver> SpSaver.registerOnSharedPreferenceChangeListener(
    owner: LifecycleOwner, callback: (key: KProperty1<SpSaver, *>) -> Unit
) {
    AbsSpSaverKProperty1Cache.prepare(owner, this)
    AbsSpSaver.getSp(this)
        .registerOnSharedPreferenceChangeListener(owner) { key ->
            owner.coroutineScope.launch {
                // 根据key获取其对应的AbsSpSaver字段
                val property = AbsSpSaverKProperty1Cache.findSpKProperty1BySpKey(
                    this@registerOnSharedPreferenceChangeListener, key
                )
                // 找得到属性，回调通知该字段被更改
                if (property == null) {
                    val simpleName =
                        this@registerOnSharedPreferenceChangeListener.javaClass.simpleName
                    Log.d(
                        TAG,
                        "registerOnSharedPreferenceChangeListener: cannot found property of the key($key) in class $simpleName"
                    )
                } else {
                    callback(property)
                }
            }
        }
}
