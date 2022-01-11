package androidx.gridlayout.widget

import android.view.Gravity
import androidx.transition.Slide

var GridLayout.defaultGap: Int
    get() = mDefaultGap
    set(value) {
        mDefaultGap = value
        invalidate()
    }

fun GridLayout.LayoutParams.column(block: GridLayoutLayoutParamsSpecBuilder.() -> Unit) {
    val builder = GridLayoutLayoutParamsSpecBuilder.createFrom(columnSpec, true)
    block(builder)
    columnSpec = builder.build()
}

fun GridLayout.LayoutParams.row(block: GridLayoutLayoutParamsSpecBuilder.() -> Unit) {
    val builder = GridLayoutLayoutParamsSpecBuilder.createFrom(rowSpec, false)
    block(builder)
    rowSpec = builder.build()
}

class GridLayoutLayoutParamsSpecBuilder(
    private val isHorizontal: Boolean
) {
    @Slide.GravityFlag
    var gravity: Int = Gravity.NO_GRAVITY
    var index: Int = UNDEFINED
    var span: Int = DEFAULT_SPAN_SIZE
    var weight: Float = GridLayout.Spec.DEFAULT_WEIGHT

    fun build(): GridLayout.Spec {
        return GridLayout.spec(index, span, GridLayout.getAlignment(gravity, isHorizontal), weight)
    }

    companion object {
        private const val UNDEFINED = GridLayout.UNDEFINED
        private val DEFAULT_SPAN = GridLayout.Interval(UNDEFINED, UNDEFINED + 1)
        private val DEFAULT_SPAN_SIZE = DEFAULT_SPAN.size()

        fun createFrom(
            spec: GridLayout.Spec, isHorizontal: Boolean
        ): GridLayoutLayoutParamsSpecBuilder {
            val builder = GridLayoutLayoutParamsSpecBuilder(isHorizontal)
            builder.gravity = when (spec.alignment) {
                GridLayout.START -> Gravity.START
                GridLayout.END -> Gravity.END
                GridLayout.LEFT -> Gravity.LEFT
                GridLayout.RIGHT -> Gravity.RIGHT
                GridLayout.TOP -> Gravity.TOP
                GridLayout.BOTTOM -> Gravity.BOTTOM
                GridLayout.CENTER -> Gravity.CENTER
                GridLayout.FILL -> Gravity.FILL
                else -> Gravity.NO_GRAVITY
            }
            builder.index = if (spec.startDefined) spec.span.min else UNDEFINED
            builder.span = spec.span.max - spec.span.min
            builder.weight = spec.weight
            return builder
        }
    }
}
