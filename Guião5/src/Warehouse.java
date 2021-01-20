import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Warehouse {
    private ReentrantLock lock = new ReentrantLock();
    private Map<String, Product> m =  new HashMap<String, Product>();
    private Condition c = lock.newCondition();

    private class Product { int q = 0; }

    private Product get(String s) {
        Product p = m.get(s);
        lock.lock();
        try {
            if (p != null) return p;
            p = new Product();
            m.put(s, p);
            return p;
        }
        finally {
            lock.unlock();
        }

    }

    public void supply(String s, int q) {
        lock.lock();
        try {
            Product p = get(s);
            p.q += q;
            c.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

    // Errado se faltar algum produto...
    public void consume(String[] a) throws InterruptedException{
        Product p;
        lock.lock();
        try {
            for (String s : a) {
                p = get(s);
                if (p.q<0)
                while (p.q == 0)
                      c.await();
                p.q--;
            }
                                }
        finally {
            lock.unlock();
        }
    }

}