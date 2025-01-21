package io.github.chenfei0928.annotation

import com.google.gson.reflect.TypeToken

/**
 * 像 Gson 的 [TypeToken] 一样，保留类，但允许混淆、压缩和优化
 *
 * - 压缩（Shrinking）：从入口开始建立引用关系网，去除网外未使用的代码。
 * 允许其被压缩,就是说指定的内容有可能被移除,但是如果没有被移除的话它也不会在后续过程中被优化或者混淆.
 * - 优化（Optimization）：对入口点以外所有的方法进行分析，将其中一部分方法变为 final的，static的，private的或内联的，从而提高执行效率。
 * 允许其被优化,但是不会被移除或者混淆(使用情况较少)
 * - 混淆（Obfuscation）：将入口点以外的类、方法、成员重构为简短的名字，可以减小生成文件体积，同时混淆代码。
 * 允许其被混淆,但是不会被移除或者优化(使用情况较少)
 *
 * [Google的 缩减、混淆处理和优化应用](https://developer.android.com/build/shrink-code?hl=zh-cn#optimization)、
 * [R8FullMode兼容性FAQ](https://r8.googlesource.com/r8/+/refs/heads/main/compatibility-faq.md#r8-full-mode)、
 * [Jake Wharton 撰写的关于 R8 优化的博文](https://jakewharton.com/blog/)
 *
 * @author chenf()
 * @date 2025-01-07 15:12
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class KeepAllowOptimizationShrinkingObfuscation
