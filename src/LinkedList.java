public class LinkedList<T> {

    private static class Node<T> {
        T value;
        Node<T> next;
        Node(T value) { this.value = value; }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void add(T x) {
        Node<T> node = new Node<>(x);
        if (tail == null) {
            head = tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
    }

    public void add(int index, T x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (index == 0) {
            Node<T> node = new Node<>(x);
            node.next = head;
            head = node;
            if (tail == null) tail = node;
            size++;
            return;
        }
        Node<T> prev = head;
        for (int i = 0; i < index - 1; i++) {
            prev = prev.next;
        }
        Node<T> node = new Node<>(x);
        node.next = prev.next;
        prev.next = node;
        if (node.next == null) tail = node;
        size++;
    }

    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        T removedValue;
        if (index == 0) {
            removedValue = head.value;
            head = head.next;
            if (head == null) tail = null;
        } else {
            Node<T> prev = head;
            for (int i = 0; i < index - 1; i++) {
                prev = prev.next;
            }
            Node<T> toRemove = prev.next;
            removedValue = toRemove.value;
            prev.next = toRemove.next;
            if (prev.next == null) tail = prev;
        }
        size--;
        return removedValue;
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.value;
    }

    public boolean contains(T x) {
        Node<T> current = head;
        while (current != null) {
            if (current.value == null ? x == null : current.value.equals(x)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    public long containsWithComparisons(T x, long[] comparisonCounterOut) {
        long comparisons = 0;
        Node<T> current = head;
        boolean found = false;
        while (current != null) {
            comparisons++;
            if (current.value == null ? x == null : current.value.equals(x)) {
                found = true;
                break;
            }
            current = current.next;
        }
        comparisonCounterOut[0] = comparisons;
        return found ? 1 : 0;
    }
}
