import javax.imageio.plugins.tiff.GeoTIFFTagSet;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

//Por fazer

class WarehouseV3 {
    private Map<String, Product> m =  new HashMap<String, Product>();
    private ReentrantLock lock = new ReentrantLock();
    private class Product { int q = 0;
        Condition c = lock.newCondition();
    }

    private Product get(String s) {
        lock.lock();
        try {
            Product p = m.get(s);
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
            p.c.signalAll();
        }

        finally {
            lock.unlock();
        }
    }

   //starvation --> threads podem consumir o produto que eu - como cliente (thread) - pretendo, ficando indefinidamente em sleep mode à espera
    public void consume(String[] a) throws InterruptedException{
        Product p;
        lock.lock();
        try {
            for(int i = 0;i < a.length;) {
                p = get(a[i]);
                i++;
                if (p.q == 0) {
                    p.c.await();
                    i = 0;
                }
            }

            for (String s : a) {
                p = get(s);
                p.q--;
            }
        }

        finally {
            lock.unlock();
        }
    }


}