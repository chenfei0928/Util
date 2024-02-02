/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-04-15 14:42
 */
package io.github.chenfei0928.graphics

import android.graphics.RectF
import androidx.annotation.FloatRange
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 根据指定圆心和半径，生成圆的范围，以供绘制
 *
 * @param centerX 圆心的x轴坐标
 * @param centerY 圆心的y轴坐标
 * @param radius  半径
 */
fun RectF.setOval(centerX: Float, centerY: Float, radius: Float) {
    set(
        centerX - radius, centerY - radius, centerX + radius, centerY + radius
    )
}

/**
 * 根据两个圆心坐标和半径，计算并生成椭圆的范围，以供绘制
 * 受限于绘制是根据矩形边界范围，不能绘制倾斜方向的椭圆，两个圆心的坐标必须位于同一x或y轴上
 */
fun RectF.setOval(
    center1X: Float,
    center1Y: Float,
    center2X: Float,
    center2Y: Float,
    @FloatRange(from = 0.0) radius: Float
) {
    when {
        center1Y == center2Y -> {
            val centerDiff = abs(center1X - center2X)
            if (centerDiff > radius) {
                throw IllegalArgumentException("半径不能小于圆心间距")
            }
            // 在同一横坐标上的圆心，横向的椭圆
            val r1 = (radius - centerDiff) / 2
            val left = min(center1X, center2X) - r1
            val right = max(center1X, center2X) + r1
            // 根据勾股定理算出扁轴上距离圆心连线距离
            val r2 = sqrt((radius / 2).pow(2) - (centerDiff / 2).pow(2))
            val top = center1Y + r2
            val bottom = center1Y - r2
            // 设置范围
            set(left, top, right, bottom)
        }
        center1X == center2X -> {
            val centerDiff = abs(center1Y - center2Y)
            if (centerDiff > radius) {
                throw IllegalArgumentException("半径不能小于圆心间距")
            }
            // 在同一纵坐标上的圆心，纵向的椭圆
            val r1 = (radius - centerDiff) / 2
            val top = max(center1Y, center2Y) + r1
            val bottom = min(center1Y, center2Y) - r1
            // 根据勾股定理算出扁轴上距离圆心连线距离
            val r2 = sqrt((radius / 2).pow(2) - (centerDiff / 2).pow(2))
            val right = center1X + r2
            val left = center1X - r2
            // 设置范围
            set(left, top, right, bottom)
        }
        else -> {
            throw IllegalArgumentException("两个圆心的坐标必须位于同一x或y轴上")
        }
    }
}
