import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/** Generates the fixed experiment inputs specified in SPEC.md. */
public final class GenData {
    private static final int[] SIZES = {10_000, 100_000, 1_000_000};
    private static final String[] DISTRIBUTIONS = {"random", "sorted", "reversed", "fewuniq"};

    public static void main(String[] args) throws IOException {
        Path directory = Path.of("data");
        Files.createDirectories(directory);
        Random random = new Random(42L);
        for (String distribution : DISTRIBUTIONS) {
            for (int n : SIZES) {
                Path file = directory.resolve(distribution + "_" + n + ".txt");
                write(file, distribution, n, random);
                System.out.println("Wrote " + file);
            }
        }
    }

    private static void write(Path file, String distribution, int n, Random random) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            for (int i = 0; i < n; i++) {
                int value = switch (distribution) {
                    case "random" -> random.nextInt(Integer.MAX_VALUE);
                    case "sorted" -> i;
                    case "reversed" -> n - 1 - i;
                    case "fewuniq" -> random.nextInt(100);
                    default -> throw new AssertionError(distribution);
                };
                writer.write(Integer.toString(value));
                writer.newLine();
            }
        }
    }
}
