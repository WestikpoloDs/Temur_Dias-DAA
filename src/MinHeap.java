public class MinHeap {

    private int[] data;
    private int size;
    private long comparisonCount;

    public MinHeap() {
        data = new int[16];
        size = 0;
    }

    public MinHeap(int initialCapacity) {
        data = new int[Math.max(16, initialCapacity)];
        size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public long getAndResetComparisonCount() {
        long c = comparisonCount;
        comparisonCount = 0;
        return c;
    }


    public void insert(int x) {
        ensureCapacity(size + 1);
        data[size] = x;
        int i = size;
        size++;

        while (i > 0) {
            int parent = (i - 1) / 2;
            comparisonCount++;
            if (data[parent] <= data[i]) {
                break;
            }
            swap(i, parent);
            i = parent;
        }
    }

    public int peekMin() {
        if (size == 0) throw new IllegalStateException("Heap is empty");
        return data[0];
    }

   
    public int extractMin() {
        if (size == 0) throw new IllegalStateException("Heap is empty");
        int min = data[0];
        size--;
        data[0] = data[size];
        data[size] = 0;

        int i = 0;
        while (true) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            int smallest = i;

            if (left < size) {
                comparisonCount++;
                if (data[left] < data[smallest]) smallest = left;
            }
            if (right < size) {
                comparisonCount++;
                if (data[right] < data[smallest]) smallest = right;
            }
            if (smallest == i) break;

            swap(i, smallest);
            i = smallest;
        }
        return min;
    }

    
    public boolean isValidHeap() {
        for (int i = 0; i < size; i++) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            if (left < size && data[left] < data[i]) return false;
            if (right < size && data[right] < data[i]) return false;
        }
        return true;
    }

    private void swap(int a, int b) {
        int tmp = data[a];
        data[a] = data[b];
        data[b] = tmp;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > data.length) {
            int newCapacity = data.length * 2;
            if (newCapacity < minCapacity) newCapacity = minCapacity;
            int[] newData = new int[newCapacity];
            System.arraycopy(data, 0, newData, 0, size);
            data = newData;
        }
    }
}