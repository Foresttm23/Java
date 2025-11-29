import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class AsyncAssignment {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("--- ЗАПУСК ЗАВДАННЯ 1 ---");
        runTask1().join();

        System.out.println("\n-------------------------\n");

        System.out.println("--- ЗАПУСК ЗАВДАННЯ 2 ---");
        runTask2().join();
    }

    // Task 1
    public static CompletableFuture<Void> runTask1() {
        CompletableFuture<char[]> arrayFuture = CompletableFuture.supplyAsync(() -> {
            long start = System.nanoTime();

            StringBuilder sb = new StringBuilder();
            for (char c = 'a'; c <= 'z'; c++)
                sb.append(c);
            for (char c = 'A'; c <= 'Z'; c++)
                sb.append(c);
            for (char c = '0'; c <= '9'; c++)
                sb.append(c);
            sb.append(" \t!@#$%^&*() ");

            String source = sb.toString();
            int length = 20;
            char[] chars = new char[length];

            for (int i = 0; i < length; i++) {
                int rndIndex = ThreadLocalRandom.current().nextInt(source.length());
                chars[i] = source.charAt(rndIndex);
            }

            logDuration("Task 1", "Генерація масиву", start);
            return chars;
        });

        CompletableFuture<Void> printOriginal = arrayFuture.thenAcceptAsync(chars -> {
            long start = System.nanoTime();

            StringBuilder sb = new StringBuilder("[Task 1] Початковий масив: ");
            for (char c : chars) {
                sb.append(c == '\t' ? "\\t " : c + " ");
            }
            System.out.println(sb);

            logDuration("Task 1", "Вивід масиву", start);
        });

        CompletableFuture<CharAnalysisResult> analysisFuture = arrayFuture.thenApplyAsync(chars -> {
            long start = System.nanoTime();

            List<Character> alphabetic = new ArrayList<>();
            List<Character> spaces = new ArrayList<>();
            List<Character> tabs = new ArrayList<>();
            List<Character> others = new ArrayList<>();

            for (char c : chars) {
                if (Character.isAlphabetic(c))
                    alphabetic.add(c);
                else if (c == ' ')
                    spaces.add(c);
                else if (c == '\t')
                    tabs.add(c);
                else
                    others.add(c);
            }

            logDuration("Task 1", "Аналіз символів", start);
            return new CharAnalysisResult(alphabetic, spaces, tabs, others);
        });

        return printOriginal.thenCombine(analysisFuture, (voidRes, analysisRes) -> analysisRes)
                .thenAcceptAsync(result -> {
                    long start = System.nanoTime();
                    System.out.println("\n[Task 1] Результати:");
                    System.out.println(" Літери: " + result.alphabetic);
                    System.out.println(" Пробіли (к-сть): " + result.spaces.size());
                    System.out.println(" Табуляції (к-сть): " + result.tabs.size());
                    System.out.println(" Інші: " + result.others);
                    logDuration("Task 1", "Вивід результатів", start);
                });
    }

    record CharAnalysisResult(List<Character> alphabetic, List<Character> spaces, List<Character> tabs,
            List<Character> others) {
    }

    // Task 2
    public static CompletableFuture<Void> runTask2() {
        long startTimeGlobal = System.nanoTime();

        CompletableFuture<double[]> sequenceFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("[Task 2] Починаємо генерацію чисел...");
            return IntStream.range(0, 20)
                    .mapToDouble(i -> ThreadLocalRandom.current().nextDouble(-100.0, 100.0))
                    .toArray();
        });

        CompletableFuture<CalculationResult> calculationFuture = sequenceFuture.thenApplyAsync(arr -> {
            double maxDiff = -1.0;
            for (int i = 0; i < arr.length - 1; i++) {
                double diff = Math.abs(arr[i] - arr[i + 1]);
                if (diff > maxDiff) {
                    maxDiff = diff;
                }
            }
            return new CalculationResult(arr, maxDiff);
        });

        return calculationFuture.thenAcceptAsync(res -> {
            System.out.println("[Task 2] Початкова послідовність:");
            for (double d : res.sequence) {
                System.out.printf("%.2f; ", d);
            }
            System.out.println();

            System.out.printf("%n[Task 2] Результат (max diff): %.4f%n", res.maxDifference);

            double durationMs = (System.nanoTime() - startTimeGlobal) / 1_000_000.0;
            System.out.printf("[Task 2] Час роботи усіх асинхронних операцій: %.4f мс%n", durationMs);
        });
    }

    record CalculationResult(double[] sequence, double maxDifference) {
    }

    private static void logDuration(String taskName, String action, long startTime) {
        double duration = (System.nanoTime() - startTime) / 1_000_000.0;
        System.out.printf("[%s] %s: %.4f мс%n", taskName, action, duration);
    }
}