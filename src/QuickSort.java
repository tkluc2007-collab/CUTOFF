import java.util.Random;

/** Quicksort from the lecture, including Fisher-Yates shuffling. */
public final class QuickSort {
    private QuickSort() { }

    public static void sort(int[] a, int cutoff) {
        sort(a, cutoff, 42L);
    }

    /** Uses a caller-supplied fixed seed so a benchmark repetition is reproducible. */
    public static void sort(int[] a, int cutoff, long shuffleSeed) {
        if (cutoff < 1) throw new IllegalArgumentException("cutoff must be >= 1");
        shuffle(a, new Random(shuffleSeed));
        sort(a, 0, a.length - 1, cutoff);
    }

    private static void shuffle(int[] a, Random random) {
        for (int i = 0; i < a.length; i++) {
            int r = i + random.nextInt(a.length - i);
            Insertion.swap(a, i, r);
        }
    }

    private static void sort(int[] a, int lo, int hi, int cutoff) {
        if (hi <= lo + cutoff - 1) {
            Insertion.insertion(a, lo, hi);
            return;
        }
        int j = partition(a, lo, hi);
        sort(a, lo, j - 1, cutoff);
        sort(a, j + 1, hi, cutoff);
    }

    private static int partition(int[] a, int lo, int hi) {
        int i = lo;
        int j = hi + 1;
        while (true) {
            while (a[++i] < a[lo]) if (i == hi) break;
            while (a[lo] < a[--j]) if (j == lo) break;
            if (i >= j) break;
            Insertion.swap(a, i, j);
        }
        Insertion.swap(a, lo, j);
        return j;
    }
}
