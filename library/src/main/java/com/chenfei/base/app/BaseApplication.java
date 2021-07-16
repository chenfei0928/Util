package com.chenfei.base.app;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ViewTarget;
import com.chenfei.library.R;
import com.chenfei.util.ExecutorUtil;
import com.chenfei.util.PowerSaveUtil;
import com.chenfei.util.ToastUtil;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

/**
 * 应用程序实例类，用于保存和管理登录用户和获取全局Context对象
 *
 * @author Admin
 * @date 2015/8/20
 */
public abstract class BaseApplication extends MultiDexApplication {
    private static BaseApplication instance;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // 初始化工作线程
        ExecutorUtil.INSTANCE.postToBg(() -> {
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        ToastUtil.init(this);
        // 设置Glide图片加载框架的viewTag
        ViewTarget.setTagId(R.id.glide_id);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).trimMemory(level);
        PowerSaveUtil.trimMemory(this, level);
    }

    public static BaseApplication getInstance() {
        return instance;
    }
}
