package java.util;

import android.os.Build;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import androidx.annotation.RequiresApi;

/**
 * 使用 {@link System#identityHashCode(Object)} 获取 hash 的{@link WeakHashMap}实现
 * 并提供Java8语言功能的实现
 *
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2020-07-29 16:04
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class SystemIdentityWeakHashMapN<K, V>
        extends SystemIdentityWeakHashMap<K, V> {

    /**
     * Constructs a new, empty <tt>SystemIdentityWeakHashMap</tt> with the given initial
     * capacity and the given load factor.
     *
     * @param initialCapacity The initial capacity of the <tt>SystemIdentityWeakHashMap</tt>
     * @param loadFactor      The load factor of the <tt>SystemIdentityWeakHashMap</tt>
     * @throws IllegalArgumentException if the initial capacity is negative,
     *                                  or if the load factor is nonpositive.
     */
    public SystemIdentityWeakHashMapN(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Constructs a new, empty <tt>SystemIdentityWeakHashMap</tt> with the given initial
     * capacity and the default load factor (0.75).
     *
     * @param initialCapacity The initial capacity of the <tt>SystemIdentityWeakHashMap</tt>
     * @throws IllegalArgumentException if the initial capacity is negative
     */
    public SystemIdentityWeakHashMapN(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new, empty <tt>SystemIdentityWeakHashMap</tt> with the default initial
     * capacity (16) and load factor (0.75).
     */
    public SystemIdentityWeakHashMapN() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new <tt>SystemIdentityWeakHashMap</tt> with the same mappings as the
     * specified map.  The <tt>SystemIdentityWeakHashMap</tt> is created with the default
     * load factor (0.75) and an initial capacity sufficient to hold the
     * mappings in the specified map.
     *
     * @param m the map whose mappings are to be placed in this map
     * @throws NullPointerException if the specified map is null
     * @since 1.3
     */
    public SystemIdentityWeakHashMapN(Map<? extends K, ? extends V> m) {
        super(m);
    }

    private class ValueIterator extends HashIterator<V> {
        public V next() {
            return nextEntry().value;
        }
    }

    private class KeyIterator extends HashIterator<K> {
        public K next() {
            return nextEntry().getKey();
        }
    }

    // Views

    transient Set<K> keySet;
    transient Collection<V> values;

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation), the results of
     * the iteration are undefined.  The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     */
    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = new KeySet();
            keySet = ks;
        }
        return ks;
    }

    private class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        public int size() {
            return SystemIdentityWeakHashMapN.this.size();
        }

        public boolean contains(Object o) {
            return containsKey(o);
        }

        public boolean remove(Object o) {
            if (containsKey(o)) {
                SystemIdentityWeakHashMapN.this.remove(o);
                return true;
            } else
                return false;
        }

        public void clear() {
            SystemIdentityWeakHashMapN.this.clear();
        }

        public Spliterator<K> spliterator() {
            return new KeySpliterator<>(SystemIdentityWeakHashMapN.this, 0, -1, 0, 0);
        }
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  If the map is
     * modified while an iteration over the collection is in progress
     * (except through the iterator's own <tt>remove</tt> operation),
     * the results of the iteration are undefined.  The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not
     * support the <tt>add</tt> or <tt>addAll</tt> operations.
     */
    public Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            vs = new Values();
            values = vs;
        }
        return vs;
    }

    private class Values extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        public int size() {
            return SystemIdentityWeakHashMapN.this.size();
        }

        public boolean contains(Object o) {
            return containsValue(o);
        }

        public void clear() {
            SystemIdentityWeakHashMapN.this.clear();
        }

        public Spliterator<V> spliterator() {
            return new ValueSpliterator<>(SystemIdentityWeakHashMapN.this, 0, -1, 0, 0);
        }
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation, or through the
     * <tt>setValue</tt> operation on a map entry returned by the
     * iterator) the results of the iteration are undefined.  The set
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
     * <tt>clear</tt> operations.  It does not support the
     * <tt>add</tt> or <tt>addAll</tt> operations.
     */
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> es = entrySet;
        return es != null ? es : (entrySet = new EntrySet());
    }

    private class EntrySet extends SystemIdentityWeakHashMap<K, V>.EntrySet {

        @NotNull
        @Override
        public Spliterator<Map.Entry<K, V>> spliterator() {
            return new EntrySpliterator<>(SystemIdentityWeakHashMapN.this, 0, -1, 0, 0);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        int expectedModCount = modCount;

        Entry<K, V>[] tab = getTable();
        for (Entry<K, V> entry : tab) {
            while (entry != null) {
                Object key = entry.get();
                if (key != null) {
                    action.accept((K) SystemIdentityWeakHashMapN.unmaskNull(key), entry.value);
                }
                entry = entry.next;

                if (expectedModCount != modCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void replaceAll(@NotNull BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        int expectedModCount = modCount;

        Entry<K, V>[] tab = getTable();
        for (Entry<K, V> entry : tab) {
            while (entry != null) {
                Object key = entry.get();
                if (key != null) {
                    entry.value = function.apply((K) SystemIdentityWeakHashMapN.unmaskNull(key), entry.value);
                }
                entry = entry.next;

                if (expectedModCount != modCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    /**
     * Similar form as other hash Spliterators, but skips dead
     * elements.
     */
    static class SystemIdentityWeakHashMapSpliterator<K, V> {
        final SystemIdentityWeakHashMapN<K, V> map;
        SystemIdentityWeakHashMapN.Entry<K, V> current; // current node
        int index;             // current index, modified on advance/split
        int fence;             // -1 until first use; then one past last index
        int est;               // size estimate
        int expectedModCount;  // for comodification checks

        SystemIdentityWeakHashMapSpliterator(SystemIdentityWeakHashMapN<K, V> m, int origin,
                                             int fence, int est,
                                             int expectedModCount) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() { // initialize fence and size on first use
            int hi;
            if ((hi = fence) < 0) {
                SystemIdentityWeakHashMapN<K, V> m = map;
                est = m.size();
                expectedModCount = m.modCount;
                hi = fence = m.table.length;
            }
            return hi;
        }

        public final long estimateSize() {
            getFence(); // force init
            return est;
        }
    }

    static final class KeySpliterator<K, V>
            extends SystemIdentityWeakHashMapSpliterator<K, V>
            implements Spliterator<K> {
        KeySpliterator(SystemIdentityWeakHashMapN<K, V> m, int origin, int fence, int est,
                       int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public KeySpliterator<K, V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null :
                    new KeySpliterator<K, V>(map, lo, index = mid, est >>>= 1,
                            expectedModCount);
        }

        public void forEachRemaining(Consumer<? super K> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            SystemIdentityWeakHashMapN<K, V> m = map;
            SystemIdentityWeakHashMapN.Entry<K, V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = tab.length;
            } else
                mc = expectedModCount;
            if (tab.length >= hi && (i = index) >= 0 &&
                    (i < (index = hi) || current != null)) {
                SystemIdentityWeakHashMapN.Entry<K, V> p = current;
                current = null; // exhaust
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        Object x = p.get();
                        p = p.next;
                        if (x != null) {
                            @SuppressWarnings("unchecked") K k =
                                    (K) SystemIdentityWeakHashMapN.unmaskNull(x);
                            action.accept(k);
                        }
                    }
                } while (p != null || i < hi);
            }
            if (m.modCount != mc)
                throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            SystemIdentityWeakHashMapN.Entry<K, V>[] tab = map.table;
            if (tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        Object x = current.get();
                        current = current.next;
                        if (x != null) {
                            @SuppressWarnings("unchecked") K k =
                                    (K) SystemIdentityWeakHashMapN.unmaskNull(x);
                            action.accept(k);
                            if (map.modCount != expectedModCount)
                                throw new ConcurrentModificationException();
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return Spliterator.DISTINCT;
        }
    }

    static final class ValueSpliterator<K, V>
            extends SystemIdentityWeakHashMapSpliterator<K, V>
            implements Spliterator<V> {
        ValueSpliterator(SystemIdentityWeakHashMapN<K, V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public ValueSpliterator<K, V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null :
                    new ValueSpliterator<K, V>(map, lo, index = mid, est >>>= 1,
                            expectedModCount);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            SystemIdentityWeakHashMapN<K, V> m = map;
            SystemIdentityWeakHashMapN.Entry<K, V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = tab.length;
            } else
                mc = expectedModCount;
            if (tab.length >= hi && (i = index) >= 0 &&
                    (i < (index = hi) || current != null)) {
                SystemIdentityWeakHashMapN.Entry<K, V> p = current;
                current = null; // exhaust
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        Object x = p.get();
                        V v = p.value;
                        p = p.next;
                        if (x != null)
                            action.accept(v);
                    }
                } while (p != null || i < hi);
            }
            if (m.modCount != mc)
                throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            SystemIdentityWeakHashMapN.Entry<K, V>[] tab = map.table;
            if (tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        Object x = current.get();
                        V v = current.value;
                        current = current.next;
                        if (x != null) {
                            action.accept(v);
                            if (map.modCount != expectedModCount)
                                throw new ConcurrentModificationException();
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return 0;
        }
    }

    static final class EntrySpliterator<K, V>
            extends SystemIdentityWeakHashMapSpliterator<K, V>
            implements Spliterator<Map.Entry<K, V>> {
        EntrySpliterator(SystemIdentityWeakHashMapN<K, V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public EntrySpliterator<K, V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null :
                    new EntrySpliterator<K, V>(map, lo, index = mid, est >>>= 1,
                            expectedModCount);
        }


        public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            SystemIdentityWeakHashMapN<K, V> m = map;
            SystemIdentityWeakHashMapN.Entry<K, V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = tab.length;
            } else
                mc = expectedModCount;
            if (tab.length >= hi && (i = index) >= 0 &&
                    (i < (index = hi) || current != null)) {
                SystemIdentityWeakHashMapN.Entry<K, V> p = current;
                current = null; // exhaust
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        Object x = p.get();
                        V v = p.value;
                        p = p.next;
                        if (x != null) {
                            @SuppressWarnings("unchecked") K k =
                                    (K) SystemIdentityWeakHashMapN.unmaskNull(x);
                            action.accept
                                    (new SimpleImmutableEntry<K, V>(k, v));
                        }
                    }
                } while (p != null || i < hi);
            }
            if (m.modCount != mc)
                throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            SystemIdentityWeakHashMapN.Entry<K, V>[] tab = map.table;
            if (tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        Object x = current.get();
                        V v = current.value;
                        current = current.next;
                        if (x != null) {
                            @SuppressWarnings("unchecked") K k =
                                    (K) SystemIdentityWeakHashMapN.unmaskNull(x);
                            action.accept
                                    (new SimpleImmutableEntry<K, V>(k, v));
                            if (map.modCount != expectedModCount)
                                throw new ConcurrentModificationException();
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return Spliterator.DISTINCT;
        }
    }
}