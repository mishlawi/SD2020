import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Math.max;

public class Agreement {

    private static class Instance {
        int result = Integer.MIN_VALUE;
        int c = 0; //da o nr da thread

    }


    private Instance agree = new Instance();
    private ReentrantLock lock = new ReentrantLock();
    private Condition cond  = lock.newCondition();
    private int n;

    Agreement(int N){
        this.n = N;
    }

    int propose (int choice) throws InterruptedException{

        lock.lock();

        try{
        Instance agree = this.agree;
        agree.c +=1;
        agree.result = max (agree.result, choice);
            if(agree.c < n)
                    while(this.agree == agree)
                        cond.await();
            else{
                cond.signalAll();
                this.agree = new Instance(); //da reset
            }
            return agree.result;
        }
        finally {
            lock.unlock();
        }
    }
}
/* o método deverá retornar em cada thread o máximo dos valores propostos*/