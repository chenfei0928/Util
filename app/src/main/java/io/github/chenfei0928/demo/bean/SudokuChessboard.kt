package io.github.chenfei0928.demo.bean

import io.github.chenfei0928.util.forEachSetBit
import java.util.BitSet

/**
 * @author chenf()
 * @date 2025-12-17 17:40
 */
@JvmInline
value class SudokuChessboard(
    val chessboard: LongArray
) {

    override fun toString(): String {
        return chessboard.joinToString("") {
            SudokuCell(it).value.toString()
        }
    }

    fun updateCellValue(cell: SudokuCell, newValue: Int) {
        chessboard[cell.indexOfChessboard] = cell.copy(value = newValue).packedValue
        val needRecheckCells = BitSet(81)
        chessboard.forEach { packedValue ->
            val currCell = SudokuCell(packedValue)
            if (currCell.indexOfRow == cell.indexOfRow ||
                currCell.indexOfColumn == cell.indexOfColumn ||
                currCell.boxIndex == cell.boxIndex
            ) {
                needRecheckCells.set(currCell.indexOfChessboard, true)
            }
        }
        needRecheckCells.forEachSetBit { index ->

        }
    }

    companion object {
        fun parse(chessboard: String): SudokuChessboard {
            return SudokuChessboard(LongArray(81) {
                SudokuCell.createByQuestion(it, chessboard[it] - '0').packedValue
            })
        }
    }
}
