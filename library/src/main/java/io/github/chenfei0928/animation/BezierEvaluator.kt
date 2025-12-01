package io.github.chenfei0928.animation

import android.animation.TypeEvaluator
import android.graphics.PointF

/**
 * 估值器，计算贝塞尔曲线路径
 *
 * [博文](https://blog.csdn.net/u010302765/article/details/70843831)
 */
class BezierEvaluator(
    private val p1: PointF,
    private val p2: PointF,
) : TypeEvaluator<PointF> {
    private val pointF = PointF()

    override fun evaluate(fraction: Float, p0: PointF, p3: PointF): PointF {
        // 贝塞尔曲线公式  p0*(1-t)^3 + 3p1*t*(1-t)^2 + 3p2*t^2*(1-t) + p3^3
        pointF.x = p0.x * (1 - fraction) * (1 - fraction) * (1 - fraction) +
                3 * p1.x * fraction * (1 - fraction) * (1 - fraction) +
                3 * p2.x * fraction * fraction * (1 - fraction) +
                p3.x * fraction * fraction * fraction
        pointF.y = p0.y * (1 - fraction) * (1 - fraction) * (1 - fraction) +
                3 * p1.y * fraction * (1 - fraction) * (1 - fraction) +
                3 * p2.y * fraction * fraction * (1 - fraction) +
                p3.y * fraction * fraction * fraction
        return pointF
    }
}
