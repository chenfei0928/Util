package androidx.viewpager2.adapter

import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.ItemViewDelegate
import com.drakeet.multitype.MultiTypeAdapter
import com.drakeet.multitype.MutableTypes
import com.drakeet.multitype.Type
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2023-03-13 16:09
 */
class FragmentStateMultiTypes(
    initialCapacity: Int = 0,
    types: MutableList<Type<*>> = ArrayList(initialCapacity)
) : MutableTypes(initialCapacity, types), AttachedTypes {
    override lateinit var adapter: FragmentStateMultiTypeAdapter
    private var attachedRecyclerView: RecyclerView? = null

    override fun <T> register(type: Type<T>) {
        super.register(type)
        val recyclerView = attachedRecyclerView
            ?: return
        // FragmentBinder.onAttachedToRecyclerView 需要adapter
        delegateAdapterSetter.set(type.delegate, adapter)
        // 如果已经 attached 到RecyclerView，将其注册到binder/delegate中
        type.onTypeAttachedToRecyclerView(recyclerView)
    }

    @CallSuper
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.attachedRecyclerView = recyclerView
        forEach { it.onTypeAttachedToRecyclerView(recyclerView) }
    }

    @CallSuper
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.attachedRecyclerView = null
        forEach { it.onTypeDetachedFromRecyclerView(recyclerView) }
    }

    private fun Type<*>.onTypeAttachedToRecyclerView(recyclerView: RecyclerView) {
        val type = delegate
        if (type is FragmentBinder<*>) {
            type.onAttachedToRecyclerView(recyclerView)
        }
    }

    private fun Type<*>.onTypeDetachedFromRecyclerView(recyclerView: RecyclerView) {
        val type = delegate
        if (type is FragmentBinder<*>) {
            type.onDetachedFromRecyclerView(recyclerView)
        }
    }

    companion object {
        private val delegateAdapterSetter: KMutableProperty1<ItemViewDelegate<*, *>, MultiTypeAdapter?> by lazy {
            val properties = ItemViewDelegate::class.declaredMemberProperties
            val property = properties.find { it.name == "_adapter" }?.apply {
                isAccessible = true
            } as? KMutableProperty1<ItemViewDelegate<*, *>, MultiTypeAdapter?>
            if (property != null)
                return@lazy property
            properties.filter { it.returnType.classifier == MultiTypeAdapter::class }
                .filterIsInstance<KMutableProperty1<ItemViewDelegate<*, *>, MultiTypeAdapter?>>()
                .first()
                .apply { isAccessible = true }
        }
    }
}
