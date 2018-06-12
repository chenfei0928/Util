package com.chenfei.library.view.appbar;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;

import com.github.florent37.glidepalette.BitmapPalette;

/**
 * 使用 GlidePalette 类库，为头图添加页面主题着色
 * Created by MrFeng on 2017/3/7.
 */
public abstract class BasePaletteCallBack implements BitmapPalette.CallBack {

    @Override
    public final void onPaletteLoaded(@Nullable Palette palette) {
        if (palette == null)
            return;
        Palette.Swatch swatch;
        if (palette.getDominantSwatch() != null) {
            // 优先使用首要色调
            swatch = palette.getDominantSwatch();
        } else if (palette.getMutedSwatch() != null) {
            // 柔和色调
            swatch = palette.getMutedSwatch();
        } else if (palette.getVibrantSwatch() != null) {
            // 鲜艳色调
            swatch = palette.getVibrantSwatch();
        } else {
            return;
        }
        onPalette(palette, swatch);
    }

    protected abstract void onPalette(@NonNull Palette palette, @NonNull Palette.Swatch swatch);
}
