/**
 * @author chenf()
 * @date 2024-08-01 14:50
 */
package io.github.chenfei0928.util

inline fun <R> Process.use(block: (Process) -> R): R {
    try {
        return block(this)
    } finally {
        this.destroy()
    }
}
