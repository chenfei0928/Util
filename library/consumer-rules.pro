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
    @androidx.annotation.NoSideEffects *;
}

## 为了可以访问到FragmentViewLifecycleOwner的Fragment
## 在 androidx.fragment.app.FragmentViewLifecycleAccessor 中使用
## androidx.fragment.app.FragmentViewLifecycleAccessor
-keep class androidx.fragment.app.FragmentViewLifecycleOwner {
    private final androidx.fragment.app.Fragment mFragment;
}

## 获取Protobuf默认实例方法
## 在 com.google.protobuf.ProtobufAccessHelperKt.protobufDefaultInstanceCache 字段中使用
## com.google.protobuf.ProtobufAccessHelperKt.getProtobufDefaultInstance
-keepnames class * extends com.google.protobuf.GeneratedMessageV3 {
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
