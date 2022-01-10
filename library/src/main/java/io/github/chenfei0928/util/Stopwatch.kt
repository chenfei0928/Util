package io.github.chenfei0928.util

import android.os.SystemClock
import androidx.annotation.IntRange
import io.github.chenfei0928.collection.sumByLong
import java.util.*

/**
 * 秒表，提供恢复计时、暂停计时、获取当前计时时间
 * Created by MrFeng on 2018/5/8.
 */
class Stopwatch {
    private val data: MutableList<Node> = LinkedList()

    /**
     * 开始、恢复计时
     */
    @Synchronized
    fun start(@IntRange(from = 0) time: Long = SystemClock.uptimeMillis()) {
        data.add(Node(time))
    }

    /**
     * 暂停计时
     */
    @Synchronized
    fun stop(@IntRange(from = 0) time: Long = SystemClock.uptimeMillis()) {
        if (data.isEmpty()) {
            return
        }
        val last = data.last()
        if (last.end == -1L) {
            last.end = time
        }
    }

    /**
     * 整理记录到的播放区间数据，以清理空区间、无效数据，只保留有效区间与未记录完成的。
     * 并将完全重复区间、部分重复区间合并后返回（不包括未记录完成的）
     */
    @Synchronized
    private fun trim(): List<Node> {
        if (data.isEmpty()) {
            return emptyList()
        }
        // 清理空区间的数据（开始时间等于结束时间）、无效数据（开始时间大于结束时间），并保留未记录完成的数据（无结束时间）
        data.removeAll { it.start >= it.end && it.end > 0 }
        // 获取到所有有效数据
        val available = data.takeWhile {
            it.end >= 0
        }.toMutableList()
        available.sort()

        // 清理、合并重复节点范围
        for (i in available.size - 2 downTo 0) {
            val node = available[i]
            val node1 = available[i + 1]
            // 判断下一节点起止范围与当前节点的起止范围
            if (node1.start in node) {
                // 如果下一节点结束位置在当前节点的范围外，合并这两个节点再移除下一节点
                // 否则当前节点完全包含下一节点，直接移除下一节点
                if (node1.end !in node) {
                    node.end = node1.end
                }
                available.remove(node1)
                data.remove(node1)
            }
        }
        return available
    }

    /**
     * 获取当前计时时间和播放范围，不包括未记录完成的
     */
    fun getDurationAndRange(): Pair<Long, List<Node>> {
        val available = trim()
        val duration = available.sumByLong { it.end - it.start }
        return Pair(duration, available)
    }

    /**
     * 当一组计时被上传，移除它们
     */
    @Synchronized
    fun onUploadDuration(duration: List<Node>) {
        data.removeAll(duration)
    }

    /**
     * 如果没有在计时中，并且计时已清空，则认为是闲置中的计时器
     */
    fun isIdle(): Boolean {
        trim()
        return data.isEmpty()
    }

    override fun toString(): String {
        return "Stopwatch(data=$data)"
    }

    data class Node(
            override val start: Long,
            var end: Long = -1
    ) : Comparable<Node>, ClosedRange<Long> {
        override fun compareTo(other: Node): Int {
            return start.compareTo(other.start)
        }

        override val endInclusive: Long = end
    }
}
