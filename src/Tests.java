import java.util.Random;

public class Tests {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        testDynamicArray();
        testLinkedList();
        testMinHeap();

        System.out.println();
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }

  

    static void check(boolean condition, String description) {
        if (condition) {
            passed++;
        } else {
            failed++;
            System.out.println("FAILED: " + description);
        }
    }

    static void expectThrows(Runnable r, String description) {
        try {
            r.run();
            failed++;
            System.out.println("FAILED (expected exception): " + description);
        } catch (IndexOutOfBoundsException | IllegalStateException e) {
            passed++;
        }
    }

    

    static void testDynamicArray() {
        System.out.println("DynamicArray");

        DynamicArray<Integer> empty = new DynamicArray<>();
        check(empty.size() == 0, "empty structure: size == 0");
        check(empty.isEmpty(), "empty structure: isEmpty");
        check(!empty.contains(1), "empty structure: contains(x) is false");
        expectThrows(() -> empty.get(0), "empty structure: get(0) throws");
        expectThrows(() -> empty.remove(0), "empty structure: remove(0) throws");

        DynamicArray<Integer> one = new DynamicArray<>();
        one.add(42);
        check(one.size() == 1, "one element: size == 1");
        check(one.get(0) == 42, "one element: get(0) correct");
        check(one.contains(42), "one element: contains true");
        check(!one.contains(7), "one element: contains false for missing");

        DynamicArray<Integer> arr = new DynamicArray<>();
        for (int i = 0; i < 20; i++) arr.add(i);
        check(arr.size() == 20, "multiple elements: size correct");
        for (int i = 0; i < 20; i++) check(arr.get(i) == i, "multiple elements: get(" + i + ")");

        
        DynamicArray<Integer> dup = new DynamicArray<>();
        dup.add(5); dup.add(5); dup.add(5);
        check(dup.size() == 3, "duplicates: size == 3");
        check(dup.contains(5), "duplicates: contains true");

        
        DynamicArray<Integer> b = new DynamicArray<>();
        b.add(1); b.add(2); b.add(3);
        b.add(0, 0);            
        b.add(b.size(), 4);    
        check(b.get(0) == 0 && b.get(b.size() - 1) == 4, "boundary indices: front/end insert");
        expectThrows(() -> b.add(-1, 99), "invalid index: add(-1, x) throws");
        expectThrows(() -> b.add(b.size() + 1, 99), "invalid index: add(size+1, x) throws");
        expectThrows(() -> b.get(-1), "invalid index: get(-1) throws");
        expectThrows(() -> b.get(b.size()), "invalid index: get(size) throws");

        
        DynamicArray<Integer> r = new DynamicArray<>();
        for (int i = 0; i < 5; i++) r.add(i); // [0,1,2,3,4]
        int removed = r.remove(2); 
        check(removed == 2, "remove: returns removed element");
        check(r.size() == 4, "remove: size decreases");
        check(r.get(2) == 3, "remove: subsequent elements shift left");

        
        DynamicArray<Integer> large = new DynamicArray<>();
        Random rnd = new Random(42);
        for (int i = 0; i < 50_000; i++) large.add(rnd.nextInt(1_000_000));
        check(large.size() == 50_000, "large input: size correct");
        check(large.contains(large.get(25_000)), "large input: contains matches get");
    }

    

    static void testLinkedList() {
        System.out.println("-- LinkedList --");

        LinkedList<Integer> empty = new LinkedList<>();
        check(empty.size() == 0, "empty structure: size == 0");
        check(!empty.contains(1), "empty structure: contains(x) is false");
        expectThrows(() -> empty.get(0), "empty structure: get(0) throws");
        expectThrows(() -> empty.remove(0), "empty structure: remove(0) throws");

        LinkedList<Integer> one = new LinkedList<>();
        one.add(42);
        check(one.size() == 1, "one element: size == 1");
        check(one.get(0) == 42, "one element: get(0) correct");

        LinkedList<Integer> list = new LinkedList<>();
        for (int i = 0; i < 20; i++) list.add(i);
        check(list.size() == 20, "multiple elements: size correct");
        for (int i = 0; i < 20; i++) check(list.get(i) == i, "multiple elements: get(" + i + ")");

        LinkedList<Integer> dup = new LinkedList<>();
        dup.add(5); dup.add(5); dup.add(5);
        check(dup.contains(5), "duplicates: contains true");

        LinkedList<Integer> b = new LinkedList<>();
        b.add(1); b.add(2); b.add(3);
        b.add(0, 0);
        b.add(b.size(), 4);
        check(b.get(0) == 0 && b.get(b.size() - 1) == 4, "boundary indices: front/end insert");
        expectThrows(() -> b.add(-1, 99), "invalid index: add(-1, x) throws");
        expectThrows(() -> b.get(b.size()), "invalid index: get(size) throws");

        LinkedList<Integer> r = new LinkedList<>();
        for (int i = 0; i < 5; i++) r.add(i);
        int removed = r.remove(2);
        check(removed == 2, "remove: returns removed element");
        check(r.get(2) == 3, "remove: subsequent elements shift");

        
        LinkedList<Integer> t = new LinkedList<>();
        t.add(1); t.add(2);
        t.remove(1); // remove tail (index 1)
        t.add(3);    
        check(t.get(1) == 3, "tail pointer stays correct after removing last node");

        LinkedList<Integer> large = new LinkedList<>();
        Random rnd = new Random(42);
        for (int i = 0; i < 20_000; i++) large.add(rnd.nextInt(1_000_000));
        check(large.size() == 20_000, "large input: size correct");
    }

    

    static void testMinHeap() {
        System.out.println(" MinHeap ");

        MinHeap empty = new MinHeap();
        check(empty.isEmpty(), "empty heap: isEmpty");
        expectThrows(empty::peekMin, "empty heap: peekMin throws");
        expectThrows(empty::extractMin, "empty heap: extractMin throws");

        MinHeap one = new MinHeap();
        one.insert(10);
        check(one.peekMin() == 10, "one element: peekMin correct");
        check(one.extractMin() == 10, "one element: extractMin correct");
        check(one.isEmpty(), "one element: empty after extract");

        MinHeap h = new MinHeap();
        int[] values = {5, 3, 8, 1, 9, 2, 7, 3, 3}; 
        for (int v : values) {
            h.insert(v);
            check(h.isValidHeap(), "heap property holds after insert(" + v + ")");
        }

        int[] sorted = values.clone();
        java.util.Arrays.sort(sorted);
        int[] extracted = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            extracted[i] = h.extractMin();
            check(h.isValidHeap(), "heap property holds after extractMin (step " + i + ")");
        }
        check(java.util.Arrays.equals(sorted, extracted), "extractMin returns non-decreasing order");

        
        MinHeap large = new MinHeap();
        Random rnd = new Random(42);
        int n = 50_000;
        int[] input = new int[n];
        for (int i = 0; i < n; i++) {
            input[i] = rnd.nextInt(1_000_000);
            large.insert(input[i]);
        }
        check(large.isValidHeap(), "large input: heap property holds");
        int[] expectedSorted = input.clone();
        java.util.Arrays.sort(expectedSorted);
        boolean nonDecreasing = true;
        int prev = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            int v = large.extractMin();
            if (v < prev) nonDecreasing = false;
            prev = v;
        }
        check(nonDecreasing, "large input: extractMin order non-decreasing");
        check(large.isEmpty(), "large input: heap empty after extracting all");
    }
}

