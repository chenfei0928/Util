package androidx.collection;

import androidx.annotation.NonNull;

/**
 * 实现算法来自{@link SparseArrayCompat} 和 {@link android.util.SparseIntArray}
 * <p>
 * User: ChenFei(chenfei0928@gmail.com)
 * Date: 2019-05-23
 * Time: 17:46
 *
 * @author MrFeng
 */
public class HashPool implements Cloneable {
    private int[] mValues;
    private int mSize;

    /**
     * Creates a new HashPool containing no mappings.
     */
    public HashPool() {
        this(10);
    }

    /**
     * Creates a new HashPool containing no mappings that will not
     * require any additional memory allocation to store the specified
     * number of mappings.  If you supply an initial capacity of 0, the
     * sparse array will be initialized with a light-weight representation
     * not requiring any additional array allocations.
     */
    public HashPool(int initialCapacity) {
        if (initialCapacity == 0) {
            mValues = AndroidXContainerHelpers.EMPTY_INTS;
        } else {
            initialCapacity = AndroidXContainerHelpers.idealIntArraySize(initialCapacity);
            mValues = new int[initialCapacity];
        }
        mSize = 0;
    }

    /**
     * Primitive int version of {@link #insert(Object[], int, int, Object)}.
     */
    public static int[] insert(int[] array, int currentSize, int index, int element) {
        assert currentSize <= array.length;

        if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        }

        int[] newArray = new int[growSize(currentSize)];
        System.arraycopy(array, 0, newArray, 0, index);
        newArray[index] = element;
        System.arraycopy(array, index, newArray, index + 1, array.length - index);
        return newArray;
    }

    /**
     * Given the current size of an array, returns an ideal size to which the array should grow.
     * This is typically double the given size, but should not be relied upon to do so in the
     * future.
     */
    public static int growSize(int currentSize) {
        return currentSize <= 4 ? 8 : currentSize * 2;
    }

    @Override
    public HashPool clone() {
        HashPool clone;
        try {
            clone = (HashPool) super.clone();
            clone.mValues = mValues.clone();
        } catch (CloneNotSupportedException e) {
            // Cannot happen as we implement Cloneable.
            throw new AssertionError(e);
        }
        return clone;
    }

    public void remove(int value) {
        int index = indexOfValue(value);
        if (index >= 0) {
            removeAt(index);
        }
    }

    public void removeAt(int index) {
        System.arraycopy(mValues, index + 1, mValues, index, mSize - (index + 1));
        mSize--;
    }

    /**
     * Returns the number of value that this HashPool
     * currently stores.
     */
    public int size() {
        return mSize;
    }

    /**
     * Return true if size() is 0.
     *
     * @return true if size() is 0.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Given an index in the range <code>0...size()-1</code>, returns
     * the value from the <code>index</code>th value that this
     * HashPool stores.
     */
    public int valueAt(int index) {
        return mValues[index];
    }

    /**
     * Returns the index for which {@link #valueAt} would return the
     * specified key, or a negative number if the specified
     * key is not mapped.
     */
    public int indexOfValue(int value) {
        return AndroidXContainerHelpers.binarySearch(mValues, mSize, value);
    }

    /**
     * Returns true if the specified value is mapped.
     */
    public boolean contains(int value) {
        return indexOfValue(value) >= 0;
    }

    /**
     * Adds a mapping from the specified value to the specified value,
     * replacing the previous mapping from the specified value if there
     * was one.
     */
    public void put(int value) {
        int i = AndroidXContainerHelpers.binarySearch(mValues, mSize, value);

        if (i < 0) {
            i = ~i;
            mValues = insert(mValues, mSize, i, value);
            mSize++;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation composes a string by iterating over its mappings. If
     * this map contains itself as a value, the string "(this Map)"
     * will appear in its place.
     */
    @NonNull
    @Override
    public String toString() {
        if (size() <= 0) {
            return "{}";
        }

        StringBuilder buffer = new StringBuilder(mSize * 11);
        buffer.append('{');
        for (int i = 0; i < mSize; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            int value = valueAt(i);
            buffer.append(value);
        }
        buffer.append('}');
        return buffer.toString();
    }
}
