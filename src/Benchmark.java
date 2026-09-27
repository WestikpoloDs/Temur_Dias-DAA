import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;


public class Benchmark {

    static final int[] N_VALUES = {100, 1_000, 10_000, 100_000};
    static final int REPEATS = 5;
    static final long SEED = 42;
    static final String OUT_DIR = "results/tables/";

    public static void main(String[] args) throws IOException {
        new java.io.File(OUT_DIR).mkdirs();
        workload1_RandomAccess();
        workload2_Search();
        workload3_InsertRemove();
        workload4_PriorityProcessing();
        System.out.println("All workloads complete. CSV files written to " + OUT_DIR);
    }


    static void workload1_RandomAccess() throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(OUT_DIR + "workload1_random_access.csv"))) {
            out.println("structure,n,avg_time_ms,accesses");
            int m = 10_000;

            for (int n : N_VALUES) {
                
                {
                    double totalMs = 0;
                    long accesses = 0;
                    for (int rep = 0; rep < REPEATS; rep++) {
                        DynamicArray<Integer> arr = new DynamicArray<>();
                        Random r = new Random(SEED);
                        for (int i = 0; i < n; i++) arr.add(r.nextInt());
                        Random idxRnd = new Random(SEED + 1);
                        int[] indices = new int[m];
                        for (int i = 0; i < m; i++) indices[i] = idxRnd.nextInt(n);

                        long start = System.nanoTime();
                        int sink = 0;
                        for (int i = 0; i < m; i++) sink += arr.get(indices[i]);
                        long end = System.nanoTime();
                        totalMs += (end - start) / 1_000_000.0;
                        accesses = m;
                        if (sink == Integer.MIN_VALUE) System.out.print(""); 
                    }
                    out.println("DynamicArray," + n + "," + (totalMs / REPEATS) + "," + accesses);
                }

                
                {
                    double totalMs = 0;
                    long accesses = 0;
                    for (int rep = 0; rep < REPEATS; rep++) {
                        LinkedList<Integer> list = new LinkedList<>();
                        Random r = new Random(SEED);
                        for (int i = 0; i < n; i++) list.add(r.nextInt());
                        Random idxRnd = new Random(SEED + 1);
                        int[] indices = new int[m];
                        for (int i = 0; i < m; i++) indices[i] = idxRnd.nextInt(n);

                        long start = System.nanoTime();
                        int sink = 0;
                        for (int i = 0; i < m; i++) sink += list.get(indices[i]);
                        long end = System.nanoTime();
                        totalMs += (end - start) / 1_000_000.0;
                        accesses = m;
                        if (sink == Integer.MIN_VALUE) System.out.print("");
                    }
                    out.println("LinkedList," + n + "," + (totalMs / REPEATS) + "," + accesses);
                }
                System.out.println("Workload1 n=" + n + " done");
            }
        }
    }

   

    static void workload2_Search() throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(OUT_DIR + "workload2_search.csv"))) {
            out.println("structure,n,avg_time_ms,total_comparisons");
            int m = 1_000;

            for (int n : N_VALUES) {
                
                {
                    double totalMs = 0;
                    long totalComparisons = 0;
                    for (int rep = 0; rep < REPEATS; rep++) {
                        DynamicArray<Integer> arr = new DynamicArray<>();
                        Random r = new Random(SEED);
                        int[] stored = new int[n];
                        for (int i = 0; i < n; i++) { stored[i] = r.nextInt(); arr.add(stored[i]); }
                        Random qRnd = new Random(SEED + 2);
                        int[] queries = new int[m];
                        for (int i = 0; i < m; i++) {
                           
                            queries[i] = (qRnd.nextBoolean() && n > 0) ? stored[qRnd.nextInt(n)] : qRnd.nextInt();
                        }

                        long comparisons = 0;
                        long start = System.nanoTime();
                        long[] counter = new long[1];
                        for (int i = 0; i < m; i++) {
                            arr.containsWithComparisons(queries[i], counter);
                            comparisons += counter[0];
                        }
                        long end = System.nanoTime();
                        totalMs += (end - start) / 1_000_000.0;
                        totalComparisons = comparisons;
                    }
                    out.println("DynamicArray," + n + "," + (totalMs / REPEATS) + "," + (totalComparisons));
                }

               
                {
                    double totalMs = 0;
                    long totalComparisons = 0;
                    for (int rep = 0; rep < REPEATS; rep++) {
                        LinkedList<Integer> list = new LinkedList<>();
                        Random r = new Random(SEED);
                        int[] stored = new int[n];
                        for (int i = 0; i < n; i++) { stored[i] = r.nextInt(); list.add(stored[i]); }
                        Random qRnd = new Random(SEED + 2);
                        int[] queries = new int[m];
                        for (int i = 0; i < m; i++) {
                            queries[i] = (qRnd.nextBoolean() && n > 0) ? stored[qRnd.nextInt(n)] : qRnd.nextInt();
                        }

                        long comparisons = 0;
                        long start = System.nanoTime();
                        long[] counter = new long[1];
                        for (int i = 0; i < m; i++) {
                            list.containsWithComparisons(queries[i], counter);
                            comparisons += counter[0];
                        }
                        long end = System.nanoTime();
                        totalMs += (end - start) / 1_000_000.0;
                        totalComparisons = comparisons;
                    }
                    out.println("LinkedList," + n + "," + (totalMs / REPEATS) + "," + (totalComparisons));
                }
                System.out.println("Workload2 n=" + n + " done");
            }
        }
    }

   

    static void workload3_InsertRemove() throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(OUT_DIR + "workload3_insert_remove.csv"))) {
            out.println("structure,n,position,operation,avg_time_ms,element_movements");
            int m = 1_000;

            for (int n : N_VALUES) {
                runInsertRemove(out, "DynamicArray", n, m, 0, "beginning");
                runInsertRemove(out, "LinkedList", n, m, 0, "beginning");
                runInsertRemove(out, "DynamicArray", n, m, n / 2, "middle");
                runInsertRemove(out, "LinkedList", n, m, n / 2, "middle");
                System.out.println("Workload3 n=" + n + " done");
            }
        }
    }

    static void runInsertRemove(PrintWriter out, String structureName, int n, int m, int index, String posLabel) {
        
        double insTotalMs = 0;
        long insMovements = 0;
        for (int rep = 0; rep < REPEATS; rep++) {
            Random r = new Random(SEED);
            Object structure = buildStructure(structureName, n, r);
            Random valRnd = new Random(SEED + 3);

            long start = System.nanoTime();
            for (int i = 0; i < m; i++) {
                int val = valRnd.nextInt();
                int insertAt = Math.min(index, sizeOf(structure, structureName));
                addTo(structure, structureName, insertAt, val);
            }
            long end = System.nanoTime();
            insTotalMs += (end - start) / 1_000_000.0;
            
            insMovements = estimateInsertMovements(structureName, n, m, index);
        }
        out.println(structureName + "," + n + "," + posLabel + ",insert," + (insTotalMs / REPEATS) + "," + insMovements);

        
        double remTotalMs = 0;
        long remMovements = 0;
        for (int rep = 0; rep < REPEATS; rep++) {
            Random r = new Random(SEED);
            Object structure = buildStructure(structureName, n, r);

            long start = System.nanoTime();
            for (int i = 0; i < m && sizeOf(structure, structureName) > 0; i++) {
                int removeAt = Math.min(index, sizeOf(structure, structureName) - 1);
                removeFrom(structure, structureName, removeAt);
            }
            long end = System.nanoTime();
            remTotalMs += (end - start) / 1_000_000.0;
            remMovements = estimateInsertMovements(structureName, n, m, index); // same order of magnitude
        }
        out.println(structureName + "," + n + "," + posLabel + ",remove," + (remTotalMs / REPEATS) + "," + remMovements);
    }

    @SuppressWarnings("unchecked")
    static Object buildStructure(String name, int n, Random r) {
        if (name.equals("DynamicArray")) {
            DynamicArray<Integer> a = new DynamicArray<>();
            for (int i = 0; i < n; i++) a.add(r.nextInt());
            return a;
        } else {
            LinkedList<Integer> l = new LinkedList<>();
            for (int i = 0; i < n; i++) l.add(r.nextInt());
            return l;
        }
    }

    @SuppressWarnings("unchecked")
    static int sizeOf(Object s, String name) {
        return name.equals("DynamicArray") ? ((DynamicArray<Integer>) s).size() : ((LinkedList<Integer>) s).size();
    }

    @SuppressWarnings("unchecked")
    static void addTo(Object s, String name, int index, int val) {
        if (name.equals("DynamicArray")) ((DynamicArray<Integer>) s).add(index, val);
        else ((LinkedList<Integer>) s).add(index, val);
    }

    @SuppressWarnings("unchecked")
    static void removeFrom(Object s, String name, int index) {
        if (name.equals("DynamicArray")) ((DynamicArray<Integer>) s).remove(index);
        else ((LinkedList<Integer>) s).remove(index);
    }

    
    static long estimateInsertMovements(String structureName, long n, long m, long index) {
        if (structureName.equals("DynamicArray")) {
            
            return m * Math.max(1, (n - index));
        } else {
            return m * Math.max(1, index);
        }
    }

    

    static void workload4_PriorityProcessing() throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(OUT_DIR + "workload4_priority_processing.csv"))) {
            out.println("n,avg_insert_time_ms,avg_extract_time_ms,total_comparisons,order_verified");

            for (int n : N_VALUES) {
                double insTotal = 0, extTotal = 0;
                long comparisons = 0;
                boolean orderOk = true;

                for (int rep = 0; rep < REPEATS; rep++) {
                    Random r = new Random(SEED);
                    int[] values = new int[n];
                    for (int i = 0; i < n; i++) values[i] = r.nextInt();

                    MinHeap heap = new MinHeap(n);
                    long start = System.nanoTime();
                    for (int i = 0; i < n; i++) heap.insert(values[i]);
                    long end = System.nanoTime();
                    insTotal += (end - start) / 1_000_000.0;
                    long insertComparisons = heap.getAndResetComparisonCount();

                    int prev = Integer.MIN_VALUE;
                    boolean thisRepOk = true;
                    start = System.nanoTime();
                    for (int i = 0; i < n; i++) {
                        int v = heap.extractMin();
                        if (v < prev) thisRepOk = false;
                        prev = v;
                    }
                    end = System.nanoTime();
                    extTotal += (end - start) / 1_000_000.0;
                    long extractComparisons = heap.getAndResetComparisonCount();

                    comparisons = insertComparisons + extractComparisons;
                    orderOk = orderOk && thisRepOk;
                }
                out.println(n + "," + (insTotal / REPEATS) + "," + (extTotal / REPEATS) + "," + comparisons + "," + orderOk);
                System.out.println("Workload4 n=" + n + " done");
            }
        }
    }
}

