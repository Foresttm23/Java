package pr1;

// Для контролю доступу до банкоматів
import java.util.concurrent.Semaphore;
// Для встановлення часу очікування
import java.util.concurrent.TimeUnit;

class Bank {
    private final Semaphore atms; // Семафор
    private volatile boolean open; // Стан банку

    public Bank(int atmCount) {
        this.atms = new Semaphore(atmCount);
        this.open = true;
    }

    // Використання банкомату клієнтом
    public void useATM(String clientName) {
        if (!open) {
            System.out.println(clientName + ": Банк вже зачинений. Я піду додому.");
            return;
        }

        try {
            System.out.println(clientName + " чекає на вільний банкомат... 🏧");
            // Кожен клієнт чекає максимум 5 секунд, якщо місця немає, то він уходить
            if (atms.tryAcquire(5, TimeUnit.SECONDS)) {
                System.out.println(clientName + " користується банкоматом 💳");
                // Симуляція використання банкомату, умовно 2 секунди
                Thread.sleep(3000);
                System.out.println(clientName + " закінчив операцію і звільнив банкомат. ✅");
                // Звільняємо місце для наступного клієнта
                atms.release();
            } else {
                System.out.println(clientName + ": Занадто довго чекати. ⏳");
            }
        } catch (InterruptedException e) {
            System.out.println(clientName + ": Виникла помилка під час зняття грошей. ❌");
            Thread.currentThread().interrupt();
        }
    }

    public void closeBank() {
        open = false;
        System.out.println("\nБанк зачинено! Нові клієнти не можуть користуватись банкоматами. ⏰\n");
    }

    public boolean isOpen() {
        return open;
    }
}
