import javax.imageio.plugins.tiff.GeoTIFFTagSet;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Warehouse_Cooperativo {
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

    // Errado se faltar algum produto...
    public void consume(String[] a) throws InterruptedException{
        Product p;
        lock.lock();
        try {
            //basta falhar uma que espera que haja sinalizaçao e volta a testar tudo
            //garante que nenhum dos produtos está a 0
            for(int i = 0;i < a.length;) {
                p = get(a[i]);
                i++;
                if (p.q == 0) {
                    p.c.await();
                    i = 0;
                }
            }
            //de certa forma "retira se os produtos da prateleira apenas depois de verificar se as prateleiras têm os produtos necessarios"
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