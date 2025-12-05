import java.util.*;
import java.util.concurrent.*;

public class AsyncArrayProcessorUpdated {

    public static void main(String[] args) {
        Random random = new Random();

        List<Integer> range = getUserRange();
        int min = range.get(0);
        int max = range.get(1);

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

        int threadCount = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futuresTemp = new ArrayList<>();

        // Racing possibility
        Runnable producerTask = () -> {
            ThreadLocalRandom threadRandom = ThreadLocalRandom.current();
            while (dataSet.size() < arraySize) {
                int val = (max - min < arraySize)
                        ? threadRandom.nextInt(1001)
                        : threadRandom.nextInt(max - min + 1) + min;
                dataSet.add(val);
            }
        };

        for (int i = 0; i < threadCount; i++) {
            futuresTemp.add(executor.submit(producerTask));
        }

        waitForTasks(futuresTemp);

        // Racing condition fix
        while (dataSet.size() > arraySize) {
            Integer last = dataSet.iterator().next();
            dataSet.remove(last);
        }

        System.out.println("\n------------------------------------------------");
        System.out.println("Масив згенеровано: \n" + dataSet);

        long startTime = System.currentTimeMillis();

        List<Integer> dataList = new ArrayList<>(dataSet);

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

        waitForTasks(futures);
        double totalSum = handleFutureResults(futures);

        executor.shutdown();
        long endTime = System.currentTimeMillis();

        printResults(totalSum, dataList.size(), startTime, endTime);
    }

    private static void waitForTasks(List<? extends Future<?>> futures) {
        boolean allDone = false;
        while (!allDone) {
            allDone = true;
            for (Future<?> future : futures) {
                if (!future.isDone()) {
                    allDone = false;
                    break;
                }
            }

            if (!allDone) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static double handleFutureResults(List<Future<Double>> futures) {
        double totalSum = 0;
        for (Future<Double> future : futures) {
            try {
                if (future.isCancelled()) {
                    System.out.println("Помилка: Одне із завдань було скасовано!");
                    return 0;
                }
                totalSum += future.get();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            } catch (ExecutionException e) {
                System.out.println("Помилка під час виконання завдання: " + e.getCause());
            }
        }
        return totalSum;
    }

    private static void printResults(double totalSum, int dataListSize, long startTime, long endTime) {
        System.out.println("------------------------------------------------\n");
        double average = totalSum / dataListSize;

        System.out.printf("Загальна сума: %.2f%n", totalSum);
        System.out.printf("Середнє значення масиву: %.2f%n", average);

        System.out.println("Час роботи програми: " + (endTime - startTime) + " мс");
    }

    private static List<Integer> getUserRange() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Введіть мінімальне число діапазону (наприклад, 0):");
        int min = scanner.nextInt();

        System.out.println("Введіть максимальне число діапазону (наприклад, 1000):");
        int max = scanner.nextInt();

        scanner.close();

        return Arrays.asList(min, max);
    }
}