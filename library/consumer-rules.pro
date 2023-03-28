
-keepnames class * implements androidx.viewbinding.ViewBinding {
    public static * bind(android.view.View);
    public static * inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
}

## 使指定方法在编译时被去除，用于去除日志
-assumenosideeffects class * {
    @androidx.annotation.NoSideEffects *;
}

## 为了可以访问到FragmentViewLifecycleOwner的Fragment
-keep class androidx.fragment.app.FragmentViewLifecycleOwner {
    private final androidx.fragment.app.Fragment mFragment;
}

-keepnames class * extends com.google.protobuf.GeneratedMessageV3 {
    public static * getDefaultInstance();
}
