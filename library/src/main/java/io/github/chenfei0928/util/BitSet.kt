package io.github.chenfei0928.util

import java.util.BitSet

/**
 * @author chenf()
 * @date 2025-11-27 10:58
 */
inline fun BitSet.forEachSetBit(action: (index: Int) -> Unit) {
    var i: Int = nextSetBit(0)
    while (i >= 0) {
        // operate on index i here
        if (i == Int.MAX_VALUE) {
            break // or (i+1) would overflow
        }
        i = nextSetBit(i + 1)
        action(i)
    }
}

inline fun BitSet.forEachClearBit(action: (index: Int) -> Unit) {
    var i: Int = nextClearBit(0)
    while (i >= 0) {
        // operate on index i here
        if (i == Int.MAX_VALUE) {
            break // or (i+1) would overflow
        }
        i = nextClearBit(i + 1)
        action(i)
    }
}

val BitSet.bitCountOfSet: Int
    get() = toLongArray().sumOf { it.countOneBits() }
