package android.support.design.widget;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.math.MathUtils;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

/**
 * 根据AppBarLayout收起进度来调整ToolBar返回箭头的着色，类似{@link CollapsingToolbarLayout}Title效果
 * 部分逻辑参考 {@link CollapsingToolbarLayout.OffsetUpdateListener}
 * Created by MrFeng on 2017/7/19.
 */
public class CollapsingToolbarBackListener implements AppBarLayout.OnOffsetChangedListener {
    private final CollapsingToolbarLayout mCollapsingToolbarLayout;
    private final ActionBar mActionBar;
    private final Drawable mBack;
    private MenuItem[] menus;
    private float mExpandedFraction = 0;
    // 收起之后的颜色
    @ColorInt
    private int mCollapsedColor = Color.BLACK;
    // 展开之后的颜色
    @ColorInt
    private int mExpandedColor = Color.WHITE;

    @SuppressLint("PrivateResource")
    public CollapsingToolbarBackListener(CollapsingToolbarLayout collapsingToolbarLayout, ActionBar actionBar) {
        mCollapsingToolbarLayout = collapsingToolbarLayout;
        mActionBar = actionBar;
        mBack = ContextCompat.getDrawable(mActionBar.getThemedContext(), android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material)
                .mutate();
    }

    public void setMenu(MenuItem... items) {
        this.menus = items;
        int color = getColor();
        for (MenuItem item : items) {
            item.setIcon(DrawableCompat.wrap(item.getIcon().mutate()));
            DrawableCompat.setTint(item.getIcon(), color);
        }
    }

    public final void setCollapsedColor(@ColorInt int collapsedColor) {
        mCollapsedColor = collapsedColor;
        setColor(blendColors(mExpandedColor, mCollapsedColor, mExpandedFraction));
    }

    public final void setExpandedColor(@ColorInt int expandedColor) {
        mExpandedColor = expandedColor;
        setColor(blendColors(mExpandedColor, mCollapsedColor, mExpandedFraction));
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        final int insetTop = mCollapsingToolbarLayout.mLastInsets != null
                ? mCollapsingToolbarLayout.mLastInsets.getSystemWindowInsetTop() : 0;

        // Update the collapsing text's fraction
        final int expandRange = mCollapsingToolbarLayout.getHeight() - ViewCompat.getMinimumHeight(
                mCollapsingToolbarLayout) - insetTop;
        mExpandedFraction = MathUtils.clamp(Math.abs(verticalOffset) / (float) expandRange, 0f, 1f);
        setColor(getColor());
    }

    protected void setColor(@ColorInt int color) {
        DrawableCompat.setTint(mBack, color);
        mActionBar.setHomeAsUpIndicator(mBack);
        // 设置菜单图标着色
        if (menus != null) {
            for (MenuItem item : menus) {
                DrawableCompat.setTint(item.getIcon(), color);
            }
        }
    }

    @ColorInt
    protected int getColor() {
        return blendColors(mExpandedColor, mCollapsedColor, mExpandedFraction);
    }

    /**
     * {@link CollapsingTextHelper#blendColors(int, int, float)}
     */
    private static int blendColors(int color1, int color2, float ratio) {
        final float inverseRatio = 1f - ratio;
        float a = (Color.alpha(color1) * inverseRatio) + (Color.alpha(color2) * ratio);
        float r = (Color.red(color1) * inverseRatio) + (Color.red(color2) * ratio);
        float g = (Color.green(color1) * inverseRatio) + (Color.green(color2) * ratio);
        float b = (Color.blue(color1) * inverseRatio) + (Color.blue(color2) * ratio);
        return Color.argb((int) a, (int) r, (int) g, (int) b);
    }
}
