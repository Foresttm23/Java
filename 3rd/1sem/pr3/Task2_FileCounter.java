import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Stream;

public class Task2_FileCounter {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Введіть шлях до директорії: ");
        String path = scanner.nextLine();
        scanner.close();

        File dir = new File(path);

        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("Помилка: вказаний шлях не існує.");
            return;
        }

        System.out.println("Починаємо сканування...");

        try (ForkJoinPool pool = new ForkJoinPool()) {
            FileWalkTask mainTask = new FileWalkTask(dir);

            long start = System.currentTimeMillis();
            List<String> results = pool.invoke(mainTask);
            long end = System.currentTimeMillis();

            System.out.println("\n--- Результати: ---");
            if (results.isEmpty()) {
                System.out.println("Текстових файлів не знайдено.");
            } else {
                for (String line : results) {
                    System.out.println(line);
                }
            }
            System.out.println("Загальний час виконання: " + (end - start) + " мс");
        }
    }

    static class FileWalkTask extends RecursiveTask<List<String>> {
        private final File directory;

        public FileWalkTask(File directory) {
            this.directory = directory;
        }

        @Override
        protected List<String> compute() {
            List<String> results = new ArrayList<>();
            List<FileWalkTask> subTasks = new ArrayList<>();

            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        FileWalkTask task = new FileWalkTask(file);
                        task.fork();
                        subTasks.add(task);
                    } else {
                        if (file.getName().endsWith(".txt")) {
                            long count = countChars(file);
                            results.add("Файл: " + file.getAbsolutePath() + " | Символів: " + count);
                        }
                    }
                }
            }

            for (FileWalkTask task : subTasks) {
                results.addAll(task.join());
            }

            return results;
        }

        private long countChars(File file) {
            try (Stream<String> lines = Files.lines(file.toPath())) {
                return lines.mapToLong(String::length).sum();
            } catch (IOException e) {
                return -1;
            }
        }
    }
}