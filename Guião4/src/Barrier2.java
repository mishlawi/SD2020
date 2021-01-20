import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//Modifique a implementação para permitir que a operação
// possa ser usada várias vezespor cada thread (barreira reutilizável),
//  de modo a suportar a sincronização no fim decada uma de várias fases de computação.


public class Barrier2 {
    private ReentrantLock lock = new ReentrantLock();
    private Condition cond = lock.newCondition();
    private int n;
    private int conta=0;
    private int era=0;

    Barrier2(int N){ this.n = N;}

    void await() throws InterruptedException{

        lock.lock();

        try {
            int hoje = era;
            conta++;
            if (conta < n )
                while (conta < n && hoje == era)
                    cond.await();
            else {
                cond.signalAll();
                //todos em que estao adormecidos vao ser acordados
                // mas nao vao ter acesso imediato ao lock, porque o último thread nao o libertou
                conta = 0;
                era++;
            }
        }
        finally {
            lock.unlock();
            }

    }

}


