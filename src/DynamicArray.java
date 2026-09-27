public class DynamicArray<T> {

    private Object[] data;
    private int size;

    private static final int DEFAULT_CAPACITY = 8;

    public DynamicArray() {
        data = new Object[DEFAULT_CAPACITY];
        size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }


    public void add(T x) {
        ensureCapacity(size + 1);
        data[size] = x;
        size++;
    }


    public void add(int index, T x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        ensureCapacity(size + 1);


        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
        }
        data[index] = x;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        T removed = (T) data[index];

        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
        }
        data[size - 1] = null; 
        size--;
        return removed;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return (T) data[index];
    }

  
    public boolean contains(T x) {
        for (int i = 0; i < size; i++) {
            if (data[i] == null ? x == null : data[i].equals(x)) {
                return true;
            }
        }
        return false;
    }

 
    public long containsWithComparisons(T x, long[] comparisonCounterOut) {
        long comparisons = 0;
        boolean found = false;
        for (int i = 0; i < size; i++) {
            comparisons++;
            if (data[i] == null ? x == null : data[i].equals(x)) {
                found = true;
                break;
            }
        }
        comparisonCounterOut[0] = comparisons;
        return found ? 1 : 0;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > data.length) {
            int newCapacity = data.length * 2;
            if (newCapacity < minCapacity) newCapacity = minCapacity;
            Object[] newData = new Object[newCapacity];
            System.arraycopy(data, 0, newData, 0, size);
            data = newData;
        }
    }
}