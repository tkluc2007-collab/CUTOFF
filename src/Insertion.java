public final class Insertion {
    private Insertion() { }

    public static void insertion(int[] a, int lo, int hi) {
        for (int i = lo; i <= hi; i++) {
            for (int j = i; j > lo && a[j] < a[j - 1]; j--) {
                swap(a, j, j - 1);
            }
        }
    }

    public static void swap(int[] a, int i, int j) {
        int value = a[i];
        a[i] = a[j];
        a[j] = value;
    }
}
