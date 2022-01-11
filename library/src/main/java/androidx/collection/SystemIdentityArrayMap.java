package androidx.collection;

import java.util.ConcurrentModificationException;

import androidx.annotation.Nullable;

/**
 * 使用 {@link System#identityHashCode(Object)} 获取 hash 的Map实现
 *
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2019-11-12 17:03
 */
public class SystemIdentityArrayMap<K, V> extends ArrayMap<K, V> {
    private static final boolean DEBUG = false;
    private static final String TAG = "ArrayMap";

    /**
     * Attempt to spot concurrent modifications to this data structure.
     * <p>
     * It's best-effort, but any time we can throw something more diagnostic than an
     * ArrayIndexOutOfBoundsException deep in the ArrayMap internals it's going to
     * save a lot of development time.
     * <p>
     * Good times to look for CME include after any allocArrays() call and at the end of
     * functions that change mSize (put/remove/clear).
     */
    private static final boolean CONCURRENT_MODIFICATION_EXCEPTIONS = true;

    /**
     * The minimum amount by which the capacity of a ArrayMap will increase.
     * This is tuned to be relatively space-efficient.
     */
    private static final int BASE_SIZE = 4;

    /**
     * Maximum number of entries to have in array caches.
     */
    private static final int CACHE_SIZE = 10;

    @SuppressWarnings("ArrayToString")
    private static void freeArrays(final int[] hashes, final Object[] array, final int size) {
        if (hashes.length == (BASE_SIZE * 2)) {
            synchronized (SimpleArrayMap.class) {
                if (mTwiceBaseCacheSize < CACHE_SIZE) {
                    array[0] = mTwiceBaseCache;
                    array[1] = hashes;
                    for (int i = (size << 1) - 1; i >= 2; i--) {
                        array[i] = null;
                    }
                    mTwiceBaseCache = array;
                    mTwiceBaseCacheSize++;
                    if (DEBUG) {
                        System.out.println(TAG + " Storing 2x cache " + array
                                + " now have " + mTwiceBaseCacheSize + " entries");
                    }
                }
            }
        } else if (hashes.length == BASE_SIZE) {
            synchronized (SimpleArrayMap.class) {
                if (mBaseCacheSize < CACHE_SIZE) {
                    array[0] = mBaseCache;
                    array[1] = hashes;
                    for (int i = (size << 1) - 1; i >= 2; i--) {
                        array[i] = null;
                    }
                    mBaseCache = array;
                    mBaseCacheSize++;
                    if (DEBUG) {
                        System.out.println(TAG + " Storing 1x cache " + array
                                + " now have " + mBaseCacheSize + " entries");
                    }
                }
            }
        }
    }

    /**
     * Returns the index of a key in the set.
     *
     * @param key The key to search for.
     * @return Returns the index of the key if it exists, else a negative integer.
     */
    @Override
    public int indexOfKey(Object key) {
        return key == null ? indexOfNull()
                : indexOf(key, System.identityHashCode(key));
    }

    /**
     * Add a new value to the array map.
     *
     * @param key   The key under which to store the value.  <b>Must not be null.</b>  If
     *              this key already exists in the array, its value will be replaced.
     * @param value The value to store for the given key.
     * @return Returns the old value that was stored for the given key, or null if there
     * was no such key.
     */
    @Override
    @Nullable
    public V put(@Nullable K key, @Nullable V value) {
        final int osize = mSize;
        final int hash;
        int index;
        if (key == null) {
            hash = 0;
            index = indexOfNull();
        } else {
            hash = System.identityHashCode(key);
            index = indexOf(key, hash);
        }
        if (index >= 0) {
            index = (index << 1) + 1;
            final V old = (V) mArray[index];
            mArray[index] = value;
            return old;
        }

        index = ~index;
        if (osize >= mHashes.length) {
            final int n = osize >= (BASE_SIZE * 2) ? (osize + (osize >> 1))
                    : (osize >= BASE_SIZE ? (BASE_SIZE * 2) : BASE_SIZE);

            if (DEBUG) {
                System.out.println(TAG + " put: grow from " + mHashes.length + " to " + n);
            }

            final int[] ohashes = mHashes;
            final Object[] oarray = mArray;
            allocArrays(n);

            if (CONCURRENT_MODIFICATION_EXCEPTIONS && osize != mSize) {
                throw new ConcurrentModificationException();
            }

            if (mHashes.length > 0) {
                if (DEBUG) {
                    System.out.println(TAG + " put: copy 0-" + osize + " to 0");
                }
                System.arraycopy(ohashes, 0, mHashes, 0, ohashes.length);
                System.arraycopy(oarray, 0, mArray, 0, oarray.length);
            }

            freeArrays(ohashes, oarray, osize);
        }

        if (index < osize) {
            if (DEBUG) {
                System.out.println(TAG + " put: move " + index + "-" + (osize - index)
                        + " to " + (index + 1));
            }
            System.arraycopy(mHashes, index, mHashes, index + 1, osize - index);
            System.arraycopy(mArray, index << 1, mArray, (index + 1) << 1, (mSize - index) << 1);
        }

        if (CONCURRENT_MODIFICATION_EXCEPTIONS) {
            if (osize != mSize || index >= mHashes.length) {
                throw new ConcurrentModificationException();
            }
        }

        mHashes[index] = hash;
        mArray[index << 1] = key;
        mArray[(index << 1) + 1] = value;
        mSize++;
        return null;
    }

    @SuppressWarnings("ArrayToString")
    private void allocArrays(final int size) {
        if (size == (BASE_SIZE * 2)) {
            synchronized (SimpleArrayMap.class) {
                if (mTwiceBaseCache != null) {
                    final Object[] array = mTwiceBaseCache;
                    mArray = array;
                    mTwiceBaseCache = (Object[]) array[0];
                    mHashes = (int[]) array[1];
                    array[0] = array[1] = null;
                    mTwiceBaseCacheSize--;
                    if (DEBUG) {
                        System.out.println(TAG + " Retrieving 2x cache " + mHashes
                                + " now have " + mTwiceBaseCacheSize + " entries");
                    }
                    return;
                }
            }
        } else if (size == BASE_SIZE) {
            synchronized (SimpleArrayMap.class) {
                if (mBaseCache != null) {
                    final Object[] array = mBaseCache;
                    mArray = array;
                    mBaseCache = (Object[]) array[0];
                    mHashes = (int[]) array[1];
                    array[0] = array[1] = null;
                    mBaseCacheSize--;
                    if (DEBUG) {
                        System.out.println(TAG + " Retrieving 1x cache " + mHashes
                                + " now have " + mBaseCacheSize + " entries");
                    }
                    return;
                }
            }
        }

        mHashes = new int[size];
        mArray = new Object[size << 1];
    }
}
