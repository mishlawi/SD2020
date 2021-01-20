import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//Suponha que cada thread apenas vai invocar await uma vez sobre o objecto.



public class Barrier {
    private ReentrantLock lock = new ReentrantLock();
    private Condition cond = lock.newCondition();
    private int n;
    private int conta = 0;


    Barrier (int N) { this.n = N;}


    void await() throws InterruptedException {

        lock.lock(); //thread adquire o lock
        try {
            conta++;
            if (conta < n) {
                while (conta < n)
                    //havendo um acordar espontâneo do thread
                    // este ciclo impede que este siga sem a condiçao ser cumprida
                    //(nao e necessario ser um while)
                    //interessa e reavaliar continuamente
                    cond.await();
            } else cond.signalAll();
        }
        finally {
            lock.unlock();
        }
        }

}
