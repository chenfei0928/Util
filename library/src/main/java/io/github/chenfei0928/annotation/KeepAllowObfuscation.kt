package io.github.chenfei0928.annotation

import com.google.gson.reflect.TypeToken

/**
 * 像 Gson 的 [TypeToken] 一样，保留类，但允许混淆
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
annotation class KeepAllowObfuscation
