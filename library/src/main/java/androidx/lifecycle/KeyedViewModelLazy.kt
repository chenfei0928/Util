package androidx.lifecycle

import kotlin.reflect.KClass

/**
 * [ViewModelLazy]
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-04-22 18:33
 */
class KeyedViewModelLazy<VM : ViewModel>(
    private val key: String,
    private val viewModelClass: KClass<VM>,
    private val storeProducer: () -> ViewModelStore,
    private val factoryProducer: () -> ViewModelProvider.Factory
) : Lazy<VM> {
    private var cached: VM? = null

    override val value: VM
        get() {
            val viewModel = cached
            return if (viewModel == null) {
                val factory = factoryProducer()
                val store = storeProducer()
                ViewModelProvider(store, factory)
                    .get(key, viewModelClass.java)
                    .also {
                        cached = it
                    }
            } else {
                viewModel
            }
        }

    override fun isInitialized() = cached != null
}
