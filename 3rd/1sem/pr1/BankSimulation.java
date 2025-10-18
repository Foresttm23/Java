package pr1;

// Основний клас з головним методом
public class BankSimulation {
    public static void main(String[] args) {
        // У банку 3 банкомати
        Bank bank = new Bank(3);

        // Імітуємо клієнтів на день, умовно 8
        Thread[] clients = new Thread[8];
        for (int i = 0; i < clients.length; i++) {
            clients[i] = new Thread(new Client("Клієнт " + (i + 1), bank));
            clients[i].start();
        }

        try {
            // Імітуємо закриття банку
            Thread.sleep(8000);
            bank.closeBank();
        } catch (InterruptedException e) {
            System.out.println("Помилка при завершенні роботи банку.");
            Thread.currentThread().interrupt();
        }

        // Імітуємо пізніх клієнтів
        for (int i = 0; i < 3; i++) {
            new Thread(new Client("Пізній клієнт " + (i + 1), bank)).start();
        }
    }
}
