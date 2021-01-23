package g8;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class TaggedConnection implements AutoCloseable{
private final Socket socket;

private final DataOutputStream os;
private final DataInputStream is;
private final ReentrantLock wlock;
private final ReentrantLock rlock;

    public static class Frame{
        public final int tag;
        public final byte[] data;
        public Frame(int tag,byte[] data){
            this.tag = tag;
            this.data = data;
        }

    }

    public TaggedConnection (Socket socket) throws IOException{
        this.socket = socket;
        this.wlock = new ReentrantLock();
        this.rlock = new ReentrantLock();
        this.is =  new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public void send (Frame frame) throws IOException{
        send(frame.tag,frame.data);

    }

    public void send ( int tag, byte [] data) throws IOException{
        try{
            wlock.lock();
            os.writeInt(4 +data.length);
            // 4 bytes pertencentes ao tag + tamanho do array data
            os.writeInt(tag);
            os.write(data);
            os.flush();
        }
        finally {
            wlock.unlock();
        }

    }

    public Frame receive () throws IOException{
    byte[] data;
    int tag;
    try{
        rlock.lock(); //get lock de leitura
        data = new byte[is.readInt() -4]; //  tira -se os 4 bytes que sao do tag
        tag = is.readInt(); // leitura do tag
        is.readFully(data); //leitura completa dos dados
    }
    finally {
            rlock.unlock();
    }
    return new Frame(tag,data);
    }

    public void close() throws IOException{
        socket.close();
    }
}
