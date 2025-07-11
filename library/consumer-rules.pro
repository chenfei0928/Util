## 获取ViewBinding工厂方法
## 在 io/github/chenfei0928/viewbinding/ViewBinding.kt 文件中使用
-keepnames class * implements androidx.viewbinding.ViewBinding {
    ## io.github.chenfei0928.util.ViewBindingKt.bindFuncCache
    public static * bind(android.view.View);
    ## io.github.chenfei0928.util.ViewBindingKt.inflateFuncCache
    public static * inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
}

## 使指定方法在编译时被去除，用于去除日志
-assumenosideeffects class * {
    @io.github.chenfei0928.annotation.NoSideEffects *;
}

## release下移除debug日志，参考protobuf lite配置
#-assumevalues class io.github.chenfei0928.webkit.BaseLogWebViewClient$Companion { boolean getDebugLog() return false; }
-assumevalues class io.github.chenfei0928.webkit.BaseWebViewClient$Companion { boolean getIgnoreSslError() return false; }

## 保留类但允许混淆
-keep,allowobfuscation @io.github.chenfei0928.annotation.KeepAllowObfuscation class * {*;}
## 参考了retrofit的混淆配置，keep类，但允许对成员优化、压缩和混淆
-keep,allowoptimization,allowshrinking,allowobfuscation @io.github.chenfei0928.annotation.KeepAllowOptimizationShrinkingObfuscation class * {*;}

## 使WithChildInObfuscation应用到子类，参考gson与retrofit配置
-if @io.github.chenfei0928.annotation.KeepAllowObfuscation @androidx.annotation.Keep class **
-keep class * extends <1>

## 使WithChildInObfuscation应用到子类，参考gson与retrofit配置
-if @io.github.chenfei0928.annotation.WithChildInObfuscation @io.github.chenfei0928.annotation.KeepAllowObfuscation class **
-keep,allowobfuscation class * extends <1>

## 使WithChildInObfuscation应用到子类，参考gson与retrofit配置
-if @io.github.chenfei0928.annotation.WithChildInObfuscation @io.github.chenfei0928.annotation.KeepAllowOptimizationShrinkingObfuscation class **
-keep,allowoptimization,allowshrinking,allowobfuscation class * extends <1>

## Type类型懒获取，它需要保证父类继承顺序不被R8破坏，但允许混淆类名与方法名
-keep,allowobfuscation class * extends io.github.chenfei0928.reflect.LazyTypeToken

## 为了可以访问到FragmentViewLifecycleOwner的Fragment
## 在 androidx.fragment.app.FragmentViewLifecycleAccessor 中使用
## androidx.fragment.app.FragmentViewLifecycleAccessor
-keep class androidx.fragment.app.FragmentViewLifecycleOwner {
    private final androidx.fragment.app.Fragment mFragment;
}

## 获取Protobuf默认实例方法
## 在 com.google.protobuf.ProtobufAccessHelperKt.protobufDefaultInstanceCache 字段中使用
## com.google.protobuf.ProtobufAccessHelperKt.getProtobufDefaultInstance
## io.github.chenfei0928.util.DependencyChecker.PROTOBUF
-keepnames class * extends com.google.protobuf.MessageLite {
    public static * getDefaultInstance();
}

## 扩展ARouter API的使用，避免使用处不依赖ARouter导致的报错而 dontwarn
## 在 io/github/chenfei0928/app/arouter 包中使用
-dontwarn com.alibaba.android.arouter.facade.Postcard
-dontwarn com.alibaba.android.arouter.facade.callback.NavigationCallback

## 反射获取RecyclerView可见区域范围
## 在 io.github.chenfei0928.widget.recyclerview.RecyclerViewKt.findVisibleRange 方法中使用
-keepnames class * extends androidx.recyclerview.widget.RecyclerView$LayoutManager {
    public int findFirstVisibleItemPosition();
    public int findLastVisibleItemPosition();
}

## 直接获取 meterial 依赖是否引入
## io.github.chenfei0928.util.DependencyChecker.MATERIAL
-dontwarn com.google.android.material.bottomsheet.BottomSheetDialog

## 反射获取 androidx 的 concurrentFuturesKtx 依赖是否引入
## io.github.chenfei0928.util.DependencyChecker.ANDROID_X_LISTENABLE_FUTURE
-keepnames class androidx.concurrent.futures.ListenableFutureKt

## 反射获取 FlexboxLayoutManager 依赖是否引入
## io.github.chenfei0928.util.DependencyChecker.FLEXBOX
-keepnames class com.google.android.flexbox.FlexboxLayoutManager
