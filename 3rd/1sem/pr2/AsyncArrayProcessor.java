import java.util.*;
import java.util.concurrent.*;

public class AsyncArrayProcessor {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        System.out.println("Введіть мінімальне число діапазону (наприклад, 0):");
        int min = scanner.nextInt();

        System.out.println("Введіть максимальне число діапазону (наприклад, 1000):");
        int max = scanner.nextInt();

        scanner.close();

        if (min >= max) {
            System.out.println("Помилка: Мінімум має бути меншим за максимум.");
            return;
        }

        int arraySize = 40 + random.nextInt(21);

        if (max - min < arraySize) {
            System.out.printf(
                    "%nПопередження: Різниця між мінімумом і максимумом менша за розмір масиву %d елементів. %nГенеруємо додаткові значення в діпазоні [0, 1000]%n",
                    arraySize);
        } else {
            System.out.printf("%nГенеруємо масив розміром %d елементів...%n", arraySize);
        }

        CopyOnWriteArraySet<Integer> dataSet = new CopyOnWriteArraySet<>();

        while (dataSet.size() < arraySize) {
            int val = (max - min < arraySize) ? random.nextInt(1001) : random.nextInt(max - min + 1) + min;
            dataSet.add(val);
        }

        System.out.println("\n------------------------------------------------");
        System.out.println("Масив згенеровано: \n" + dataSet);

        long startTime = System.currentTimeMillis();

        List<Integer> dataList = new ArrayList<>(dataSet);
        int threadCount = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Double>> futures = new ArrayList<>();

        int chunkSize = (int) Math.ceil((double) dataList.size() / threadCount);

        for (int i = 0; i < dataList.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, dataList.size());
            List<Integer> subList = dataList.subList(i, end);

            Callable<Double> task = () -> {
                System.out.println("Потік " + Thread.currentThread().getName() + " обробляє частину: " + subList);
                double sum = 0;
                for (Integer num : subList) {
                    sum += num;
                }
                return sum;
            };

            futures.add(executor.submit(task));
        }

        boolean allDone = false;
        while (!allDone) {
            allDone = true;
            for (Future<Double> future : futures) {
                if (!future.isDone()) {
                    allDone = false;
                    break;
                }
            }

            if (!allDone) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        double totalSum = 0;
        for (Future<Double> future : futures) {
            try {
                if (future.isCancelled()) {
                    System.out.println("Помилка: Одне із завдань було скасовано!");
                    return;
                }
                totalSum += future.get();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            } catch (ExecutionException e) {
                System.out.println("Помилка під час виконання завдання: " + e.getCause());
            }
        }

        executor.shutdown();

        long endTime = System.currentTimeMillis();

        System.out.println("------------------------------------------------\n");
        double average = totalSum / dataList.size();

        System.out.printf("Загальна сума: %.2f%n", totalSum);
        System.out.printf("Середнє значення масиву: %.2f%n", average);

        System.out.println("Час роботи програми: " + (endTime - startTime) + " мс");
    }
}