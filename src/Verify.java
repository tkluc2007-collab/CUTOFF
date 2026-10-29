import java.util.Arrays;
import java.util.Random;

/** Correctness checks required before collecting timing data. */
public final class Verify {
    private static final int[] CUTOFFS = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30, 40, 50, 75, 100};
    private static final int[] SIZES = {0, 1, 2, 3, 10, 100, 1_000, 100_000};

    public static void main(String[] args) {
        int passed = 0;
        int total = 0;
        for (String kind : new String[] {"random", "sorted", "reversed", "duplicates"}) {
            for (int n : SIZES) {
                int[] input = input(kind, n);
                for (int cutoff : CUTOFFS) {
                    total += 2;
                    passed += verify("MergeSort", kind, n, cutoff, input, false) ? 1 : 0;
                    passed += verify("QuickSort", kind, n, cutoff, input, true) ? 1 : 0;
                }
            }
        }
        System.out.printf("%s: %d/%d checks passed%n", passed == total ? "PASS" : "FAIL", passed, total);
        if (passed != total) System.exit(1);
    }

    private static boolean verify(String algorithm, String kind, int n, int cutoff, int[] input, boolean quick) {
        int[] actual = input.clone();
        int[] expected = input.clone();
        Arrays.sort(expected);
        if (quick) QuickSort.sort(actual, cutoff, 10_000L + cutoff);
        else MergeSort.sort(actual, cutoff);
        boolean ok = Arrays.equals(actual, expected);
        System.out.printf("%s %s n=%d cutoff=%d: %s%n", algorithm, kind, n, cutoff, ok ? "PASS" : "FAIL");
        return ok;
    }

    private static int[] input(String kind, int n) {
        int[] a = new int[n];
        Random random = new Random(1234L + n);
        for (int i = 0; i < n; i++) {
            switch (kind) {
                case "random" -> a[i] = random.nextInt(Integer.MAX_VALUE);
                case "sorted" -> a[i] = i;
                case "reversed" -> a[i] = n - i;
                case "duplicates" -> a[i] = random.nextInt(100);
                default -> throw new AssertionError(kind);
            }
        }
        return a;
    }
}
