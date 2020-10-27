class Depositos implements Runnable {
    private Bank bank;

    public Depositos(Bank b) {
        this.bank = b;
    }

    public void run() {

        final long I = 1000;
        for (long i = 0; i < I; i++)
            this.bank.deposit(100);

    }
}

public class ex2 {
    public static void main (String[] arg) throws Exception {
        Bank b = new Bank();
        int N = 10;
        Thread threads[] = new Thread[N];


        for (int i = 0; i < N; i++)
            threads[i].start();


        for (int i = 0; i < N; i++)
            threads[i].join();

        for (int i=0;i<N;i++)
            System.out.println(b.balance());
    }

}




