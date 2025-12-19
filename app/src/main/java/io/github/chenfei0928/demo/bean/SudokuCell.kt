package io.github.chenfei0928.demo.bean

import androidx.annotation.IntRange

/**
 * @author chenf()
 * @date 2025-12-17 11:22
 */
typealias SudokuCell = SudokuCellLong

@JvmInline
value class SudokuCellInt(
    val packedValue: Int
) {
    val value: Int // 4bit
        get() = packedValue and 0xf
    val allowedValue: Int // 9bit
        get() = (packedValue shr 4) and 0x1ff
    val fromQuestion: Boolean // 1bit
        get() = packedValue and QUESTION_MARK != 0
    val indexOfChessboard: Int // 7bit
        get() = (packedValue shr 14) and 0x7f

    val indexOfBox: Int
        get() = (indexOfRow % 3) + indexOfColumn % 3 * 3
    val boxIndex: Int
        get() = indexOfRow / 3 * 3 + indexOfColumn / 3
    val indexOfRow: Int
        get() = indexOfChessboard / 9
    val indexOfColumn: Int
        get() = indexOfChessboard % 9

    fun copy(value: Int, allowedValue: Int = this.allowedValue): SudokuCellInt {
        return SudokuCellInt((packedValue and 0xf.inv()) or value or allowedValue)
    }

    override fun toString(): String {
        return "SudokuCellInt(value=$value, allowedValue=0b${Integer.toBinaryString(allowedValue)}, fromQuestion=$fromQuestion, indexOfChessboard=$indexOfChessboard, indexOfBox=$indexOfBox, boxIndex=$boxIndex, indexOfRow=$indexOfRow, indexOfColumn=$indexOfColumn"
    }

    companion object {
        const val ALLOWED_0: Int = 1
        const val ALLOWED_1: Int = 1 shl 1
        const val ALLOWED_2: Int = 1 shl 2
        const val ALLOWED_3: Int = 1 shl 3
        const val ALLOWED_4: Int = 1 shl 4
        const val ALLOWED_5: Int = 1 shl 5
        const val ALLOWED_6: Int = 1 shl 6
        const val ALLOWED_7: Int = 1 shl 7
        const val ALLOWED_8: Int = 1 shl 8
        const val ALLOWED_9: Int = 1 shl 9

        private const val QUESTION_MARK = 0x200

        fun createByQuestion(
            indexOfChessboard: Int,
            @IntRange(from = 1, to = 9) value: Int
        ): SudokuCellInt = SudokuCellInt(
            value or QUESTION_MARK or indexOfChessboard shl 14
        )
    }
}

@JvmInline
value class SudokuCellLong(
    val packedValue: Long
) {
    val value: Int // 4bit
        get() = (packedValue and 0xf).toInt()
    val allowedValue: Int // 9bit
        get() = ((packedValue shr 4) and 0x1ff).toInt()
    val fromQuestion: Boolean // 1bit
        get() = packedValue and QUESTION_MARK != 0L
    val indexOfChessboard: Int // 7bit
        get() = ((packedValue shr 14) and 0x7f).toInt()
    val indexOfBox: Int // 4bit
        get() = ((packedValue shr 21) and 0xf).toInt()
    val boxIndex: Int // 4bit
        get() = ((packedValue shr 25) and 0xf).toInt()
    val indexOfRow: Int // 4bit
        get() = ((packedValue shr 29) and 0xf).toInt()
    val indexOfColumn: Int // 4bit
        get() = ((packedValue shr 33) and 0xf).toInt()

    fun copy(value: Int, allowedValue: Int = this.allowedValue): SudokuCellLong {
        return SudokuCellLong((packedValue and 0xfL.inv()) or value.toLong() or allowedValue.toLong())
    }

    override fun toString(): String {
        return "SudokuCellLong(value=$value, allowedValue=0b${Integer.toBinaryString(allowedValue)}, fromQuestion=$fromQuestion, indexOfChessboard=$indexOfChessboard, indexOfBox=$indexOfBox, boxIndex=$boxIndex, indexOfRow=$indexOfRow, indexOfColumn=$indexOfColumn"
    }

    companion object {
        const val ALLOWED_0: Int = 1
        const val ALLOWED_1: Int = 1 shl 1
        const val ALLOWED_2: Int = 1 shl 2
        const val ALLOWED_3: Int = 1 shl 3
        const val ALLOWED_4: Int = 1 shl 4
        const val ALLOWED_5: Int = 1 shl 5
        const val ALLOWED_6: Int = 1 shl 6
        const val ALLOWED_7: Int = 1 shl 7
        const val ALLOWED_8: Int = 1 shl 8
        const val ALLOWED_9: Int = 1 shl 9

        private const val QUESTION_MARK = 0x200L

        fun createByQuestion(
            indexOfChessboard: Int,
            @IntRange(from = 1, to = 9) value: Int
        ): SudokuCellLong {
            val sudokuCellInt = SudokuCellInt.createByQuestion(indexOfChessboard, value)
            return SudokuCellLong(
                sudokuCellInt.packedValue.toLong() or
                        sudokuCellInt.indexOfBox.toLong() shl 21 or
                        sudokuCellInt.boxIndex.toLong() shl 25 or
                        sudokuCellInt.indexOfRow.toLong() shl 29 or
                        sudokuCellInt.indexOfColumn.toLong() shl 33
            )
        }
    }
}
