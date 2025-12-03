import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;

public class Task1_ArraySearch {

    private static final int THRESHOLD = 1000;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введіть кількість рядків: ");
        int rows = validateInput(scanner);
        System.out.print("Введіть кількість стовпців: ");
        int cols = validateInput(scanner);
        scanner.close();

        int[][] matrix = generateMatrix(rows, cols);

        int firstElement = matrix[0][0];
        System.out.println("Перший елемент: " + firstElement);
        int thresholdValue = firstElement * 2;
        System.out.println("Шукаємо мінімальний елемент > " + thresholdValue);

        if (rows * cols <= 200) {
            printMatrix(matrix);
        } else {
            System.out.println("Масив занадто великий для виведення.");
        }

        // Warm up JVM
        ForkJoinPool warmupPool = new ForkJoinPool();
        warmupPool.invoke(new FindMinTask(matrix, 0, rows, thresholdValue));
        solveWithWorkDealing(matrix, thresholdValue);

        long startStealing = System.nanoTime();
        ForkJoinPool fjPool = new ForkJoinPool();
        FindMinTask fjTask = new FindMinTask(matrix, 0, rows, thresholdValue);
        int resultStealing = fjPool.invoke(fjTask); // Wait for result
        long endStealing = System.nanoTime();

        long startDealing = System.nanoTime();
        int resultDealing = solveWithWorkDealing(matrix, thresholdValue);
        long endDealing = System.nanoTime();

        System.out.println("\n--- Результати ---");
        printResult("Work Stealing (ForkJoin)", resultStealing, endStealing - startStealing);
        printResult("Work Dealing (ExecutorService)", resultDealing, endDealing - startDealing);
    }

    // Work Stealing
    static class FindMinTask extends RecursiveTask<Integer> {
        int[][] matrix;
        int startRow, endRow;
        int thresholdValue;

        public FindMinTask(int[][] matrix, int startRow, int endRow, int thresholdValue) {
            this.matrix = matrix;
            this.startRow = startRow;
            this.endRow = endRow;
            this.thresholdValue = thresholdValue;
        }

        @Override
        protected Integer compute() {
            if ((endRow - startRow) <= THRESHOLD) {
                int min = Integer.MAX_VALUE;
                for (int i = startRow; i < endRow; i++) {
                    for (int val : matrix[i]) {
                        if (val > thresholdValue && val < min) {
                            min = val;
                        }
                    }
                }
                return min;
            } else {
                int mid = startRow + (endRow - startRow) / 2;
                FindMinTask left = new FindMinTask(matrix, startRow, mid, thresholdValue);
                FindMinTask right = new FindMinTask(matrix, mid, endRow, thresholdValue);
                left.fork();
                int rightResult = right.compute();
                int leftResult = left.join();
                return Math.min(leftResult, rightResult);
            }
        }
    }

    // Work Dealing
    private static int solveWithWorkDealing(int[][] matrix, int thresholdValue)
            throws InterruptedException, ExecutionException {
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cores);
        List<Callable<Integer>> tasks = new ArrayList<>();

        int rowsPerThread = Math.max(1, matrix.length / cores);

        for (int i = 0; i < matrix.length; i += rowsPerThread) {
            int finalI = i;
            int end = Math.min(i + rowsPerThread, matrix.length);

            tasks.add(() -> {
                int min = Integer.MAX_VALUE;
                for (int r = finalI; r < end; r++) {
                    for (int val : matrix[r]) {
                        if (val > thresholdValue && val < min) {
                            min = val;
                        }
                    }
                }
                return min;
            });
        }

        List<Future<Integer>> results = executor.invokeAll(tasks);
        int globalMin = Integer.MAX_VALUE;
        for (Future<Integer> f : results) {
            globalMin = Math.min(globalMin, f.get());
        }
        executor.shutdown();
        return globalMin;
    }

    private static int validateInput(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.println("Це не число. Спробуйте ще раз:");
            scanner.next();
        }
        int val = scanner.nextInt();
        return Math.max(val, 1);
    }

    private static int[][] generateMatrix(int rows, int cols) {
        Random rand = new Random();
        int[][] matrix = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = rand.nextInt(1001);
            }
        }
        return matrix;
    }

    private static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int val : row) {
                System.out.printf("%4d", val);
            }
            System.out.println();
        }
    }

    private static void printResult(String method, int result, long nanos) {
        String resStr = (result == Integer.MAX_VALUE) ? "Не знайдено" : String.valueOf(result);
        System.out.printf("%-30s | Результат: %-10s | Час: %.3f мс%n",
                method, resStr, nanos / 1_000_000.0);
    }
}