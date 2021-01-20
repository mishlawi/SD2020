import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


//alternativa à alinea b)



public class Barrier3 {
    private int c = 0;
    private boolean open = false;
    private int n;
    Lock l = new ReentrantLock();
    Condition cond = l.newCondition();

    Barrier3(int N){
        this.n = N;
    }



    void await() throws InterruptedException{
        l.lock();
        try{
            //inicialmente open a falso
            while(open) //enquanto a barreira está aberta as threads "nao se aproximam" dela
                cond.await();
            c+=1;
                if(c<n)
                    while(!open)      //garante-se sempre a condiçao
                        cond.await();
                    else{
                        cond.signalAll();
                        open = true;
                }
                c-=1;
                    if(c==0){
                        open= false;
                        cond.signalAll();
                    }
        }
        finally {
            l.unlock();
        }
    }
}
