import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Runs the cutoff experiment and writes results/raw.csv. */
public final class Benchmark {
    private static final int[] CUTOFFS = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30, 40, 50, 75, 100};
    private static final String[] ALGORITHMS = {"mergesort", "quicksort"};
    private static final String[] DISTRIBUTIONS = {"random", "sorted", "reversed", "fewuniq"};

    public static void main(String[] args) throws IOException {
        String sizeOption = System.getProperty("sizes", "10000,100000,1000000");
        String repsOption = System.getProperty("reps");
        for (String arg : args) {
            if (arg.startsWith("--sizes=")) sizeOption = arg.substring("--sizes=".length());
            else if (arg.startsWith("--reps=")) repsOption = arg.substring("--reps=".length());
            else throw new IllegalArgumentException("Unknown option: " + arg);
        }
        int[] sizes = parseSizes(sizeOption);
        Integer forcedReps = repsOption == null ? null : Integer.valueOf(repsOption);
        Path results = Path.of("results");
        Files.createDirectories(results);
        try (BufferedWriter out = Files.newBufferedWriter(results.resolve("raw.csv"))) {
            out.write("algo,dist,n,cutoff,rep,time_ns\n");
            for (String algorithm : ALGORITHMS) {
                for (String distribution : DISTRIBUTIONS) {
                    for (int n : sizes) {
                        int[] original = load(Path.of("data", distribution + "_" + n + ".txt"), n);
                        int reps = forcedReps != null ? forcedReps : (n <= 100_000 ? 15 : 9);
                        warmUp(algorithm, original, n);
                        for (int rep = 1; rep <= reps; rep++) {
                            List<Integer> cutoffs = cutoffOrder(algorithm, distribution, n, rep);
                            for (int cutoff : cutoffs) {
                                int[] copy = original.clone();
                                long start = System.nanoTime();
                                run(algorithm, copy, cutoff, rep);
                                long elapsed = System.nanoTime() - start;
                                out.write(String.format("%s,%s,%d,%d,%d,%d%n", algorithm, distribution, n, cutoff, rep, elapsed));
                            }
                            out.flush();
                        }
                        System.out.printf("Completed %s %s n=%d (%d reps)%n", algorithm, distribution, n, reps);
                    }
                }
            }
        }
    }

    private static void warmUp(String algorithm, int[] original, int n) {
        for (int round = 0; round < 5; round++) {
            for (int cutoff : CUTOFFS) {
                int[] copy = original.clone();
                run(algorithm, copy, cutoff, -1L - round);
            }
        }
    }

    private static void run(String algorithm, int[] a, int cutoff, long rep) {
        if (algorithm.equals("mergesort")) MergeSort.sort(a, cutoff);
        else QuickSort.sort(a, cutoff, 42L + rep);
    }

    private static List<Integer> cutoffOrder(String algorithm, String distribution, int n, int rep) {
        List<Integer> order = new ArrayList<>();
        for (int cutoff : CUTOFFS) order.add(cutoff);
        long seed = 31L * algorithm.hashCode() + 17L * distribution.hashCode() + n + rep;
        Collections.shuffle(order, new Random(seed));
        return order;
    }

    private static int[] load(Path file, int expectedSize) throws IOException {
        int[] a = new int[expectedSize];
        try (var lines = Files.lines(file)) {
            var iterator = lines.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                if (i == a.length) throw new IOException("Too many values in " + file);
                a[i++] = Integer.parseInt(iterator.next());
            }
            if (i != a.length) throw new IOException("Expected " + a.length + " values in " + file + ", found " + i);
        }
        return a;
    }

    private static int[] parseSizes(String value) {
        String[] parts = value.split(",");
        int[] sizes = new int[parts.length];
        for (int i = 0; i < parts.length; i++) sizes[i] = Integer.parseInt(parts[i].trim());
        return sizes;
    }
}
