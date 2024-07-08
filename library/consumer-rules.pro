## 获取ViewBinding工厂方法
-keepnames class * implements androidx.viewbinding.ViewBinding {
    ## io.github.chenfei0928.util.ViewBindingKt.bindFuncCache
    public static * bind(android.view.View);
    ## io.github.chenfei0928.util.ViewBindingKt.inflateFuncCache
    public static * inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
}

## 使指定方法在编译时被去除，用于去除日志
-assumenosideeffects class * {
    @androidx.annotation.NoSideEffects *;
}

## 为了可以访问到FragmentViewLifecycleOwner的Fragment
## androidx.fragment.app.FragmentViewLifecycleAccessor
-keep class androidx.fragment.app.FragmentViewLifecycleOwner {
    private final androidx.fragment.app.Fragment mFragment;
}

## 获取Protobuf默认实例方法
## com.google.protobuf.ProtobufAccessHelperKt.getProtobufDefaultInstance
-keepnames class * extends com.google.protobuf.GeneratedMessageV3 {
    public static * getDefaultInstance();
}

-dontwarn com.alibaba.android.arouter.facade.Postcard
-dontwarn com.alibaba.android.arouter.facade.callback.NavigationCallback
