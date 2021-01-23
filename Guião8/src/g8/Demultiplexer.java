package g8;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


import static g8.TaggedConnection.Frame;

public class Demultiplexer {
    private final TaggedConnection conn;
    private final Lock lock = new ReentrantLock();
    private final Map<Integer,Entry> buf = new HashMap<>();
    private IOException exception = null;


    private class Entry {
        int waiters = 0;
        final Condition cond = lock.newCondition();
        //deque == double ended queue aka pode se inserir à cabeça e na cauda
        final ArrayDeque<byte[]> queue = new ArrayDeque<>();
    }

    private Entry get(int tag){
        Entry e = buf.get(tag);
        if(e==null){
            e=new Entry();
            buf.put(tag,e);
        }
        return e;
    }

    public Demultiplexer(TaggedConnection conn){
    this.conn = conn;
    }


    public void start(){
    new Thread(()->{
        try{
            for(;;){
                Frame frame = conn.receive(); // nao tem que ter o lock detido
            lock.lock();
            try {
                Entry e = get(frame.tag);
                e.queue.add(frame.data); //com a entro acedo a queue e ponho (apenas) os dados da frame -> no "lado de colocação standard"
                e.cond.signal();
            }
            finally {
                lock.unlock();
            }
            }
        }
        catch(IOException e){
            lock.lock();
            try{
                exception = e;
                buf.forEach( (k,v) -> v.cond.signalAll());
            }
            finally {
                lock.unlock();
            }
        }
    }).start();
    }

    public void send (Frame frame) throws IOException{
        //encapsulamento
        conn.send(frame);

    }
    public void send (int tag,byte[] data) throws  IOException{
        //encapsulamento
        conn.send(tag,data);
    }

    public byte[] receive (int tag) throws IOException,InterruptedException{
        //receive de um tag
        lock.lock();
        try{
            Entry e = get(tag); //obtemos entry correspondente ao tag
            e.waiters++; //como estamos a tentar receber temos +1 metodo interessado na receçao
            // por isso incrementamos waiters
            for(;;){
                // se ha algo a consumir
                if(!e.queue.isEmpty()){ // se houverem dados para serem consumidos na queue
                    byte[] res = e.queue.poll(); //vamos retirar do lado oposto onde os dados foram inseridos
                    e.waiters--; // temos - alguem a espera do resultado
                    if(e.queue.isEmpty() && e.waiters == 0) //verificaçao final
                        buf.remove(tag); //reduzir info do map retira-se a tag
                    return res;
                }

                if(exception!= null) throw exception;
                //situacao normal
                e.cond.await();
            }
        }
        finally {
            lock.unlock();
        }
    }

    public void close() throws IOException{
        conn.close();
    }
}
