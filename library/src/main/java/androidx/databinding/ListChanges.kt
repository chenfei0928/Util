package androidx.databinding

import androidx.core.util.Pools

/**
 * @author chenf()
 * @date 2023-02-17 16:07
 */
data class ListChanges(
    var notificationType: Int = 0,
    var start: Int = 0,
    var count: Int = 0,
    var to: Int = 0,
) {

    abstract class ListCallback : ObservableList.OnListChangedCallback<ObservableList<Any>>() {
        protected abstract fun onNotifyCallback(sender: ObservableList<Any>, changes: ListChanges)

        override fun onChanged(sender: ObservableList<Any>) {
            val listChanges = sListChanges.acquire() ?: ListChanges()
            onNotifyCallback(sender, listChanges.apply {
                notificationType = ALL
                start = 0
                count = 0
                to = 0
            })
            sListChanges.release(listChanges)
        }

        override fun onItemRangeChanged(
            sender: ObservableList<Any>, positionStart: Int, itemCount: Int
        ) {
            val listChanges = sListChanges.acquire() ?: ListChanges()
            onNotifyCallback(sender, listChanges.apply {
                notificationType = CHANGED
                start = positionStart
                count = 0
                to = itemCount
            })
            sListChanges.release(listChanges)
        }

        override fun onItemRangeInserted(
            sender: ObservableList<Any>, positionStart: Int, itemCount: Int
        ) {
            val listChanges = sListChanges.acquire() ?: ListChanges()
            onNotifyCallback(sender, listChanges.apply {
                notificationType = INSERTED
                start = positionStart
                count = 0
                to = itemCount
            })
            sListChanges.release(listChanges)
        }

        override fun onItemRangeMoved(
            sender: ObservableList<Any>, fromPosition: Int, toPosition: Int, itemCount: Int
        ) {
            val listChanges = sListChanges.acquire() ?: ListChanges()
            onNotifyCallback(sender, listChanges.apply {
                notificationType = MOVED
                start = fromPosition
                count = toPosition
                to = itemCount
            })
            sListChanges.release(listChanges)
        }

        override fun onItemRangeRemoved(
            sender: ObservableList<Any>, positionStart: Int, itemCount: Int
        ) {
            val listChanges = sListChanges.acquire() ?: ListChanges()
            onNotifyCallback(sender, listChanges.apply {
                notificationType = REMOVED
                start = positionStart
                count = 0
                to = itemCount
            })
            sListChanges.release(listChanges)
        }

        companion object {
            private val sListChanges = Pools.SynchronizedPool<ListChanges>(10)
        }
    }

    companion object {
        const val ALL = 0
        const val CHANGED = 1
        const val INSERTED = 2
        const val MOVED = 3
        const val REMOVED = 4
    }
}
