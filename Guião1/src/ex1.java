class Inc implements Runnable {
    public void run() {
        final long I=100;

        for (long i = 0; i < I; i++)
            System.out.println(i);
    }
}

public class ex1 {
    public static void main(String arg[]) throws Exception {
        int N = 10;
        Thread threads[] = new Thread[N];
        Thread t = new Thread(new Inc());
        t.start();
        t.join();


        for (int i = 0; i < N; i++) {
            threads[i] = new Thread(new Inc());
        }
        for (int i = 0; i < N; i++) {
            threads[i].start();

        }
        for (int i = 0; i < N; i++) {
            threads[i].join();
        }

        System.out.println("Fim");
    }

}

