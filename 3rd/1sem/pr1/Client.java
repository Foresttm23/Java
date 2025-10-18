package pr1;

class Client implements Runnable {
    private final String name;
    private final Bank bank;

    public Client(String name, Bank bank) {
        this.name = name;
        this.bank = bank;
    }

    @Override
    public void run() {
        try {
            // Затримка перед приходом клієнта, умовно 5 секунд
            Thread.sleep(5000);
            bank.useATM(name);
        } catch (InterruptedException e) {
            System.out.println(name + ": Мене перервали.");
            Thread.currentThread().interrupt();
        }
    }
}