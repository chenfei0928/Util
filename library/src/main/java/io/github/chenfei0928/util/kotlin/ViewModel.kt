package io.github.chenfei0928.util.kotlin

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.KeyedViewModelLazy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider

private val factory = ViewModelProvider.NewInstanceFactory()
val factoryProducer = { factory }

inline fun <reified VM : ViewModel> ComponentActivity.viewModels(
        key: String? = null, noinline factoryProducerBlock: (() -> VM)? = null
): Lazy<VM> {
    val factoryPromise: () -> ViewModelProvider.Factory = factoryProducerBlock?.let {
        {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return factoryProducerBlock() as T
                }
            }
        }
    } ?: factoryProducer
    return if (key.isNullOrBlank()) {
        ViewModelLazy(VM::class, { this.viewModelStore }, factoryPromise)
    } else {
        KeyedViewModelLazy(key, VM::class, { this.viewModelStore }, factoryPromise)
    }
}

inline fun <reified VM : ViewModel> Fragment.viewModels(
        key: String? = null, noinline factoryProducerBlock: (() -> VM)? = null
): Lazy<VM> {
    val factoryPromise: () -> ViewModelProvider.Factory = factoryProducerBlock?.let {
        {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return factoryProducerBlock() as T
                }
            }
        }
    } ?: factoryProducer
    return if (key.isNullOrBlank()) {
        ViewModelLazy(VM::class, { requireActivity().viewModelStore }, factoryPromise)
    } else {
        KeyedViewModelLazy(key, VM::class, { requireActivity().viewModelStore }, factoryPromise)
    }
}

fun <VM : ViewModel> Fragment.viewModels(
    key: String, clazz: Class<VM>
): Lazy<VM> {
    return lazy {
        ViewModelProvider(
            requireActivity().viewModelStore, factoryProducer()
        ).get(key, clazz)
    }
}

inline fun <reified VM : ViewModel> Fragment.viewModelsOnSelf(): Lazy<VM> {
    return ViewModelLazy(VM::class, { this.viewModelStore }, factoryProducer)
}
