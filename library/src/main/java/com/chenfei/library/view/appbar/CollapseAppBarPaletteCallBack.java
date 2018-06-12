package com.chenfei.library.view.appbar;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.graphics.Palette;

/**
 * 使用 GlidePalette 类库，为头图添加页面主题着色
 * Created by MrFeng on 2017/3/7.
 */
public class CollapseAppBarPaletteCallBack extends BasePaletteCallBack {
    private final CollapsingToolbarLayout mToolbarLayout;
    private final ActionBar mActionBar;
    @NonNull
    private ColorFilter mExpandedTitleColorFilter = color -> color;

    public CollapseAppBarPaletteCallBack(CollapsingToolbarLayout toolbarLayout, ActionBar supportActionBar) {
        mToolbarLayout = toolbarLayout;
        mActionBar = supportActionBar;
    }

    @CallSuper
    protected void onPalette(@NonNull Palette palette, @NonNull Palette.Swatch swatch) {
        // 设置Title的文字颜色
        @ColorInt int expandedTitleColor =
                mExpandedTitleColorFilter.filter(swatch.getTitleTextColor());
        mToolbarLayout.setExpandedTitleColor(expandedTitleColor);
        mToolbarLayout.setCollapsedTitleTextColor(swatch.getTitleTextColor());
        // 设置收起Toolbar后的背景色
        mToolbarLayout.setContentScrimColor(swatch.getRgb());
        mToolbarLayout.setStatusBarScrimColor(swatch.getRgb());
        // 修改ActionBar返回颜色
        @SuppressLint("PrivateResource") Drawable drawable = ContextCompat
                .getDrawable(mActionBar.getThemedContext(), android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material)
                .mutate();
        DrawableCompat.setTint(drawable, swatch.getTitleTextColor());
        mActionBar.setHomeAsUpIndicator(drawable);
        // 改变颜色后重新绘制
        mToolbarLayout.invalidate();
    }

    public CollapseAppBarPaletteCallBack withTransparentExpandedTitle() {
        return withExpandedTitleColor(color -> color & 0x00ffffff);
    }

    public CollapseAppBarPaletteCallBack withExpandedTitleColor(@NonNull ColorFilter filter) {
        mExpandedTitleColorFilter = filter;
        return this;
    }

    public interface ColorFilter {
        @ColorInt
        int filter(@ColorInt int color);
    }
}
